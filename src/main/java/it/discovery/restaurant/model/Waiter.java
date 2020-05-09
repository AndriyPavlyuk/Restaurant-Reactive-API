package it.discovery.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@EqualsAndHashCode(of="id")
@AllArgsConstructor 
@ToString
public class Waiter {
	
	private int id;

	private String name;
}
