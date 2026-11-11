package com.touragency.model;

/** Meals included in the price of the hotel. */
public enum Meal implements XmlNamed {
    /** Meals are not included. */
    NONE("none"),
    /** Bed and breakfast. */
    BB("BB"),
    /** Half board: breakfast and dinner. */
    HB("HB"),
    /** All inclusive. */
    AI("AI");

    private final String xmlName;

    Meal(String xmlName) {
        this.xmlName = xmlName;
    }

    @Override
    public String xmlName() {
        return xmlName;
    }

    /** @return {@code true} if at least breakfast is included */
    public boolean isIncluded() {
        return this != NONE;
    }
}
