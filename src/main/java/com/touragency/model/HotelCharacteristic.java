package com.touragency.model;

import java.util.Objects;

/**
 * Characteristics of the hotel.
 *
 * @param stars          category of the hotel, 1..5
 * @param meal           which meals are included
 * @param roomPlaces     how many people the room is for, 1..3
 * @param tv             whether there is a TV set in the room
 * @param airConditioner whether there is an air conditioner in the room
 */
public record HotelCharacteristic(int stars, Meal meal, int roomPlaces, boolean tv, boolean airConditioner) {

    public static final int MIN_STARS = 1;
    public static final int MAX_STARS = 5;
    public static final int MIN_ROOM_PLACES = 1;
    public static final int MAX_ROOM_PLACES = 3;

    public HotelCharacteristic {
        Objects.requireNonNull(meal, "meal");
        requireInRange("Stars", stars, MIN_STARS, MAX_STARS);
        requireInRange("Room places", roomPlaces, MIN_ROOM_PLACES, MAX_ROOM_PLACES);
    }

    @Override
    public String toString() {
        return stars + "* hotel, meal " + meal + ", room for " + roomPlaces
                + ", TV " + yesNo(tv) + ", air conditioner " + yesNo(airConditioner);
    }

    private static String yesNo(boolean flag) {
        return flag ? "yes" : "no";
    }

    private static void requireInRange(String what, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    what + " must be between " + min + " and " + max + ", but got " + value);
        }
    }
}
