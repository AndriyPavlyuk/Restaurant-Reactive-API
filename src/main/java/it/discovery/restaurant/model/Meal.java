package it.discovery.restaurant.model;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter 
@AllArgsConstructor
@ToString
public class Meal {
	
	private String name;
	
	private double price;
	
	/**
	 * Time to cook this meal
	 */
	private Duration duration;

}
