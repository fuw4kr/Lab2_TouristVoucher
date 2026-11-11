package com.touragency.parser;

import com.touragency.model.Voucher;
import com.touragency.xml.SafeXml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/** Reads vouchers with the StAX event API: the application asks the reader for the next event itself. */
public final class StaxVoucherParser implements VoucherStreamParser {

    @Override
    public List<Voucher> parse(InputStream xml) throws XMLStreamException {
        VoucherEventCollector collector = new VoucherEventCollector();
        XMLEventReader reader = SafeXml.staxFactory().createXMLEventReader(xml);
        try {
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    collector.elementOpened(element.getName().getLocalPart(), attributes(element));
                } else if (event.isCharacters()) {
                    collector.text(event.asCharacters().getData());
                } else if (event.isEndElement()) {
                    collector.elementClosed(event.asEndElement().getName().getLocalPart());
                }
            }
        } finally {
            reader.close();
        }
        return collector.vouchers();
    }

    private static Map<String, String> attributes(StartElement element) {
        Map<String, String> attributes = new HashMap<>();
        Iterator<Attribute> iterator = element.getAttributes();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            attributes.put(attribute.getName().getLocalPart(), attribute.getValue());
        }
        return attributes;
    }
}
