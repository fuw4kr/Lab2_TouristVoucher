package com.touragency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.model.Voucher;
import com.touragency.parser.ParserKind;
import com.touragency.sorting.VoucherOrder;
import com.touragency.xml.VoucherXmlException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TouristVoucherAppTest {

    @TempDir
    Path dir;

    private Path output;

    @BeforeEach
    void setUp() {
        output = dir.resolve("grouped.xml");
    }

    private AppOptions options(Path xml, VoucherOrder order) {
        return new AppOptions(xml, SampleFiles.XSD, SampleFiles.XSL, output, order);
    }

    private String[] arguments(Path xml, String... more) {
        String[] arguments = new String[4 + more.length];
        arguments[0] = "--xml=" + xml;
        arguments[1] = "--xsd=" + SampleFiles.XSD;
        arguments[2] = "--xsl=" + SampleFiles.XSL;
        arguments[3] = "--out=" + output;
        System.arraycopy(more, 0, arguments, 4, more.length);
        return arguments;
    }

    @Test
    void validDocumentIsReadAndTransformed() {
        assertTrue(new TouristVoucherApp(options(SampleFiles.MAIN_XML, VoucherOrder.COUNTRY)).run());
        assertTrue(Files.isRegularFile(output));
    }

    @Test
    void invalidDocumentIsNotProcessed() {
        assertFalse(new TouristVoucherApp(options(SampleFiles.SCHEMA_ERRORS_XML, VoucherOrder.COUNTRY)).run());
        assertFalse(Files.exists(output));
    }

    @Test
    void missingDocumentIsAnError() {
        TouristVoucherApp app = new TouristVoucherApp(options(dir.resolve("missing.xml"), VoucherOrder.COUNTRY));

        assertThrows(VoucherXmlException.class, app::run);
    }

    @Test
    void everyParserGivesVouchersInTheChosenOrder() {
        TouristVoucherApp app = new TouristVoucherApp(options(SampleFiles.SAMPLE_XML, VoucherOrder.DURATION));

        Map<ParserKind, List<Voucher>> results = app.readWithAllParsers();

        assertEquals(ParserKind.values().length, results.size());
        for (List<Voucher> vouchers : results.values()) {
            assertEquals(List.of("v-102", "v-103", "v-101"), vouchers.stream().map(Voucher::getId).toList());
        }
    }

    @Test
    void launchSucceedsWithCorrectArguments() {
        assertTrue(TouristVoucherApp.launch(arguments(SampleFiles.MAIN_XML, "--sort=cost")));
        assertTrue(Files.isRegularFile(output));
    }

    @Test
    void launchReportsInvalidDocument() {
        assertFalse(TouristVoucherApp.launch(arguments(SampleFiles.SCHEMA_ERRORS_XML)));
    }

    @Test
    void launchReportsWrongArguments() {
        assertFalse(TouristVoucherApp.launch("--sort=weather"));
        assertFalse(TouristVoucherApp.launch("something"));
    }

    @Test
    void launchReportsMissingFiles() {
        assertFalse(TouristVoucherApp.launch(arguments(dir.resolve("missing.xml"))));
        assertFalse(TouristVoucherApp.launch(arguments(SampleFiles.MAIN_XML, "--xsd=" + dir.resolve("missing.xsd"))));
        assertFalse(TouristVoucherApp.launch(arguments(SampleFiles.MAIN_XML, "--xsl=" + dir.resolve("missing.xsl"))));
    }

    @Test
    void mainNeverThrows() {
        assertDoesNotThrow(() -> TouristVoucherApp.main(arguments(SampleFiles.SAMPLE_XML)));
        assertDoesNotThrow(() -> TouristVoucherApp.main(new String[] {"wrong"}));
    }
}
