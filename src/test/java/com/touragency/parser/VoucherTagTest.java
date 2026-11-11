package com.touragency.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VoucherTagTest {

    @ParameterizedTest
    @EnumSource(VoucherTag.class)
    void tagIsFoundByItsXmlName(VoucherTag tag) {
        assertEquals(Optional.of(tag), VoucherTag.find(tag.xmlName()));
    }

    @Test
    void unknownElementIsNotFound() {
        assertTrue(VoucherTag.find("Guide").isEmpty());
        assertTrue(VoucherTag.find("voucher").isEmpty());
    }

    @Test
    void namesAreTheSameAsInTheSchema() {
        assertEquals("TouristVoucher", VoucherTag.TOURIST_VOUCHER.xmlName());
        assertEquals("Voucher", VoucherTag.VOUCHER.xmlName());
        assertEquals("NumberDaysNights", VoucherTag.NUMBER_DAYS_NIGHTS.xmlName());
        assertEquals("HotelCharacteristic", VoucherTag.HOTEL_CHARACTERISTIC.xmlName());
        assertEquals("RoomType", VoucherTag.ROOM_TYPE.xmlName());
        assertEquals("id", VoucherTag.Attribute.ID);
        assertEquals("days", VoucherTag.Attribute.DAYS);
        assertEquals("nights", VoucherTag.Attribute.NIGHTS);
        assertEquals("currency", VoucherTag.Attribute.CURRENCY);
        assertEquals("price", VoucherTag.Attribute.PRICE);
    }

    @Test
    void groupingElementsHoldNoData() {
        assertFalse(VoucherTag.TOURIST_VOUCHER.isBound());
        assertFalse(VoucherTag.HOTEL_CHARACTERISTIC.isBound());
        assertEquals(VoucherTag.values().length - 2, VoucherTag.bound().size());
        assertFalse(VoucherTag.bound().contains(VoucherTag.TOURIST_VOUCHER));
        assertFalse(VoucherTag.bound().contains(VoucherTag.HOTEL_CHARACTERISTIC));
    }

    @Test
    void bindingGroupingElementDoesNothing() {
        VoucherBuilder builder = new VoucherBuilder();

        assertDoesNotThrow(() -> VoucherTag.HOTEL_CHARACTERISTIC.bind(builder, Map.of(), "ignored"));
    }

    @Test
    void everyBoundTagFillsItsPartOfTheVoucher() {
        VoucherBuilder builder = new VoucherBuilder();

        VoucherTag.VOUCHER.bind(builder, Map.of("id", "v-100"), "");
        VoucherTag.TYPE.bind(builder, Map.of(), "rest");
        VoucherTag.COUNTRY.bind(builder, Map.of(), "Spain");
        VoucherTag.NUMBER_DAYS_NIGHTS.bind(builder, Map.of("days", "5", "nights", "4"), "");
        VoucherTag.TRANSPORT.bind(builder, Map.of(), "air");
        VoucherTag.STARS.bind(builder, Map.of(), "4");
        VoucherTag.MEAL.bind(builder, Map.of(), "HB");
        VoucherTag.ROOM_TYPE.bind(builder, Map.of(), "2");
        VoucherTag.TV.bind(builder, Map.of(), "true");
        VoucherTag.AIR_CONDITIONER.bind(builder, Map.of(), "false");
        VoucherTag.COST.bind(builder, Map.of("currency", "EUR"), "");
        VoucherTag.TOTAL.bind(builder, Map.of(), "700.00");
        VoucherTag.SERVICE.bind(builder, Map.of("price", "700.00"), "package");

        assertEquals("Voucher v-100: REST in Spain, 5 days / 4 nights, by AIR, "
                + "4* hotel, meal HB, room for 2, TV yes, air conditioner no, "
                + "cost 700.00 EUR [package (700.00)]", builder.build().toString());
    }

    @Test
    void boundTagsFollowTheOrderOfTheSchema() {
        List<VoucherTag> bound = VoucherTag.bound();

        assertEquals(VoucherTag.VOUCHER, bound.get(0));
        assertEquals(VoucherTag.SERVICE, bound.get(bound.size() - 1));
    }
}
