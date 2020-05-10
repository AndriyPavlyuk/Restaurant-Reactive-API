package it.discovery.restaurant.reactor.service;

import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.exception.RestaurantClosingException;
import it.discovery.restaurant.model.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaiterService implements AutoCloseable {

    private final Set<Waiter> availableWaiters;

    private final CookService cookService;

    private final Scheduler scheduler;

    private final ExecutorService executorService;

    private final LocalTime workEndTime;

    public WaiterService(CookService cookService) {
        availableWaiters = new CopyOnWriteArraySet<>();
        this.cookService = cookService;

        availableWaiters.add(new Waiter(1, "Samanta"));
        availableWaiters.add(new Waiter(2, "Ann"));
        availableWaiters.add(new Waiter(3, "Tiffany"));

        executorService = Executors.newFixedThreadPool(availableWaiters.size());
        scheduler = Schedulers.fromExecutor(executorService);

        workEndTime = LocalTime.of(23, 0);
    }

    /**
     * Acquire first available waiter
     *
     * @param visit
     * @return
     */
    public Flux<Waiter> acquire(Visit visit) {
        if (visit.getCreated().isAfter(workEndTime)) {
            return Flux.error(RestaurantClosingException::new);
        }

        if (availableWaiters.isEmpty()) {
            return Flux.error(() -> new NoAvailableWaiterException(visit.getCustomer()));
        }

        Waiter waiter = availableWaiters.iterator().next();
        availableWaiters.remove(waiter);

        return Flux.just(waiter);
    }

    public void release(Waiter waiter) {
        availableWaiters.add(waiter);
    }

    public Order order(Customer customer, Waiter waiter, Collection<String> mealNames) {
        return new Order(waiter, customer, mealNames);
    }

    public Flux<OrderItem> take(Order order) {
        return Flux.fromIterable(order.getMealNames())
                .flatMap(cookService::cook)
                .map(meal -> new OrderItem(meal, order));
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
