package com.touragency.xml;

/** Thrown when an XML file cannot be read, parsed, validated or transformed. */
public class VoucherXmlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VoucherXmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
