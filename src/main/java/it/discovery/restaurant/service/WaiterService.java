package it.discovery.restaurant.service;

import it.discovery.restaurant.exception.NoAvailableWaiterException;
import it.discovery.restaurant.model.Customer;
import it.discovery.restaurant.model.Order;
import it.discovery.restaurant.model.OrderResponse;
import it.discovery.restaurant.model.Waiter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

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
	 * @return
	 */
	public Waiter acquire() {
		if (availableWaiters.isEmpty()) {
			throw new NoAvailableWaiterException();
		}

		Waiter waiter = availableWaiters.iterator().next();
		availableWaiters.remove(waiter);

		return waiter;
	}

	public void release(Waiter waiter) {
		availableWaiters.add(waiter);
	}

	public Order order(Customer customer, Waiter waiter, Collection<String> mealNames) {
		return new Order(waiter, customer, mealNames);
	}

	public List<OrderResponse> take(Order order) {
		return order.getMealNames().stream()
				.map(mealName -> new OrderResponse(cookService.cook(mealName), order.getWaiter()))
				.collect(Collectors.toList());
	}

}
