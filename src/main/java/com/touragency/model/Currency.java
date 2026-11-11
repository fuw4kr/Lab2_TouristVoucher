package com.touragency.model;

/** Currency the cost of a voucher is given in. */
public enum Currency implements XmlNamed {
    UAH,
    USD,
    EUR;

    /** Currency codes are written in XML exactly like the constants. */
    @Override
    public String xmlName() {
        return name();
    }
}
