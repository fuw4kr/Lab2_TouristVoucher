package com.touragency.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.SampleFiles;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class SafeXmlTest {

    /** A document that tries to read a local file through an external entity. */
    private static final String XXE = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE TouristVoucher [<!ENTITY file SYSTEM \"file:///etc/hosts\">]>"
            + "<TouristVoucher>&file;</TouristVoucher>";

    private static InputStream xxe() {
        return new ByteArrayInputStream(XXE.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void documentBuilderIsNamespaceAwareAndRefusesDoctype() throws Exception {
        DocumentBuilder builder = SafeXml.documentBuilder();

        assertTrue(builder.isNamespaceAware());
        assertThrows(SAXException.class, () -> builder.parse(xxe()));
    }

    @Test
    void documentBuilderReadsOrdinaryDocuments() throws Exception {
        assertEquals("TouristVoucher",
                SafeXml.documentBuilder().parse(SampleFiles.MAIN_XML.toFile()).getDocumentElement().getTagName());
    }

    @Test
    void saxParserIsNamespaceAwareAndRefusesDoctype() throws Exception {
        SAXParser parser = SafeXml.saxParser();

        assertTrue(parser.isNamespaceAware());
        assertThrows(SAXException.class, () -> parser.parse(xxe(), new DefaultHandler()));
    }

    @Test
    void staxFactoryHasDtdSwitchedOff() {
        XMLInputFactory factory = SafeXml.staxFactory();

        assertEquals(Boolean.TRUE, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(Boolean.TRUE, factory.getProperty(XMLInputFactory.IS_COALESCING));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    void schemaFactoryDoesNotLoadExternalSchemas() throws Exception {
        SchemaFactory factory = SafeXml.schemaFactory();
        String schemaWithInclude = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "<xs:include schemaLocation=\"http://example.invalid/types.xsd\"/></xs:schema>";

        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertNotNull(factory.newSchema(SampleFiles.XSD.toFile()));
        assertThrows(SAXException.class,
                () -> factory.newSchema(new StreamSource(new StringReader(schemaWithInclude))));
    }

    @Test
    void transformerFactoryDoesNotLoadExternalResources() throws Exception {
        TransformerFactory factory = SafeXml.transformerFactory();

        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
        assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
        assertNotNull(factory.newTransformer());
    }

    @Test
    void exceptionKeepsMessageAndCause() {
        IOException cause = new IOException("no such file");

        VoucherXmlException exception = new VoucherXmlException("Cannot read", cause);

        assertEquals("Cannot read", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
