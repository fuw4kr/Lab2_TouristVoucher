package com.touragency.validation;

import com.touragency.xml.SafeXml;
import com.touragency.xml.VoucherXmlException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Checks XML documents against an XSD schema. */
public final class XsdValidator {

    private static final Logger LOG = LoggerFactory.getLogger(XsdValidator.class);

    private final Schema schema;
    private final Path xsd;

    /**
     * @param xsd the schema file; it is compiled once and can be used for many documents
     * @throws VoucherXmlException if the schema cannot be read or is not a correct XSD
     */
    public XsdValidator(Path xsd) {
        this.xsd = xsd;
        try (InputStream stream = Files.newInputStream(xsd)) {
            this.schema = SafeXml.schemaFactory().newSchema(new StreamSource(stream));
        } catch (IOException | SAXException e) {
            throw new VoucherXmlException("Cannot load schema " + xsd + ": " + e.getMessage(), e);
        }
        LOG.debug("Schema {} loaded", xsd);
    }

    /**
     * @param xml the document to check
     * @return descriptions of everything that is wrong with the document; an empty list if it is valid
     * @throws VoucherXmlException if the document cannot be read
     */
    public List<String> findProblems(Path xml) {
        ProblemCollector collector = new ProblemCollector();
        List<String> problems = new ArrayList<>();
        try (InputStream stream = Files.newInputStream(xml)) {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(collector);
            validator.validate(new StreamSource(stream));
        } catch (SAXException e) {
            // a fatal error: the document is not well-formed and cannot be read to the end
            problems.add(e instanceof SAXParseException position ? ProblemCollector.describe(position) : e.getMessage());
        } catch (IOException e) {
            throw new VoucherXmlException("Cannot read " + xml + ": " + e.getMessage(), e);
        }
        problems.addAll(0, collector.problems());
        return problems;
    }

    /** Validates the document and writes the result to the log. */
    public boolean isValid(Path xml) {
        List<String> problems = findProblems(xml);
        if (problems.isEmpty()) {
            LOG.info("{} is valid against {}", xml.getFileName(), xsd.getFileName());
            return true;
        }
        LOG.warn("{} is not valid against {}, {} problem(s):", xml.getFileName(), xsd.getFileName(),
                problems.size());
        problems.forEach(problem -> LOG.warn("  {}", problem));
        return false;
    }
}
