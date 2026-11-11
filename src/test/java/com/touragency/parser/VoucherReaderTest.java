package com.touragency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.SampleFiles;
import com.touragency.Samples;
import com.touragency.model.Currency;
import com.touragency.model.HotelCharacteristic;
import com.touragency.model.Meal;
import com.touragency.model.StayDuration;
import com.touragency.model.TourType;
import com.touragency.model.Transport;
import com.touragency.model.Voucher;
import com.touragency.xml.VoucherXmlException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Every test runs once for each parser, so all of them are checked against the same expectations. */
class VoucherReaderTest {

    @TempDir
    Path dir;

    private static List<Voucher> read(ParserKind kind, Path xml) {
        return new VoucherReader(kind).read(xml);
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void kindCreatesItsOwnNewParser(ParserKind kind) {
        VoucherStreamParser parser = kind.newParser();

        assertNotSame(parser, kind.newParser());
        assertTrue(parser.getClass().getSimpleName().toUpperCase(Locale.ROOT).startsWith(kind.name()),
                parser.getClass().getSimpleName());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void readsVouchersInTheOrderOfTheDocument(ParserKind kind) {
        List<Voucher> vouchers = read(kind, SampleFiles.SAMPLE_XML);

        assertEquals(List.of("v-101", "v-102", "v-103"), vouchers.stream().map(Voucher::getId).toList());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void readsAllDataOfVoucher(ParserKind kind) {
        assertEquals(Samples.greece(), read(kind, SampleFiles.SAMPLE_XML).get(0));
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void readsTextWithLineBreaksCdataAndNumericBooleans(ParserKind kind) {
        Voucher czech = read(kind, SampleFiles.SAMPLE_XML).get(1);

        assertEquals(TourType.WEEKEND, czech.getType());
        assertEquals("Czech Republic", czech.getCountry());
        assertEquals(new StayDuration(2, 1), czech.getDuration());
        assertEquals(Transport.AUTO, czech.getTransport());
        assertEquals(new HotelCharacteristic(3, Meal.NONE, 1, true, false), czech.getHotel());
        assertEquals(Currency.UAH, czech.getCost().currency());
        assertEquals(2, czech.getCost().services().size());
        assertEquals("bus", czech.getCost().services().get(0).name());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void readsTheMainDocument(ParserKind kind) {
        List<Voucher> vouchers = read(kind, SampleFiles.MAIN_XML);

        assertEquals(8, vouchers.size());
        assertEquals(8, vouchers.stream().map(Voucher::getId).distinct().count());
        for (Voucher voucher : vouchers) {
            assertNotNull(voucher.getType(), voucher.getId());
            assertNotNull(voucher.getTransport(), voucher.getId());
            assertFalse(voucher.getCountry().isBlank(), voucher.getId());
            assertFalse(voucher.getCost().services().isEmpty(), voucher.getId());
            // in the main document the total cost is exactly the sum of what is included
            assertEquals(0, voucher.getCost().total().compareTo(voucher.getCost().servicesTotal()), voucher.getId());
        }
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void allParsersReturnEqualCollections(ParserKind kind) {
        assertEquals(read(ParserKind.values()[0], SampleFiles.MAIN_XML), read(kind, SampleFiles.MAIN_XML));
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void failsIfFileDoesNotExist(ParserKind kind) {
        Path missing = dir.resolve("missing.xml");

        VoucherXmlException e = assertThrows(VoucherXmlException.class, () -> read(kind, missing));

        assertInstanceOf(IOException.class, e.getCause());
        assertTrue(e.getMessage().startsWith(kind.name()), e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void failsIfDocumentIsNotWellFormed(ParserKind kind) {
        VoucherXmlException e = assertThrows(VoucherXmlException.class,
                () -> read(kind, SampleFiles.NOT_WELL_FORMED_XML));

        assertTrue(e.getMessage().contains("vouchers-not-well-formed.xml"), e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void failsIfDataIsWrong(ParserKind kind) {
        assertDataRejected(kind, "<Stars>4</Stars>", "<Stars>four</Stars>");
        assertDataRejected(kind, "<Type>rest</Type>", "<Type>business</Type>");
        assertDataRejected(kind, "<Tv>true</Tv>", "<Tv>maybe</Tv>");
        assertDataRejected(kind, "<Total>700.00</Total>", "");
        assertDataRejected(kind, " id=\"v-100\"", "");
        assertDataRejected(kind, " nights=\"4\"", "");
        assertDataRejected(kind, " nights=\"4\"", " nights=\"40\"");
        assertDataRejected(kind, " currency=\"EUR\"", "");
        assertDataRejected(kind, " price=\"700.00\"", "");
        assertDataRejected(kind, "<Service price=\"700.00\">package</Service>", "");
    }

    private void assertDataRejected(ParserKind kind, String fragment, String replacement) {
        Path xml = SampleFiles.documentWith(dir, fragment, replacement);

        VoucherXmlException e = assertThrows(VoucherXmlException.class, () -> read(kind, xml),
                () -> kind + " accepted a voucher where '" + fragment + "' became '" + replacement + "'");

        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void acceptsValidGeneratedDocument(ParserKind kind) {
        Path xml = SampleFiles.document(dir, SampleFiles.VOUCHER_XML);

        assertEquals("Spain", read(kind, xml).get(0).getCountry());
    }

    @ParameterizedTest
    @EnumSource(ParserKind.class)
    void refusesDocumentWithDoctype(ParserKind kind) throws IOException {
        Path xml = Files.writeString(dir.resolve("xxe.xml"), "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE TouristVoucher [<!ENTITY file SYSTEM \"file:///etc/hosts\">]>"
                + "<TouristVoucher>" + SampleFiles.VOUCHER_XML.replace("Spain", "&file;") + "</TouristVoucher>");

        assertThrows(VoucherXmlException.class, () -> read(kind, xml));
    }

    @Test
    void wrapsExceptionOfTheParserAndKeepsItAsCause() {
        IOException failure = new IOException("disk error");
        VoucherReader reader = new VoucherReader("TEST", stream -> {
            throw failure;
        });

        VoucherXmlException e = assertThrows(VoucherXmlException.class, () -> reader.read(SampleFiles.MAIN_XML));

        assertSame(failure, e.getCause());
        assertTrue(e.getMessage().contains("TEST parser cannot read"), e.getMessage());
        assertTrue(e.getMessage().contains("disk error"), e.getMessage());
    }
}
