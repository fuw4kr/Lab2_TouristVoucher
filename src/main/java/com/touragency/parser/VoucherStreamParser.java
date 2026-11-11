package com.touragency.parser;

import com.touragency.model.Voucher;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

/**
 * A parsing technology (DOM, SAX or StAX) that turns an XML stream into vouchers.
 * Implementations contain only the code specific to the technology; opening the
 * file, logging and error handling are done once in {@link VoucherReader}.
 */
@FunctionalInterface
public interface VoucherStreamParser {

    /**
     * @param xml the document; the caller closes the stream
     * @return vouchers in the order of the document
     * @throws IllegalArgumentException if the document is well-formed but its data is wrong
     */
    List<Voucher> parse(InputStream xml)
            throws IOException, SAXException, ParserConfigurationException, XMLStreamException;
}
