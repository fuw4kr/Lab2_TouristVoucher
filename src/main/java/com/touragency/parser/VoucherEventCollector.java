package com.touragency.parser;

import com.touragency.model.Voucher;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Receives the events of a streaming parser (element opened, text, element
 * closed) and builds vouchers out of them. SAX and StAX deliver the same events
 * in different ways, so both parsers delegate to this class.
 */
final class VoucherEventCollector {

    private final List<Voucher> vouchers = new ArrayList<>();
    /** Attributes of the elements that are open at the moment, the innermost one on top. */
    private final Deque<Map<String, String>> openElements = new ArrayDeque<>();
    private final StringBuilder text = new StringBuilder();
    private VoucherBuilder builder;

    void elementOpened(String name, Map<String, String> attributes) {
        openElements.push(attributes);
        text.setLength(0);
        if (VoucherTag.VOUCHER.xmlName().equals(name)) {
            builder = new VoucherBuilder();
        }
    }

    /** May be called several times for one text node. */
    void text(String part) {
        text.append(part);
    }

    void elementClosed(String name) {
        Map<String, String> attributes = openElements.pop();
        VoucherTag.find(name)
                .filter(VoucherTag::isBound)
                .ifPresent(tag -> store(tag, attributes));
        text.setLength(0);
    }

    private void store(VoucherTag tag, Map<String, String> attributes) {
        if (builder == null) {
            throw new IllegalArgumentException("<" + tag.xmlName() + "> must be inside <"
                    + VoucherTag.VOUCHER.xmlName() + ">");
        }
        tag.bind(builder, attributes, text.toString());
        if (tag == VoucherTag.VOUCHER) {
            vouchers.add(builder.build());
            builder = null;
        }
    }

    List<Voucher> vouchers() {
        return vouchers;
    }
}
