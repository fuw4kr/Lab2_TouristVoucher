package com.touragency.parser;

import com.touragency.model.Voucher;
import com.touragency.xml.VoucherXmlException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Reads vouchers from an XML file with the chosen parser. Everything the three
 * parsers have in common (working with the file, logging, turning low-level
 * exceptions into {@link VoucherXmlException}) is here.
 */
public final class VoucherReader {

    private static final Logger LOG = LoggerFactory.getLogger(VoucherReader.class);

    private final String parserName;
    private final VoucherStreamParser parser;

    public VoucherReader(ParserKind kind) {
        this(kind.name(), kind.newParser());
    }

    VoucherReader(String parserName, VoucherStreamParser parser) {
        this.parserName = parserName;
        this.parser = parser;
    }

    /**
     * @param xml the file to read
     * @return vouchers in the order of the document
     * @throws VoucherXmlException if the file cannot be read, is not well-formed or contains wrong data
     */
    public List<Voucher> read(Path xml) {
        LOG.debug("{}: reading {}", parserName, xml);
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(xml))) {
            List<Voucher> vouchers = parser.parse(stream);
            LOG.info("{}: {} vouchers read from {}", parserName, vouchers.size(), xml.getFileName());
            return vouchers;
        } catch (IOException | SAXException | ParserConfigurationException | XMLStreamException
                 | IllegalArgumentException e) {
            LOG.error("{}: cannot read {}: {}", parserName, xml, e.toString());
            throw new VoucherXmlException(parserName + " parser cannot read " + xml + ": " + e.getMessage(), e);
        }
    }
}
