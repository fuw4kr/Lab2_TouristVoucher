package com.touragency.model;

/** Type of the voucher. */
public enum TourType implements XmlNamed {
    WEEKEND("weekend"),
    EXCURSION("excursion"),
    REST("rest"),
    PILGRIMAGE("pilgrimage");

    private final String xmlName;

    TourType(String xmlName) {
        this.xmlName = xmlName;
    }

    @Override
    public String xmlName() {
        return xmlName;
    }
}
