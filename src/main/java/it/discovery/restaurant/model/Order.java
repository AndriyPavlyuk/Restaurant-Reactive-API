package it.discovery.restaurant.model;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Order {
	
	private final Waiter waiter;
	
	private final Customer customer; 
	
	private final Collection<String> mealNames;

}
