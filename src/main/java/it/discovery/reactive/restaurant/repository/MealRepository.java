package it.discovery.reactive.restaurant.repository;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import it.discovery.restaurant.model.Meal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MealRepository {
	
	private final Map<String, Meal> meals;
	
	public MealRepository() {
		meals = new ConcurrentHashMap<>();
		meals.put("coffee", new Meal("coffee", 10, Duration.ofSeconds(1)));
		meals.put("soup", new Meal("soup", 50, Duration.ofSeconds(3)));
		meals.put("beef", new Meal("beef", 150, Duration.ofSeconds(5)));
	}
	
	public Set<String> getMealNames() {
		return meals.keySet();
	}

	public Meal getMeal(String name) {
		try {
			Thread.sleep(2000);
			
			return meals.get(name);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
