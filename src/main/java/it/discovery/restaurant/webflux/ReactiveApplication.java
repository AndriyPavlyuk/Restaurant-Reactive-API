package it.discovery.restaurant.webflux;

import it.discovery.restaurant.reactor.repository.MealRepository;
import it.discovery.restaurant.reactor.service.CookService;
import it.discovery.restaurant.reactor.service.WaiterService;
import it.discovery.restaurant.webflux.client.CookClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }

    @Bean
    public MealRepository mealRepository() {
        return new MealRepository();
    }

    @Bean
    public CookService cookService() {
        return new CookService(mealRepository());
    }

    @Bean
    public WaiterService waiterService() {
        return new WaiterService(cookService());
    }

    @Bean
    public CookClient cookClient() {
        return new CookClient();
    }
}
