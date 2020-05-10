package it.discovery.restaurant.rxjava3.bootstrap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.OrderItem;
import it.discovery.restaurant.model.Visit;
import it.discovery.restaurant.rxjava3.repository.MealRepository;
import it.discovery.restaurant.rxjava3.service.CookService;
import it.discovery.restaurant.rxjava3.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.LocalTime.now;

public class Starter {
    private static final int NUM_SEATS = 10;

    private final CookService cookService;

    private final WaiterService waiterService;

    private final FacebookConnector facebookConnector;

    private final SiteConnector siteConnector;

    private final Set<String> mealNames;

    private final Subject<String> feedbackHandler;

    private final Scheduler scheduler;

    private final ExecutorService executorService;

    public Starter() {
        MealRepository mealRepository = new MealRepository();
        cookService = new CookService(mealRepository);
        waiterService = new WaiterService(cookService);
        facebookConnector = new FacebookConnector();
        siteConnector = new SiteConnector();
        mealNames = mealRepository.getMealNames();

        feedbackHandler = PublishSubject.create();
        feedbackHandler.subscribe(siteConnector::saveFeedback);
        feedbackHandler.subscribe(facebookConnector::saveFeedback);

        executorService = Executors.newFixedThreadPool(NUM_SEATS);
        scheduler = Schedulers.from(executorService, true);
    }

    public static void main(String[] args) throws Exception {
        Starter starter = new Starter();
        try {
            starter.start();
        } finally {
            //starter.close();
        }
    }

    private void close() throws Exception {
        executorService.shutdown();
        waiterService.close();
    }

    private void start() {
        Observable.range(1, 20)
                .observeOn(scheduler)
                .map(i -> new Customer("Donald" + i))
                .map(customer -> new Visit(customer, now()))
                .flatMap(this::serveCustomer)
                .retry(3, ex -> ex instanceof NoAvailableWaiterException)
                .doAfterTerminate(this::close)
                .subscribe(orderItem -> System.out.println(
                        "Customer " + orderItem.getOrder().getCustomer().getName() + ".Got orders " + orderItem),
                        err -> {
                            System.err.println("Error: " + err);
                            if (err instanceof NoAvailableWaiterException) {
                                Customer customer = ((NoAvailableWaiterException) err).getCustomer();
                                sendFeedback(customer);
                            }
                        });

    }

    private Observable<OrderItem> serveCustomer(Visit visit) {
        return waiterService.acquire(visit)
                .map(waiter -> waiterService.order(visit.getCustomer(), waiter, mealNames))
                .doOnNext(order -> waiterService.release(order.getWaiter()))
                .flatMap(waiterService::take)
                .timeout(3, TimeUnit.SECONDS);
    }

    private void sendFeedback(Customer customer) {
        String feedback = "Customer " + customer.getName() +
                " is unhappy because no available waiters";
        feedbackHandler.onNext(feedback);
    }
}
