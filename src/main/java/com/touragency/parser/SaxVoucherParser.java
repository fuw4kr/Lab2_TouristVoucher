package com.touragency.parser;

import com.touragency.model.Voucher;
import com.touragency.xml.SafeXml;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Reads vouchers with the SAX API: the parser calls back the handler for every part of the document. */
public final class SaxVoucherParser implements VoucherStreamParser {

    @Override
    public List<Voucher> parse(InputStream xml) throws IOException, SAXException, ParserConfigurationException {
        VoucherEventCollector collector = new VoucherEventCollector();
        SafeXml.saxParser().parse(xml, new CollectingHandler(collector));
        return collector.vouchers();
    }

    /** Translates SAX callbacks into calls of {@link VoucherEventCollector}. */
    private static final class CollectingHandler extends DefaultHandler {

        private final VoucherEventCollector collector;

        CollectingHandler(VoucherEventCollector collector) {
            this.collector = collector;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // the Attributes object is reused by the parser, so its content has to be copied
            Map<String, String> copy = new HashMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                copy.put(attributes.getLocalName(i), attributes.getValue(i));
            }
            collector.elementOpened(localName, copy);
        }

        @Override
        public void characters(char[] chars, int start, int length) {
            collector.text(String.valueOf(chars, start, length));
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            collector.elementClosed(localName);
        }
    }
}
