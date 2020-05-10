package it.discovery.restaurant.webflux.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("util")
public class UtilController {

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> latinSymbols() {
        return Flux.range('a', 'z' - 'a' + 1)
                .map(Character::toChars)
                .map(String::valueOf);
    }
}
