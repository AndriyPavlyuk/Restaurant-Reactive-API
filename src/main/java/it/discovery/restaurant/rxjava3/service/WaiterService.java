package it.discovery.restaurant.rxjava3.service;

import io.reactivex.rxjava3.core.Observable;
import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.Order;
import it.discovery.restaurant.model.OrderResponse;
import it.discovery.restaurant.model.Waiter;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WaiterService {

    private final Set<Waiter> availableWaiters;

    private final CookService cookService;

    public WaiterService(CookService cookService) {
        availableWaiters = new CopyOnWriteArraySet<>();
        this.cookService = cookService;

        availableWaiters.add(new Waiter(1, "Samanta"));
        availableWaiters.add(new Waiter(2, "Ann"));
        availableWaiters.add(new Waiter(3, "Tiffany"));
    }

    /**
     * Acquire first available waiter
     *
     * @return
     */
    public Observable<Waiter> acquire() {
        if (availableWaiters.isEmpty()) {
            return Observable.error(NoAvailableWaiterException::new);
        }

        Waiter waiter = availableWaiters.iterator().next();
        availableWaiters.remove(waiter);

        return Observable.just(waiter);
    }

    public void release(Waiter waiter) {
        availableWaiters.add(waiter);
    }

    public Order order(Customer customer, Waiter waiter, Collection<String> mealNames) {
        return new Order(waiter, customer, mealNames);
    }

    public Observable<OrderResponse> take(Order order) {
        return Observable.fromIterable(order.getMealNames())
                .flatMap(cookService::cook)
                .map(meal -> new OrderResponse(meal, order.getWaiter()));
    }

}
