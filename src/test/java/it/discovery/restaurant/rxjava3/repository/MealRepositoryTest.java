package it.discovery.restaurant.rxjava3.repository;

import io.reactivex.rxjava3.subscribers.TestSubscriber;
import it.discovery.restaurant.exception.NoMealException;
import it.discovery.restaurant.model.Meal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class MealRepositoryTest {

    MealRepository mealRepository;

    @BeforeEach
    void setup() {
        mealRepository = new MealRepository();
    }

    @Test
    void getMealNames_returnsAll() {
        Set<String> names = mealRepository.getMealNames();

        assertNotNull(names);
        assertEquals(3, names.size());
        assertAll(() -> assertTrue(names.contains("coffee")), () -> assertTrue(names.contains("soup")),
                () -> assertTrue(names.contains("beef")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"coffee", "soup", "beef"})
    void getMeal_mealPresent_success(String name) throws InterruptedException {
        TestSubscriber<Meal> subscriber = new TestSubscriber<>();
        mealRepository.getMeal(name).subscribe(subscriber);

        subscriber.await(1, TimeUnit.SECONDS);
        subscriber.assertComplete();
        subscriber.assertValue(meal -> meal.getName().equals(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "tea"})
    void getMeal_mealAbsent_error(String name) throws InterruptedException {
        TestSubscriber<Meal> subscriber = new TestSubscriber<>();
        mealRepository.getMeal(name).subscribe(subscriber);

        subscriber.await(1, TimeUnit.SECONDS);
        subscriber.assertError(NoMealException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 50})
    void getMeal_timeTooShort_timeout(int timeout) throws InterruptedException {
        TestSubscriber<Meal> subscriber = new TestSubscriber<>();
        mealRepository.getMeal("coffee").subscribe(subscriber);

        subscriber.await(timeout, TimeUnit.MILLISECONDS);
        subscriber.assertNotComplete();
    }
}
