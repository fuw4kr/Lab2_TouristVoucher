package com.touragency.validation;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Error handler that remembers every violation of the schema, so the validator
 * can report all of them at once and not only the first one.
 */
final class ProblemCollector implements ErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemCollector.class);

    private final List<String> problems = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) {
        LOG.warn("Validation warning at {}", describe(exception));
    }

    @Override
    public void error(SAXParseException exception) {
        problems.add(describe(exception));
    }

    /** After a fatal error the document cannot be read any further, so validation stops. */
    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
    }

    List<String> problems() {
        return problems;
    }

    static String describe(SAXParseException exception) {
        return "[" + exception.getLineNumber() + ":" + exception.getColumnNumber() + "] "
                + exception.getMessage();
    }
}
