package it.discovery.restaurant.reactor.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.discovery.restaurant.model.Cook;
import it.discovery.restaurant.model.Meal;
import it.discovery.restaurant.reactor.repository.MealRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@Slf4j
public class CookService implements AutoCloseable {

    private final MealRepository mealRepository;

    private final List<Cook> availableCooks;

    private final Random random = new Random();

    private final Map<Cook, Scheduler> schedulers;

    public CookService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
        availableCooks = new CopyOnWriteArrayList<>();
        schedulers = new ConcurrentHashMap<>();

        availableCooks.add(new Cook(1, "John"));
        availableCooks.add(new Cook(2, "Peter"));
        availableCooks.add(new Cook(3, "Alexander"));
    }

    private Scheduler getScheduler(Cook cook) {
        return schedulers.computeIfAbsent(cook, key -> {
            ThreadFactory factory = new ThreadFactoryBuilder()
                    .setNameFormat(cook.getName() + "-" + cook.getId()).build();
            ExecutorService executorService = Executors.newSingleThreadExecutor(factory);
            return Schedulers.fromExecutor(executorService);
        });
    }

    private Cook randomCook() {
        return availableCooks.get(random.nextInt(3));
    }

    public Mono<Meal> cook(String name) {
        return Mono.just(name)
                .flatMap(mealRepository::getMeal)
                .publishOn(getScheduler(randomCook()))
                .doOnNext(item -> {
                    String cook = Thread.currentThread().getName();
                    System.out.println("Cook " + cook + " is assigned to meal " + item);
                }).delayUntil(meal -> Mono.just(meal.getDuration()));
    }

    @Override
    public void close() throws Exception {
        schedulers.values().forEach(Scheduler::dispose);
    }
}
