package com.touragency.model;

/**
 * Length of the trip (the {@code NumberDaysNights} element).
 *
 * @param days   number of days, at least one
 * @param nights number of nights, cannot exceed the number of days
 */
public record StayDuration(int days, int nights) {

    public StayDuration {
        if (days < 1) {
            throw new IllegalArgumentException("A trip lasts at least one day, but got " + days);
        }
        if (nights < 0 || nights > days) {
            throw new IllegalArgumentException(
                    "Number of nights must be between 0 and " + days + ", but got " + nights);
        }
    }

    @Override
    public String toString() {
        return days + " days / " + nights + " nights";
    }
}
