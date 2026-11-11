package com.touragency.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests of the small immutable parts of a voucher. */
class ValueObjectsTest {

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }

    @Nested
    class StayDurationTest {

        @Test
        void keepsDaysAndNights() {
            StayDuration duration = new StayDuration(8, 7);

            assertEquals(8, duration.days());
            assertEquals(7, duration.nights());
            assertEquals("8 days / 7 nights", duration.toString());
        }

        @ParameterizedTest
        @CsvSource({"1, 0", "1, 1", "90, 90", "14, 13"})
        void acceptsNightsUpToTheNumberOfDays(int days, int nights) {
            assertDoesNotThrow(() -> new StayDuration(days, nights));
        }

        @ParameterizedTest
        @CsvSource({"0, 0", "-1, 0", "3, -1", "3, 4"})
        void rejectsImpossibleDurations(int days, int nights) {
            assertThrows(IllegalArgumentException.class, () -> new StayDuration(days, nights));
        }

        @Test
        void durationsWithTheSameValuesAreEqual() {
            assertEquals(new StayDuration(3, 2), new StayDuration(3, 2));
            assertNotEquals(new StayDuration(3, 2), new StayDuration(3, 3));
        }
    }

    @Nested
    class HotelCharacteristicTest {

        @Test
        void keepsAllCharacteristics() {
            HotelCharacteristic hotel = new HotelCharacteristic(4, Meal.HB, 2, true, false);

            assertEquals(4, hotel.stars());
            assertEquals(Meal.HB, hotel.meal());
            assertEquals(2, hotel.roomPlaces());
            assertTrue(hotel.tv());
            assertFalse(hotel.airConditioner());
        }

        @Test
        void isPrintedInReadableForm() {
            assertEquals("4* hotel, meal HB, room for 2, TV yes, air conditioner no",
                    new HotelCharacteristic(4, Meal.HB, 2, true, false).toString());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5})
        void acceptsStarsFromOneToFive(int stars) {
            assertDoesNotThrow(() -> new HotelCharacteristic(stars, Meal.BB, 1, false, false));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 6})
        void rejectsStarsOutOfRange(int stars) {
            assertThrows(IllegalArgumentException.class,
                    () -> new HotelCharacteristic(stars, Meal.BB, 1, false, false));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 4})
        void rejectsRoomPlacesOutOfRange(int places) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> new HotelCharacteristic(3, Meal.BB, places, false, false));

            assertEquals("Room places must be between 1 and 3, but got " + places, e.getMessage());
        }

        @Test
        void rejectsMissingMeal() {
            assertThrows(NullPointerException.class, () -> new HotelCharacteristic(3, null, 1, false, false));
        }
    }

    @Nested
    class IncludedServiceTest {

        @Test
        void keepsNameAndPrice() {
            IncludedService service = new IncludedService("flight", money("420.00"));

            assertEquals("flight", service.name());
            assertEquals(money("420.00"), service.price());
            assertEquals("flight (420.00)", service.toString());
        }

        @Test
        void freeServiceIsAllowed() {
            assertDoesNotThrow(() -> new IncludedService("insurance", BigDecimal.ZERO));
        }

        @Test
        void rejectsWrongValues() {
            assertThrows(IllegalArgumentException.class, () -> new IncludedService("  ", money("1")));
            assertThrows(IllegalArgumentException.class, () -> new IncludedService("flight", money("-0.01")));
            assertThrows(NullPointerException.class, () -> new IncludedService(null, money("1")));
            assertThrows(NullPointerException.class, () -> new IncludedService("flight", null));
        }
    }

    @Nested
    class CostTest {

        private final List<IncludedService> services = List.of(
                new IncludedService("flight", money("420.00")),
                new IncludedService("hotel", money("760.00")));

        @Test
        void keepsCurrencyTotalAndServices() {
            Cost cost = new Cost(Currency.EUR, money("1250.00"), services);

            assertEquals(Currency.EUR, cost.currency());
            assertEquals(money("1250.00"), cost.total());
            assertEquals(services, cost.services());
            assertEquals("1250.00 EUR [flight (420.00), hotel (760.00)]", cost.toString());
        }

        @Test
        void sumsThePricesOfServices() {
            assertEquals(money("1180.00"), new Cost(Currency.EUR, money("1250.00"), services).servicesTotal());
        }

        @Test
        void servicesCannotBeChangedFromOutside() {
            List<IncludedService> source = new ArrayList<>(services);
            Cost cost = new Cost(Currency.EUR, money("1250.00"), source);

            source.clear();

            assertEquals(2, cost.services().size());
            List<IncludedService> copy = cost.services();
            assertThrows(UnsupportedOperationException.class, copy::clear);
        }

        @Test
        void rejectsWrongValues() {
            assertThrows(IllegalArgumentException.class, () -> new Cost(Currency.EUR, BigDecimal.ZERO, services));
            assertThrows(IllegalArgumentException.class, () -> new Cost(Currency.EUR, money("-5"), services));
            assertThrows(IllegalArgumentException.class, () -> new Cost(Currency.EUR, money("5"), List.of()));
            assertThrows(NullPointerException.class, () -> new Cost(null, money("5"), services));
            assertThrows(NullPointerException.class, () -> new Cost(Currency.EUR, null, services));
        }
    }
}
