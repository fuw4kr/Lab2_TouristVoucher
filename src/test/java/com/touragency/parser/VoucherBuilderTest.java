package com.touragency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.Samples;
import com.touragency.model.Voucher;
import java.math.BigDecimal;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class VoucherBuilderTest {

    /** Fills the builder with the data of voucher v-101, skipping one step if asked. */
    private static VoucherBuilder greece(String skipped) {
        VoucherBuilder builder = new VoucherBuilder();
        step("id", skipped, () -> builder.id("v-101"));
        step("type", skipped, () -> builder.type("rest"));
        step("country", skipped, () -> builder.country("Greece"));
        step("duration", skipped, () -> builder.duration("8", "7"));
        step("transport", skipped, () -> builder.transport("air"));
        step("stars", skipped, () -> builder.stars("4"));
        step("meal", skipped, () -> builder.meal("AI"));
        step("roomPlaces", skipped, () -> builder.roomPlaces("2"));
        step("tv", skipped, () -> builder.tv("true"));
        step("airConditioner", skipped, () -> builder.airConditioner("true"));
        step("currency", skipped, () -> builder.currency("EUR"));
        step("total", skipped, () -> builder.total("1250.00"));
        step("services", skipped, () -> {
            builder.service("flight", "420.00");
            builder.service("hotel", "760.00");
            builder.service("transfer", "70.00");
        });
        return builder;
    }

    private static void step(String name, String skipped, Runnable action) {
        if (!name.equals(skipped)) {
            action.run();
        }
    }

    private static void assertRejected(Consumer<VoucherBuilder> call) {
        assertThrows(IllegalArgumentException.class, () -> call.accept(new VoucherBuilder()));
    }

    @Test
    void buildsVoucherFromStrings() {
        assertEquals(Samples.greece(), greece(null).build());
    }

    @Test
    void ignoresWhitespaceAroundValues() {
        VoucherBuilder builder = greece(null);
        builder.id("  v-101 ");
        builder.type("\n   rest\n ");
        builder.country(" Greece\t");
        builder.duration(" 8 ", " 7 ");
        builder.stars(" 4 ");
        builder.total(" 1250.00 ");

        assertEquals(Samples.greece(), builder.build());
    }

    @ParameterizedTest
    @CsvSource({"true, true", "1, true", "false, false", "0, false"})
    void readsBooleansInAllFormsAllowedByXsd(String text, boolean expected) {
        VoucherBuilder builder = greece(null);
        builder.tv(text);
        builder.airConditioner(text);

        Voucher voucher = builder.build();

        assertEquals(expected, voucher.getHotel().tv());
        assertEquals(expected, voucher.getHotel().airConditioner());
    }

    @Test
    void keepsMoneyExactlyAsWritten() {
        Voucher voucher = greece(null).build();

        assertEquals(new BigDecimal("1250.00"), voucher.getCost().total());
        assertEquals(new BigDecimal("1250.00"), voucher.getCost().servicesTotal());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void rejectsEmptyValues(String empty) {
        assertRejected(builder -> builder.id(empty));
        assertRejected(builder -> builder.country(empty));
        assertRejected(builder -> builder.type(empty));
        assertRejected(builder -> builder.stars(empty));
        assertRejected(builder -> builder.tv(empty));
        assertRejected(builder -> builder.total(empty));
        assertRejected(builder -> builder.duration(empty, "1"));
        assertRejected(builder -> builder.duration("1", empty));
        assertRejected(builder -> builder.currency(empty));
        assertRejected(builder -> builder.service(empty, "1"));
        assertRejected(builder -> builder.service("bus", empty));
    }

    @Test
    void rejectsValuesOfWrongType() {
        assertRejected(builder -> builder.type("business"));
        assertRejected(builder -> builder.transport("bicycle"));
        assertRejected(builder -> builder.meal("lunch"));
        assertRejected(builder -> builder.currency("GBP"));
        assertRejected(builder -> builder.stars("four"));
        assertRejected(builder -> builder.roomPlaces("2.5"));
        assertRejected(builder -> builder.tv("yes"));
        assertRejected(builder -> builder.airConditioner("TRUE"));
        assertRejected(builder -> builder.total("a lot"));
        assertRejected(builder -> builder.service("bus", "cheap"));
        assertRejected(builder -> builder.duration("week", "6"));
    }

    @Test
    void rejectsValuesThatBreakTheRulesOfTheModel() {
        assertRejected(builder -> builder.duration("3", "5"));
        assertRejected(builder -> builder.service("bus", "-10"));
        assertThrows(IllegalArgumentException.class, () -> {
            VoucherBuilder builder = greece(null);
            builder.stars("9");
            builder.build();
        });
        assertThrows(IllegalArgumentException.class, () -> greece("services").build());
    }

    @ParameterizedTest
    @CsvSource({
            "id, Voucher", "type, Type", "country, Country", "duration, NumberDaysNights",
            "transport, Transport", "stars, Stars", "meal, Meal", "roomPlaces, RoomType",
            "tv, Tv", "airConditioner, AirConditioner", "currency, Cost", "total, Total"})
    void rejectsVoucherWithoutMandatoryPart(String skipped, String element) {
        VoucherBuilder builder = greece(skipped);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);

        assertTrue(e.getMessage().contains("<" + element + ">"), e.getMessage());
    }
}
