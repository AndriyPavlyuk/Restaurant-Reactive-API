package it.discovery.restaurant.reactor.repository;

import it.discovery.restaurant.exception.NoMealException;
import it.discovery.restaurant.model.Meal;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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

    public Mono<Meal> getMeal(String name) {
        if (!meals.containsKey(name)) {
            return Mono.error(NoMealException::new);
        }
        return Mono.just(meals.get(name))
                .delayElement(Duration.ofMillis(400));
    }
}
