package it.discovery.restaurant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
@ToString(of = "waiter")
public class Order {
	
	private final Waiter waiter;
	
	private final Customer customer; 
	
	private final Collection<String> mealNames;

}
