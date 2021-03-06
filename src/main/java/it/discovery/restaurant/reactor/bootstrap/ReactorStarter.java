package it.discovery.restaurant.reactor.bootstrap;

import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.OrderItem;
import it.discovery.restaurant.model.Visit;
import it.discovery.restaurant.reactor.repository.MealRepository;
import it.discovery.restaurant.reactor.service.CookService;
import it.discovery.restaurant.reactor.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.time.LocalTime.now;

public class ReactorStarter {
    private static final int NUM_SEATS = 10;

    private final CookService cookService;

    private final WaiterService waiterService;

    private final FacebookConnector facebookConnector;

    private final SiteConnector siteConnector;

    private final Set<String> mealNames;

    private final DirectProcessor<String> feedbackHandler;

    private final Scheduler scheduler;

    private final ExecutorService executorService;

    public ReactorStarter() {
        MealRepository mealRepository = new MealRepository();
        cookService = new CookService(mealRepository);
        waiterService = new WaiterService(cookService);
        facebookConnector = new FacebookConnector();
        siteConnector = new SiteConnector();
        mealNames = mealRepository.getMealNames();

        feedbackHandler = DirectProcessor.create();
        feedbackHandler.subscribe(siteConnector::saveFeedback);
        feedbackHandler.subscribe(facebookConnector::saveFeedback);

        executorService = Executors.newFixedThreadPool(NUM_SEATS);
        scheduler = Schedulers.fromExecutor(executorService);
    }

    public static void main(String[] args) throws Exception {
        ReactorStarter starter = new ReactorStarter();
        try {
            starter.start();
        } finally {
            //starter.close();
        }
    }

    private void close() {
        executorService.shutdown();
        try {
            waiterService.close();
            cookService.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void start() {
        Flux.range(1, 20)
                .publishOn(scheduler)
                .map(i -> new Customer("Donald" + i))
                .map(customer -> new Visit(customer, now()))
                .onBackpressureBuffer(10, visit -> System.out.println("Queue overflow"),
                        BufferOverflowStrategy.DROP_LATEST)
                .flatMap(this::serveCustomer)
                .retry(3, ex -> ex instanceof NoAvailableWaiterException)
                .doAfterTerminate(this::close)
                .subscribe(orderItem -> System.out.println(
                        "Customer " + orderItem.getOrder().getCustomer().getName() + " got orders " + orderItem),
                        err -> {
                            System.err.println("Error: " + err);
                            if (err instanceof NoAvailableWaiterException) {
                                Customer customer = ((NoAvailableWaiterException) err).getCustomer();
                                sendFeedback(customer);
                            }
                        });

    }

    private Flux<OrderItem> serveCustomer(Visit visit) {
        return waiterService.acquire(visit)
                .map(waiter -> waiterService.order(visit.getCustomer(), waiter, mealNames))
                .doOnNext(order -> waiterService.release(order.getWaiter()))
                .flatMap(waiterService::take)
                .timeout(Duration.ofSeconds(3));
    }

    private void sendFeedback(Customer customer) {
        String feedback = "Customer " + customer.getName() +
                " is unhappy because no available waiters";
        feedbackHandler.onNext(feedback);
    }
}
