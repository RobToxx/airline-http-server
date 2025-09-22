package model;

import java.math.BigDecimal;

public enum PassengerType {
	CHILD(new BigDecimal("60500.00")),
	ADULT(new BigDecimal("65950.00")),
	SENIOR(new BigDecimal("50000.00"));

	public final BigDecimal price;

	PassengerType(BigDecimal price) {

		this.price = price;
	}
}