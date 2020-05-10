package it.discovery.restaurant.rxjava3.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.discovery.restaurant.model.Cook;
import it.discovery.restaurant.model.Meal;
import it.discovery.restaurant.rxjava3.repository.MealRepository;
import lombok.extern.slf4j.Slf4j;

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
            return Schedulers.from(executorService);
        });
    }

    private Cook randomCook() {
        return availableCooks.get(random.nextInt(3));
    }

    public Flowable<Meal> cook(String name) {
        return Flowable.just(name)
                .flatMap(mealRepository::getMeal)
                .observeOn(getScheduler(randomCook()))
                .doOnNext(item -> {
                    String cook = Thread.currentThread().getName();
                    System.out.println("Cook " + cook + " is assigned to meal " + item);
                }).delay(meal -> Flowable.just(meal.getDuration()));
    }

    @Override
    public void close() throws Exception {
        schedulers.values().forEach(Scheduler::shutdown);
    }
}
