package it.discovery.restaurant.rxjava3.bootstrap;

import io.reactivex.rxjava3.core.Observable;
import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.OrderItem;
import it.discovery.restaurant.rxjava3.repository.MealRepository;
import it.discovery.restaurant.rxjava3.service.CookService;
import it.discovery.restaurant.rxjava3.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Starter {

    private final CookService cookService;

    private final WaiterService waiterService;

    private final FacebookConnector facebookConnector;

    private final SiteConnector siteConnector;

    private final Set<String> mealNames;

    public Starter() {
        MealRepository mealRepository = new MealRepository();
        cookService = new CookService(mealRepository);
        waiterService = new WaiterService(cookService);
        facebookConnector = new FacebookConnector();
        siteConnector = new SiteConnector();
        mealNames = mealRepository.getMealNames();
    }

    public static void main(String[] args) {
        Starter starter = new Starter();
        starter.start();
    }

    private void start() {
        Observable.range(1, 20)
                .map(i -> new Customer("Donald" + i))
                .flatMap(this::serveCustomer)
                .retry(3, ex -> ex instanceof NoAvailableWaiterException)
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

    private Observable<OrderItem> serveCustomer(Customer customer) {
        return waiterService.acquire(customer)
                .map(waiter -> waiterService.order(customer, waiter, mealNames))
                .doOnNext(order -> waiterService.release(order.getWaiter()))
                .flatMap(waiterService::take)
                .timeout(3, TimeUnit.SECONDS);
    }

    private void sendFeedback(Customer customer) {
        String feedback = "Customer " + customer.getName() +
                " is unhappy because no available waiters";
        siteConnector.saveFeedback(feedback);
        facebookConnector.saveFeedback(feedback);
    }
}
