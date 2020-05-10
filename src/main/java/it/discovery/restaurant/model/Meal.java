package it.discovery.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

@Getter
@AllArgsConstructor
@ToString(exclude = "duration")
public class Meal {
	
	private String name;
	
	private double price;
	
	/**
	 * Time to cook this meal
	 */
	private Duration duration;

}
