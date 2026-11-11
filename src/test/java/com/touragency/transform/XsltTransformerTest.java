package com.touragency.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.SampleFiles;
import com.touragency.xml.SafeXml;
import com.touragency.xml.VoucherXmlException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class XsltTransformerTest {

    private final XsltTransformer transformer = new XsltTransformer(SampleFiles.XSL);

    @TempDir
    Path dir;

    /** Transforms the document and returns the root element of the result. */
    private Element transformed(Path xml) throws Exception {
        Path output = dir.resolve("result.xml");
        transformer.transform(xml, output);
        return SafeXml.documentBuilder().parse(output.toFile()).getDocumentElement();
    }

    private static List<Element> children(Element parent) {
        List<Element> children = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) node);
            }
        }
        return children;
    }

    private static String textOf(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent();
    }

    @Test
    void splitsVouchersIntoSubListsByType() throws Exception {
        Element root = transformed(SampleFiles.MAIN_XML);
        List<Element> groups = children(root);

        assertEquals("TouristVoucher", root.getTagName());
        assertEquals(List.of("excursion", "pilgrimage", "rest", "weekend"),
                groups.stream().map(Element::getTagName).toList());
        for (Element group : groups) {
            List<Element> vouchers = children(group);
            assertEquals(2, vouchers.size(), group.getTagName());
            assertEquals("2", group.getAttribute("vouchers"));
            for (Element voucher : vouchers) {
                assertEquals("Voucher", voucher.getTagName());
                assertEquals(group.getTagName(), textOf(voucher, "Type"));
            }
        }
    }

    @Test
    void noVoucherIsLostOrDuplicated() throws Exception {
        Element root = transformed(SampleFiles.MAIN_XML);
        NodeList vouchers = root.getElementsByTagName("Voucher");
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < vouchers.getLength(); i++) {
            ids.add(((Element) vouchers.item(i)).getAttribute("id"));
        }

        assertEquals(8, ids.size());
        assertEquals(8, ids.stream().distinct().count());
    }

    @Test
    void sortsVouchersOfGroupByCountry() throws Exception {
        Element excursions = children(transformed(SampleFiles.MAIN_XML)).get(0);

        assertEquals(List.of("Czech Republic", "Italy"),
                children(excursions).stream().map(voucher -> textOf(voucher, "Country")).toList());
    }

    @Test
    void copiesVoucherWithAllItsContent() throws Exception {
        Element root = transformed(SampleFiles.SAMPLE_XML);
        Element rest = (Element) root.getElementsByTagName("rest").item(0);
        Element greece = children(rest).get(0);

        assertEquals("1", rest.getAttribute("vouchers"));
        assertEquals("v-101", greece.getAttribute("id"));
        assertEquals(1, greece.getAttributes().getLength());
        assertEquals("Greece", textOf(greece, "Country"));
        assertEquals("4", textOf(greece, "Stars"));
        assertEquals("1250.00", textOf(greece, "Total"));
        Element duration = (Element) greece.getElementsByTagName("NumberDaysNights").item(0);
        assertEquals("8", duration.getAttribute("days"));
        assertEquals("7", duration.getAttribute("nights"));
        assertEquals("EUR", ((Element) greece.getElementsByTagName("Cost").item(0)).getAttribute("currency"));
        NodeList services = greece.getElementsByTagName("Service");
        assertEquals(3, services.getLength());
        assertEquals("flight", services.item(0).getTextContent());
        assertEquals("420.00", ((Element) services.item(0)).getAttribute("price"));
    }

    @Test
    void createsDirectoriesForTheResult() {
        Path output = dir.resolve("deep").resolve("deeper").resolve("result.xml");

        transformer.transform(SampleFiles.MAIN_XML, output);

        assertTrue(Files.isRegularFile(output));
    }

    @Test
    void failsIfDocumentDoesNotExist() {
        Path output = dir.resolve("result.xml");

        assertThrows(VoucherXmlException.class, () -> transformer.transform(dir.resolve("missing.xml"), output));
        assertFalse(Files.exists(output));
    }

    @Test
    void failsIfDocumentIsNotWellFormed() {
        assertThrows(VoucherXmlException.class,
                () -> transformer.transform(SampleFiles.NOT_WELL_FORMED_XML, dir.resolve("result.xml")));
    }

    @Test
    void failsIfStylesheetDoesNotExist() {
        Path missing = dir.resolve("missing.xsl");

        VoucherXmlException e = assertThrows(VoucherXmlException.class, () -> new XsltTransformer(missing));

        assertTrue(e.getMessage().contains("missing.xsl"), e.getMessage());
    }

    @Test
    void failsIfStylesheetIsBroken() throws IOException {
        Path broken = Files.writeString(dir.resolve("broken.xsl"), "<xsl:stylesheet");

        assertThrows(VoucherXmlException.class, () -> new XsltTransformer(broken));
    }
}
