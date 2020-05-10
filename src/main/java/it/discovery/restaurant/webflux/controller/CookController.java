package it.discovery.restaurant.webflux.controller;

import it.discovery.restaurant.model.Meal;
import it.discovery.restaurant.reactor.service.CookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("cook")
public class CookController {
    private final CookService cookService;

    @GetMapping(path = "{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Meal> cook(@PathVariable String name) {
        return cookService.cook(name);
    }
}
