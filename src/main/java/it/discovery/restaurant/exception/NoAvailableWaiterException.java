package it.discovery.restaurant.exception;

import it.discovery.restaurant.model.Customer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NoAvailableWaiterException extends RuntimeException {
    private final Customer customer;
}
