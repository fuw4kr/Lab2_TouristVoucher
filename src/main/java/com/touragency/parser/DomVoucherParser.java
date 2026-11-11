package com.touragency.parser;

import com.touragency.model.Voucher;
import com.touragency.xml.SafeXml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Reads vouchers with the DOM API: the document is loaded into memory as a tree. */
public final class DomVoucherParser implements VoucherStreamParser {

    @Override
    public List<Voucher> parse(InputStream xml) throws IOException, SAXException, ParserConfigurationException {
        Document document = SafeXml.documentBuilder().parse(xml);
        document.getDocumentElement().normalize();

        List<Voucher> vouchers = new ArrayList<>();
        for (Element voucherElement : elements(document.getDocumentElement(), VoucherTag.VOUCHER)) {
            vouchers.add(toVoucher(voucherElement));
        }
        return vouchers;
    }

    private static Voucher toVoucher(Element voucherElement) {
        VoucherBuilder builder = new VoucherBuilder();
        bind(VoucherTag.VOUCHER, voucherElement, builder);
        for (VoucherTag tag : VoucherTag.bound()) {
            for (Element element : elements(voucherElement, tag)) {
                bind(tag, element, builder);
            }
        }
        return builder.build();
    }

    private static void bind(VoucherTag tag, Element element, VoucherBuilder builder) {
        tag.bind(builder, attributes(element), element.getTextContent());
    }

    /** @return descendants of the parent that are elements of the given kind */
    private static List<Element> elements(Element parent, VoucherTag tag) {
        NodeList nodes = parent.getElementsByTagName(tag.xmlName());
        List<Element> elements = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add((Element) nodes.item(i));
        }
        return elements;
    }

    private static Map<String, String> attributes(Element element) {
        NamedNodeMap nodes = element.getAttributes();
        Map<String, String> attributes = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node attribute = nodes.item(i);
            attributes.put(attribute.getLocalName(), attribute.getNodeValue());
        }
        return attributes;
    }
}
