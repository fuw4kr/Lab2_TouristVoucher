package com.touragency.model;

/** The way tourists are transported. */
public enum Transport implements XmlNamed {
    AIR("air"),
    RAILWAY("railway"),
    AUTO("auto"),
    LINER("liner");

    private final String xmlName;

    Transport(String xmlName) {
        this.xmlName = xmlName;
    }

    @Override
    public String xmlName() {
        return xmlName;
    }
}
