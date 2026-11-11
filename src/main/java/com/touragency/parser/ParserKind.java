package com.touragency.parser;

import java.util.function.Supplier;

/** The parsing technologies the application supports. */
public enum ParserKind {
    DOM(DomVoucherParser::new);

    private final Supplier<VoucherStreamParser> constructor;

    ParserKind(Supplier<VoucherStreamParser> constructor) {
        this.constructor = constructor;
    }

    /** @return a new parser of this kind */
    public VoucherStreamParser newParser() {
        return constructor.get();
    }
}
