package it.discovery.restaurant.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import it.discovery.reactive.restaurant.repository.MealRepository;
import it.discovery.restaurant.model.Cook;
import it.discovery.restaurant.model.Meal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookService {
	
	private final MealRepository mealRepository;
	
	private final Set<Cook> availableCooks;
	
	public CookService(MealRepository mealRepository) {
		this.mealRepository = mealRepository;
		availableCooks = new CopyOnWriteArraySet<>();
		
		availableCooks.add(new Cook(1, "John"));
		availableCooks.add(new Cook(2, "Peter"));
		availableCooks.add(new Cook(3, "Alexander"));
	}
	
	public Meal cook(String name) {
		Meal meal = mealRepository.getMeal(name);
		
		if(meal == null) {
			return null;
		}

		while(availableCooks.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);			
			}
		}
		Cook cook = availableCooks.iterator().next();
		availableCooks.remove(cook);

		try {
			Thread.sleep(meal.getDuration().toMillis());
			
			return meal;
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);			
		} finally {
			availableCooks.add(cook);
		}
	}

}
