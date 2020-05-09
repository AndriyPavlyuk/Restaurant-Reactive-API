package it.discovery.restaurant.rxjava3.repository;

import io.reactivex.rxjava3.core.Observable;
import it.discovery.restaurant.exception.NoMealException;
import it.discovery.restaurant.model.Meal;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

	public Observable<Meal> getMeal(String name) {
		if (!meals.containsKey(name)) {
			return Observable.error(NoMealException::new);
		}
		return Observable.just(meals.get(name))
				.delay(400, TimeUnit.MILLISECONDS);
	}
}
