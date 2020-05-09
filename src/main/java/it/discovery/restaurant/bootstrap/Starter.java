package it.discovery.restaurant.bootstrap;

import it.discovery.reactive.restaurant.repository.MealRepository;
import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.exception.NoMealException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.OrderItem;
import it.discovery.restaurant.service.CookService;
import it.discovery.restaurant.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
            List<OrderItem> orderRespons = null;
            int attempts = 0;

            while (attempts <= 3 && orderRespons == null) {
                try {
                    orderRespons = serveCustomer(customer);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof NoAvailableWaiterException) {
                        attempts++;
                    } else if (e.getCause() instanceof NoMealException) {
                        System.out.println("No orders prepared");
                        break;
                    }
                } catch (InterruptedException | TimeoutException e) {
                    System.out.println("Timeout for customer " + customer.getName());
                }
            }
            if (orderRespons != null) {
                System.out.println("Customer " + customer.getName() + ".Got orders " + orderRespons);
            }
        });
    }

    private List<OrderItem> serveCustomer(Customer customer) throws InterruptedException, ExecutionException, TimeoutException {
        return CompletableFuture.supplyAsync(waiterService::acquire)
                .thenApply(waiter -> waiterService.order(customer, waiter, mealNames))
                .thenApplyAsync(waiterService::take)
                .whenComplete((orderResponses, ex) -> {
                    if (orderResponses != null) {
                        waiterService.release(orderResponses.get(0).getOrder().getWaiter());
                    }
                }).get(3, TimeUnit.SECONDS);
    }

    private void sendFeedback(Customer customer) {
        String feedback = "Customer " + customer.getName() +
                " is unhappy because no available waiters";
        siteConnector.saveFeedback(feedback);
        facebookConnector.saveFeedback(feedback);
    }
}
