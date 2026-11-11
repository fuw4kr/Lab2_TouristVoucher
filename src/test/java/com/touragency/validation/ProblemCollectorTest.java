package com.touragency.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

class ProblemCollectorTest {

    private final ProblemCollector collector = new ProblemCollector();

    private static SAXParseException at(int line, int column, String message) {
        return new SAXParseException(message, "public", "system", line, column);
    }

    @Test
    void descriptionStartsWithThePosition() {
        assertEquals("[12:34] wrong value", ProblemCollector.describe(at(12, 34, "wrong value")));
    }

    @Test
    void remembersAllErrors() {
        collector.error(at(1, 1, "first"));
        collector.error(at(2, 5, "second"));

        assertEquals(List.of("[1:1] first", "[2:5] second"), collector.problems());
    }

    @Test
    void warningIsOnlyLogged() {
        collector.warning(at(1, 1, "not important"));

        assertTrue(collector.problems().isEmpty());
    }

    @Test
    void fatalErrorStopsTheValidation() {
        SAXParseException fatal = at(3, 3, "document is broken");

        assertSame(fatal, assertThrows(SAXParseException.class, () -> collector.fatalError(fatal)));
        assertTrue(collector.problems().isEmpty());
    }
}
