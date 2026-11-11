package com.touragency.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One thing included in the cost of a voucher.
 *
 * @param name  what is included: flight, hotel, excursions...
 * @param price how much of the total cost it takes
 */
public record IncludedService(String name, BigDecimal price) {

    public IncludedService {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name of a service is empty");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price of '" + name + "' is negative: " + price);
        }
    }

    @Override
    public String toString() {
        return name + " (" + price + ")";
    }
}
