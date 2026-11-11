package com.touragency.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Cost of a voucher: how much it is and what is included.
 *
 * @param currency currency of all amounts
 * @param total    full price of the voucher, greater than zero
 * @param services what is included in the price, at least one item
 */
public record Cost(Currency currency, BigDecimal total, List<IncludedService> services) {

    public Cost {
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(total, "total");
        services = List.copyOf(services);
        if (total.signum() <= 0) {
            throw new IllegalArgumentException("Total cost must be positive, but got " + total);
        }
        if (services.isEmpty()) {
            throw new IllegalArgumentException("Cost must include at least one service");
        }
    }

    /** @return the sum of prices of all included services */
    public BigDecimal servicesTotal() {
        return services.stream()
                .map(IncludedService::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return total + " " + currency + " " + services;
    }
}
