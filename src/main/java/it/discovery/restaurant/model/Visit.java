package it.discovery.restaurant.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Visit {
    private final Customer customer;

    private final LocalTime created;
}
