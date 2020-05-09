package it.discovery.restaurant.rxjava3.service;

import io.reactivex.rxjava3.core.Observable;
import it.discovery.restaurant.model.Cook;
import it.discovery.restaurant.model.Meal;
import it.discovery.restaurant.rxjava3.repository.MealRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

	public Observable<Meal> cook(String name) {
		Observable<Meal> obs = mealRepository.getMeal(name);

		while (availableCooks.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		Cook cook = availableCooks.iterator().next();
		availableCooks.remove(cook);

		return obs.delay(meal -> Observable.just(meal.getDuration()))
				.doAfterNext(meal -> availableCooks.add(cook));
	}

}
