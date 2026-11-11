package com.touragency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.sorting.VoucherOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AppOptionsTest {

    @Test
    void withoutArgumentsDefaultsAreUsed() {
        assertEquals(AppOptions.defaults(), AppOptions.parse());
    }

    @Test
    void defaultFilesAreTheFilesOfTheProject() {
        AppOptions options = AppOptions.defaults();

        assertTrue(Files.isRegularFile(options.xml()), options.xml().toString());
        assertTrue(Files.isRegularFile(options.xsd()), options.xsd().toString());
        assertTrue(Files.isRegularFile(options.xsl()), options.xsl().toString());
        assertEquals(Path.of("output", "vouchers-by-type.xml"), options.output());
        assertEquals(VoucherOrder.COUNTRY, options.order());
    }

    @Test
    void everyOptionCanBeGiven() {
        AppOptions options = AppOptions.parse(
                "--xml=a.xml", "--xsd=b.xsd", "--xsl=c.xsl", "--out=result/d.xml", "--sort=cost");

        assertEquals(new AppOptions(Path.of("a.xml"), Path.of("b.xsd"), Path.of("c.xsl"),
                Path.of("result", "d.xml"), VoucherOrder.COST), options);
    }

    @Test
    void optionsThatAreNotGivenKeepDefaults() {
        AppOptions options = AppOptions.parse("--sort=STARS");

        assertEquals(VoucherOrder.STARS, options.order());
        assertEquals(AppOptions.defaults().xml(), options.xml());
        assertEquals(AppOptions.defaults().output(), options.output());
    }

    @Test
    void theLastValueOfRepeatedOptionWins() {
        assertEquals(Path.of("second.xml"), AppOptions.parse("--xml=first.xml", "--xml=second.xml").xml());
    }

    @Test
    void valueMayContainEqualsSign() {
        assertEquals(Path.of("a=b.xml"), AppOptions.parse("--xml=a=b.xml").xml());
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.xml", "-xml=file.xml", "--xml", "--xml=", "xml=file.xml"})
    void argumentInWrongFormatIsRejected(String argument) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> AppOptions.parse(argument));

        assertTrue(e.getMessage().contains(argument), e.getMessage());
    }

    @Test
    void unknownOptionIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> AppOptions.parse("--verbose=true"));

        assertEquals("Unknown option '--verbose'", e.getMessage());
    }

    @Test
    void unknownSortOrderIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> AppOptions.parse("--sort=weather"));
    }
}
