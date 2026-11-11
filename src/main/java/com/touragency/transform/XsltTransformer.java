package com.touragency.transform;

import com.touragency.xml.SafeXml;
import com.touragency.xml.VoucherXmlException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Applies an XSL stylesheet to XML documents. */
public final class XsltTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(XsltTransformer.class);
    private static final String INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";
    private static final String INDENT_SIZE = "4";

    private final Templates templates;
    private final Path xsl;

    /**
     * @param xsl the stylesheet; it is compiled once and can be applied to many documents
     * @throws VoucherXmlException if the stylesheet cannot be read or compiled
     */
    public XsltTransformer(Path xsl) {
        this.xsl = xsl;
        try (InputStream stream = Files.newInputStream(xsl)) {
            this.templates = SafeXml.transformerFactory().newTemplates(new StreamSource(stream));
        } catch (IOException | TransformerException e) {
            throw new VoucherXmlException("Cannot load stylesheet " + xsl + ": " + e.getMessage(), e);
        }
        LOG.debug("Stylesheet {} loaded", xsl);
    }

    /**
     * @param xml    the document to transform
     * @param output where to write the result; directories are created when needed
     * @throws VoucherXmlException if the document cannot be read or transformed or the result cannot be written
     */
    public void transform(Path xml, Path output) {
        Path target = output.toAbsolutePath();
        try (InputStream source = Files.newInputStream(xml)) {
            Files.createDirectories(target.getParent());
            Transformer transformer = templates.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(INDENT_AMOUNT, INDENT_SIZE);
            try (OutputStream result = Files.newOutputStream(target)) {
                transformer.transform(new StreamSource(source), new StreamResult(result));
            }
        } catch (IOException | TransformerException e) {
            LOG.error("Cannot transform {}: {}", xml, e.toString());
            throw new VoucherXmlException("Cannot transform " + xml + " with " + xsl + ": " + e.getMessage(), e);
        }
        LOG.info("{} transformed with {}, result saved to {}", xml.getFileName(), xsl.getFileName(), target);
    }
}
