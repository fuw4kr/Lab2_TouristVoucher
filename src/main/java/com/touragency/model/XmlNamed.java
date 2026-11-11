package com.touragency.model;

import java.util.Arrays;

/** Implemented by enums whose constants have a fixed spelling in the XML document. */
public interface XmlNamed {

    /** @return the constant exactly as it is written in XML */
    String xmlName();

    /**
     * Looks up an enum constant by its XML spelling.
     *
     * @param type the enum to search in
     * @param xml  the text read from the document
     * @throws IllegalArgumentException if the enum has no constant with this spelling
     */
    static <T extends Enum<T> & XmlNamed> T fromXml(Class<T> type, String xml) {
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.xmlName().equals(xml))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "'" + xml + "' is not a valid " + type.getSimpleName()));
    }
}
