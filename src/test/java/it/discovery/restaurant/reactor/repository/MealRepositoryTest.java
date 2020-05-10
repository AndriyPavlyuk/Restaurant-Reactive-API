package it.discovery.restaurant.reactor.repository;

import it.discovery.restaurant.exception.NoMealException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Set;

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
        StepVerifier.create(mealRepository.getMeal(name))
                .thenAwait(Duration.ofSeconds(1))
                .expectNextMatches(meal -> meal.getName().equals(name))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "tea"})
    void getMeal_mealAbsent_error(String name) throws InterruptedException {
        StepVerifier.create(mealRepository.getMeal(name))
                .thenAwait(Duration.ofSeconds(1))
                .verifyError(NoMealException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 50})
    void getMeal_timeTooShort_timeout(int timeout) throws InterruptedException {
        StepVerifier.create(mealRepository.getMeal("beef"))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(timeout))
                .thenAwait(Duration.ofMillis(1000 - timeout))
                .expectNextCount(1)
                .verifyComplete();
    }
}
