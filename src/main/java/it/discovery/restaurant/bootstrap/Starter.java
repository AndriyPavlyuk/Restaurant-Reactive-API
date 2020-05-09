package it.discovery.restaurant.bootstrap;

import it.discovery.reactive.restaurant.repository.MealRepository;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.Meal;
import it.discovery.restaurant.model.Order;
import it.discovery.restaurant.model.Waiter;
import it.discovery.restaurant.service.CookService;
import it.discovery.restaurant.service.WaiterService;
import it.discovery.restaurant.social.FacebookConnector;
import it.discovery.restaurant.social.SiteConnector;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Starter {

    public static void main(String[] args) {
        MealRepository mealRepository = new MealRepository();
        CookService cookService = new CookService(mealRepository);

        WaiterService waiterService = new WaiterService(cookService);

        FacebookConnector facebookConnector = new FacebookConnector();

        SiteConnector siteConnector = new SiteConnector();

        Set<String> mealNames = mealRepository.getMealNames();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Customer> customers = Stream.iterate(1, i -> i + 1).limit(20).map(i -> new Customer("Donald" + i))
                .collect(Collectors.toList());
        customers.forEach(customer -> {
            Future<Waiter> future = executorService.submit(waiterService::acquire);
            int attempts = 0;
            Waiter waiter = null;
            while (attempts <= 3 && waiter == null) {
                try {
                    waiter = future.get(3, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    //e.printStackTrace();
                    attempts++;
                }
            }
            if (waiter == null) {
                String feedback = "Customer " + customer.getName() +
                        " is unhappy because no available waiters";
                siteConnector.saveFeedback(feedback);
                facebookConnector.saveFeedback(feedback);
            } else {
                Order order = waiterService.order(customer, waiter, mealNames);
                List<Meal> meals = waiterService.take(order);
                if (meals == null) {
                    System.out.println("No order prepared");
                } else {
                    System.out.println("Got order " + order.getMealNames());
                }
                waiterService.release(waiter);
            }
        });

    }
}
