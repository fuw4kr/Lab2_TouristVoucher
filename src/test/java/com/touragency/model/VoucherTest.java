package com.touragency.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.touragency.Samples;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class VoucherTest {

    private static Voucher greeceBut(Consumer<Voucher> change) {
        Voucher voucher = Samples.greece();
        change.accept(voucher);
        return voucher;
    }

    @Test
    void newVoucherHasNoData() {
        Voucher voucher = new Voucher();

        assertNull(voucher.getId());
        assertNull(voucher.getType());
        assertNull(voucher.getCountry());
        assertNull(voucher.getDuration());
        assertNull(voucher.getTransport());
        assertNull(voucher.getHotel());
        assertNull(voucher.getCost());
    }

    @Test
    void gettersReturnWhatSettersStored() {
        Voucher voucher = Samples.greece();

        assertEquals("v-101", voucher.getId());
        assertEquals(TourType.REST, voucher.getType());
        assertEquals("Greece", voucher.getCountry());
        assertEquals(new StayDuration(8, 7), voucher.getDuration());
        assertEquals(Transport.AIR, voucher.getTransport());
        assertEquals(new HotelCharacteristic(4, Meal.AI, 2, true, true), voucher.getHotel());
        assertEquals(Currency.EUR, voucher.getCost().currency());
        assertEquals(new BigDecimal("1250.00"), voucher.getCost().total());
    }

    @Test
    void vouchersWithTheSameDataAreEqual() {
        Voucher voucher = Samples.greece();

        assertEquals(voucher, voucher);
        assertEquals(voucher, Samples.greece());
        assertEquals(voucher.hashCode(), Samples.greece().hashCode());
    }

    @Test
    void vouchersAreDifferentIfAnyFieldDiffers() {
        Voucher voucher = Samples.greece();
        Cost otherCost = new Cost(Currency.USD, BigDecimal.TEN,
                List.of(new IncludedService("bus", BigDecimal.TEN)));

        assertNotEquals(voucher, greeceBut(v -> v.setId("v-999")));
        assertNotEquals(voucher, greeceBut(v -> v.setType(TourType.WEEKEND)));
        assertNotEquals(voucher, greeceBut(v -> v.setCountry("Italy")));
        assertNotEquals(voucher, greeceBut(v -> v.setDuration(new StayDuration(2, 1))));
        assertNotEquals(voucher, greeceBut(v -> v.setTransport(Transport.LINER)));
        assertNotEquals(voucher, greeceBut(v -> v.setHotel(new HotelCharacteristic(1, Meal.NONE, 1, false, false))));
        assertNotEquals(voucher, greeceBut(v -> v.setCost(otherCost)));
        assertNotEquals(voucher, null);
        assertNotEquals(voucher, "v-101");
    }

    @Test
    void isPrintedOnOneLine() {
        assertEquals("Voucher v-101: REST in Greece, 8 days / 7 nights, by AIR, "
                + "4* hotel, meal AI, room for 2, TV yes, air conditioner yes, "
                + "cost 1250.00 EUR [flight (420.00), hotel (760.00), transfer (70.00)]",
                Samples.greece().toString());
    }
}
