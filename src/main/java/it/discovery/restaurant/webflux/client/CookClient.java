package it.discovery.restaurant.webflux.client;

import it.discovery.restaurant.model.Meal;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CookClient {

    private final WebClient client;

    public CookClient() {
        client = WebClient.create("http://localhost:8080");
    }

    public Mono<Meal> cook(String name) {
        return client.get().uri("/cook/" + name)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMap(res -> res.bodyToMono(Meal.class));
    }
}
