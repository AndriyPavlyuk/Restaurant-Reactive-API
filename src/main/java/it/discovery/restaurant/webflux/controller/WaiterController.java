package it.discovery.restaurant.webflux.controller;

import it.discovery.restaurant.model.Order;
import it.discovery.restaurant.model.OrderItem;
import it.discovery.restaurant.reactor.service.WaiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("waiter")
@RequiredArgsConstructor
public class WaiterController {

    private final WaiterService waiterService;

    @PostMapping(path = "order", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderItem> takeOrder(@RequestBody Order order) {
        return waiterService.take(order);
    }
}
