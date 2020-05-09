package it.discovery.reactive.restaurant.repository;

import it.discovery.restaurant.model.Meal;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MealRepository {
	
	private final Map<String, Meal> meals;
	
	public MealRepository() {
		meals = new ConcurrentHashMap<>();
		meals.put("coffee", new Meal("coffee", 10, Duration.ofMillis(200)));
		meals.put("soup", new Meal("soup", 50, Duration.ofMillis(400)));
		meals.put("beef", new Meal("beef", 150, Duration.ofMillis(600)));
	}
	
	public Set<String> getMealNames() {
		return meals.keySet();
	}

	public Meal getMeal(String name) {
		try {
			Thread.sleep(400);

			return meals.get(name);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
