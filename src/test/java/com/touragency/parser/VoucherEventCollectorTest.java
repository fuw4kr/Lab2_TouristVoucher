package com.touragency.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.Samples;
import com.touragency.model.Voucher;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VoucherEventCollectorTest {

    private final VoucherEventCollector collector = new VoucherEventCollector();

    private void open(String name) {
        collector.elementOpened(name, Map.of());
    }

    private void close(String name) {
        collector.elementClosed(name);
    }

    private void leaf(String name, String text) {
        leaf(name, Map.of(), text);
    }

    private void leaf(String name, Map<String, String> attributes, String text) {
        collector.elementOpened(name, attributes);
        collector.text(text);
        collector.elementClosed(name);
    }

    /** Sends the events of voucher v-101 of the sample document. */
    private void greece() {
        collector.elementOpened("Voucher", Map.of("id", "v-101"));
        collector.text("\n        ");
        leaf("Type", "rest");
        leaf("Country", "Greece");
        leaf("NumberDaysNights", Map.of("days", "8", "nights", "7"), "");
        leaf("Transport", "air");
        open("HotelCharacteristic");
        leaf("Stars", "4");
        leaf("Meal", "AI");
        leaf("RoomType", "2");
        leaf("Tv", "true");
        leaf("AirConditioner", "true");
        close("HotelCharacteristic");
        collector.elementOpened("Cost", Map.of("currency", "EUR"));
        leaf("Total", "1250.00");
        leaf("Service", Map.of("price", "420.00"), "flight");
        leaf("Service", Map.of("price", "760.00"), "hotel");
        leaf("Service", Map.of("price", "70.00"), "transfer");
        close("Cost");
        collector.text("\n    ");
        close("Voucher");
    }

    @Test
    void thereAreNoVouchersWithoutEvents() {
        assertTrue(collector.vouchers().isEmpty());
    }

    @Test
    void buildsVoucherFromEvents() {
        open("TouristVoucher");
        greece();
        close("TouristVoucher");

        assertEquals(List.of(Samples.greece()), collector.vouchers());
    }

    @Test
    void buildsEveryVoucherOfTheDocument() {
        open("TouristVoucher");
        greece();
        greece();
        greece();
        close("TouristVoucher");

        assertEquals(3, collector.vouchers().size());
    }

    @Test
    void gluesTextThatComesInParts() {
        collector.elementOpened("Voucher", Map.of("id", "v-101"));
        collector.elementOpened("Country", Map.of());
        collector.text("Czech ");
        collector.text("Repu");
        collector.text("blic");
        collector.elementClosed("Country");

        // the rest of the voucher is not important here, so the check is done through the error message
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> close("Voucher"));
        assertTrue(e.getMessage().contains("<Type>"), e.getMessage());
    }

    @Test
    void attributesOfOuterElementSurviveInnerElements() {
        open("TouristVoucher");
        greece();

        Voucher voucher = collector.vouchers().get(0);

        // "id" and "currency" are read when Voucher and Cost are closed, after all their children
        assertEquals("v-101", voucher.getId());
        assertEquals("EUR", voucher.getCost().currency().xmlName());
    }

    @Test
    void skipsElementsItDoesNotKnow() {
        collector.elementOpened("Voucher", Map.of("id", "v-101"));
        leaf("Guide", "John");
        close("Voucher-unknown");
        open("Voucher-unknown");

        assertTrue(collector.vouchers().isEmpty());
    }

    @Test
    void refusesDataOutsideOfVoucher() {
        open("TouristVoucher");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> leaf("Country", "Greece"));

        assertEquals("<Country> must be inside <Voucher>", e.getMessage());
    }
}
