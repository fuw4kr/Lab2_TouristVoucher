package com.touragency;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/** XML files used by the tests: resources of the project and documents generated on the fly. */
public final class SampleFiles {

    /** The document, the schema and the stylesheet of the application itself. */
    public static final Path MAIN_XML = resource("tourist-voucher.xml");
    public static final Path XSD = resource("tourist-voucher.xsd");

    /** Three valid vouchers: v-101 (Greece), v-102 (Czech Republic), v-103 (Israel). */
    public static final Path SAMPLE_XML = resource("vouchers-sample.xml");
    public static final Path SCHEMA_ERRORS_XML = resource("vouchers-schema-errors.xml");
    public static final Path NOT_WELL_FORMED_XML = resource("vouchers-not-well-formed.xml");

    /** A valid voucher element; tests break it by replacing its parts. */
    public static final String VOUCHER_XML = """
            <Voucher id="v-100">
                <Type>rest</Type>
                <Country>Spain</Country>
                <NumberDaysNights days="5" nights="4"/>
                <Transport>air</Transport>
                <HotelCharacteristic>
                    <Stars>4</Stars>
                    <Meal>HB</Meal>
                    <RoomType>2</RoomType>
                    <Tv>true</Tv>
                    <AirConditioner>true</AirConditioner>
                </HotelCharacteristic>
                <Cost currency="EUR">
                    <Total>700.00</Total>
                    <Service price="700.00">package</Service>
                </Cost>
            </Voucher>
            """;

    private SampleFiles() {
    }

    public static Path resource(String name) {
        URL url = SampleFiles.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new IllegalStateException("No test resource " + name);
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Writes a document with the given content of the root element into the directory. */
    public static Path document(Path directory, String rootContent) {
        try {
            return Files.writeString(directory.resolve("vouchers.xml"),
                    "<TouristVoucher>" + rootContent + "</TouristVoucher>");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Writes a document with {@link #VOUCHER_XML} where one fragment is replaced by another. */
    public static Path documentWith(Path directory, String fragment, String replacement) {
        if (!VOUCHER_XML.contains(fragment)) {
            throw new IllegalArgumentException("The voucher has no fragment " + fragment);
        }
        return document(directory, VOUCHER_XML.replace(fragment, replacement));
    }
}
