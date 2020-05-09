package it.discovery.restaurant.rxjava3.bootstrap;

import io.reactivex.rxjava3.core.Observable;
import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.OrderResponse;
import it.discovery.restaurant.rxjava3.repository.MealRepository;
import it.discovery.restaurant.rxjava3.service.CookService;
import it.discovery.restaurant.rxjava3.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<Customer> customers = Stream.iterate(1, i -> i + 1).limit(20).map(i -> new Customer("Donald" + i))
                .collect(Collectors.toList());
        customers.forEach(customer -> {
            serveCustomer(customer)
                    .retry(3, ex -> ex instanceof NoAvailableWaiterException)
                    .subscribe(response -> System.out.println(
                            "Customer " + customer.getName() + ".Got orders " + response),
                            err -> {
                                System.err.println("Error: " + err);
                                sendFeedback(customer);
                            });
        });
    }

    private Observable<OrderResponse> serveCustomer(Customer customer) {
        return waiterService.acquire()
                .map(waiter -> waiterService.order(customer, waiter, mealNames))
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
