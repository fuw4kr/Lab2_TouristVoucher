package com.touragency.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.SampleFiles;
import com.touragency.xml.VoucherXmlException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class XsdValidatorTest {

    private final XsdValidator validator = new XsdValidator(SampleFiles.XSD);

    @TempDir
    Path dir;

    private boolean validWith(String fragment, String replacement) {
        return validator.findProblems(SampleFiles.documentWith(dir, fragment, replacement)).isEmpty();
    }

    @Test
    void mainDocumentIsValid() {
        assertEquals(List.of(), validator.findProblems(SampleFiles.MAIN_XML));
        assertTrue(validator.isValid(SampleFiles.MAIN_XML));
    }

    @Test
    void sampleDocumentIsValid() {
        assertEquals(List.of(), validator.findProblems(SampleFiles.SAMPLE_XML));
    }

    @Test
    void generatedVoucherIsValid() {
        assertEquals(List.of(), validator.findProblems(SampleFiles.document(dir, SampleFiles.VOUCHER_XML)));
    }

    @Test
    void findsAllViolationsOfTheSchema() {
        List<String> problems = validator.findProblems(SampleFiles.SCHEMA_ERRORS_XML);
        String all = String.join("\n", problems);

        assertTrue(problems.size() >= 5, all);
        assertTrue(all.contains("business"), all);
        assertTrue(all.contains("nights"), all);
        assertTrue(all.contains("'6'"), all);
        assertTrue(all.contains("GBP"), all);
        assertTrue(all.contains("v-001"), all);
        assertTrue(all.contains("Service"), all);
        assertTrue(problems.stream().allMatch(problem -> problem.matches("(?s)\\[\\d+:\\d+] .+")), all);
        assertFalse(validator.isValid(SampleFiles.SCHEMA_ERRORS_XML));
    }

    @Test
    void documentThatIsNotWellFormedHasOneProblem() {
        List<String> problems = validator.findProblems(SampleFiles.NOT_WELL_FORMED_XML);

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("["), problems.get(0));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "days=\"5\" nights=\"4\"  | days=\"1\" nights=\"0\"",
            "days=\"5\" nights=\"4\"  | days=\"90\" nights=\"90\"",
            "<Stars>4</Stars>         | <Stars>1</Stars>",
            "<Stars>4</Stars>         | <Stars>5</Stars>",
            "<RoomType>2</RoomType>   | <RoomType>1</RoomType>",
            "<RoomType>2</RoomType>   | <RoomType>3</RoomType>",
            "<Total>700.00</Total>    | <Total>0.01</Total>",
            "price=\"700.00\"         | price=\"0\"",
            "id=\"v-100\"             | id=\"v-999\"",
            "<Country>Spain</Country> | <Country>Cote d'Ivoire</Country>"})
    void acceptsValuesOnTheBorders(String fragment, String replacement) {
        assertTrue(validWith(fragment, replacement));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "days=\"5\"               | days=\"0\"",
            "days=\"5\"               | days=\"91\"",
            "nights=\"4\"             | nights=\"-1\"",
            "nights=\"4\"             | nights=\"91\"",
            "<Stars>4</Stars>         | <Stars>0</Stars>",
            "<Stars>4</Stars>         | <Stars>6</Stars>",
            "<RoomType>2</RoomType>   | <RoomType>0</RoomType>",
            "<RoomType>2</RoomType>   | <RoomType>4</RoomType>",
            "<Total>700.00</Total>    | <Total>0</Total>",
            "<Total>700.00</Total>    | <Total>700.005</Total>",
            "price=\"700.00\"         | price=\"-1\"",
            "id=\"v-100\"             | id=\"100\"",
            "id=\"v-100\"             | id=\"v-1000\"",
            "<Country>Spain</Country> | <Country>spain</Country>",
            "<Country>Spain</Country> | <Country>S</Country>",
            "<Meal>HB</Meal>          | <Meal>FB</Meal>",
            "<Transport>air</Transport> | <Transport>Air</Transport>",
            "<Tv>true</Tv>            | <Tv>yes</Tv>"})
    void rejectsValuesOutsideTheBorders(String fragment, String replacement) {
        assertFalse(validWith(fragment, replacement));
    }

    @Test
    void rejectsDocumentWithoutVouchers() {
        assertFalse(validator.isValid(SampleFiles.document(dir, "")));
    }

    @Test
    void failsIfDocumentDoesNotExist() {
        Path missing = dir.resolve("missing.xml");

        assertThrows(VoucherXmlException.class, () -> validator.findProblems(missing));
    }

    @Test
    void failsIfSchemaDoesNotExist() {
        Path missing = dir.resolve("missing.xsd");

        VoucherXmlException e = assertThrows(VoucherXmlException.class, () -> new XsdValidator(missing));

        assertTrue(e.getMessage().contains("missing.xsd"), e.getMessage());
    }

    @Test
    void failsIfSchemaIsBroken() throws IOException {
        Path broken = Files.writeString(dir.resolve("broken.xsd"), "<xs:schema");

        assertThrows(VoucherXmlException.class, () -> new XsdValidator(broken));
    }
}
