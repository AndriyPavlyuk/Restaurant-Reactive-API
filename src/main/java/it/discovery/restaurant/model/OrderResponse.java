package it.discovery.restaurant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class OrderResponse {

    private final Meal meal;

    private final Waiter waiter;
}
