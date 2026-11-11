package com.touragency.sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.touragency.SampleFiles;
import com.touragency.Samples;
import com.touragency.model.Currency;
import com.touragency.model.TourType;
import com.touragency.model.Voucher;
import com.touragency.parser.ParserKind;
import com.touragency.parser.VoucherReader;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class VoucherOrderTest {

    private static final Voucher SPAIN = Samples.voucher("v-1", "Spain", TourType.REST, 7, 4, Currency.EUR, "900.00");
    private static final Voucher AUSTRIA = Samples.voucher("v-2", "austria", TourType.WEEKEND, 3, 5, Currency.EUR, "450.00");
    private static final Voucher POLAND = Samples.voucher("v-3", "Poland", TourType.EXCURSION, 3, 3, Currency.UAH, "9000.00");
    private static final Voucher SPAIN_AGAIN = Samples.voucher("v-4", "Spain", TourType.REST, 7, 4, Currency.EUR, "900.00");

    private static final List<Voucher> VOUCHERS = List.of(SPAIN_AGAIN, SPAIN, AUSTRIA, POLAND);

    private static List<String> idsSortedBy(VoucherOrder order) {
        return order.sort(VOUCHERS).stream().map(Voucher::getId).toList();
    }

    @Test
    void byCountryIgnoresCase() {
        assertEquals(List.of("v-2", "v-3", "v-1", "v-4"), idsSortedBy(VoucherOrder.COUNTRY));
    }

    @Test
    void byTypeFollowsTheOrderOfTheEnum() {
        assertEquals(List.of("v-2", "v-3", "v-1", "v-4"), idsSortedBy(VoucherOrder.TYPE));
    }

    @Test
    void byDurationPutsShortTripsFirst() {
        assertEquals(List.of("v-2", "v-3", "v-1", "v-4"), idsSortedBy(VoucherOrder.DURATION));
    }

    @Test
    void byDurationComparesNightsWhenDaysAreEqual() {
        Voucher noNights = Samples.voucher("v-9", "Spain", TourType.REST, 1, 4, Currency.EUR, "900.00");
        Voucher oneNight = Samples.voucher("v-8", "Spain", TourType.REST, 1, 4, Currency.EUR, "900.00");
        oneNight.setDuration(new com.touragency.model.StayDuration(1, 1));

        assertTrue(VoucherOrder.DURATION.comparator().compare(noNights, oneNight) < 0);
    }

    @Test
    void byStarsPutsTheBestHotelFirst() {
        assertEquals(List.of("v-2", "v-1", "v-4", "v-3"), idsSortedBy(VoucherOrder.STARS));
    }

    @Test
    void byCostGroupsByCurrencyAndThenComparesTotals() {
        assertEquals(List.of("v-3", "v-2", "v-1", "v-4"), idsSortedBy(VoucherOrder.COST));
    }

    @ParameterizedTest
    @EnumSource(VoucherOrder.class)
    void equalVouchersAreOrderedById(VoucherOrder order) {
        assertTrue(order.comparator().compare(SPAIN, SPAIN_AGAIN) < 0);
        assertTrue(order.comparator().compare(SPAIN_AGAIN, SPAIN) > 0);
        assertEquals(0, order.comparator().compare(SPAIN, SPAIN));
    }

    @ParameterizedTest
    @EnumSource(VoucherOrder.class)
    void sortReturnsNewListAndKeepsTheOriginal(VoucherOrder order) {
        List<Voucher> sorted = order.sort(VOUCHERS);

        assertNotSame(VOUCHERS, sorted);
        assertEquals(List.of(SPAIN_AGAIN, SPAIN, AUSTRIA, POLAND), VOUCHERS);
        assertEquals(VOUCHERS.size(), sorted.size());
    }

    @ParameterizedTest
    @EnumSource(VoucherOrder.class)
    void sortsTheMainDocument(VoucherOrder order) {
        List<Voucher> sorted = order.sort(new VoucherReader(ParserKind.values()[0]).read(SampleFiles.MAIN_XML));
        Comparator<Voucher> comparator = order.comparator();

        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(comparator.compare(sorted.get(i - 1), sorted.get(i)) < 0, order + " at position " + i);
        }
    }

    @ParameterizedTest
    @CsvSource({"country, COUNTRY", "TYPE, TYPE", "Duration, DURATION", "' stars ', STARS", "cost, COST"})
    void isParsedFromTextInAnyCase(String text, VoucherOrder expected) {
        assertEquals(expected, VoucherOrder.parse(text));
    }

    @Test
    void unknownOrderIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> VoucherOrder.parse("weather"));

        assertEquals("Unknown sort order 'weather'", e.getMessage());
    }

    @Test
    void costComparatorComparesTotalsOfTheSameCurrency() {
        CostComparator comparator = new CostComparator();

        assertTrue(comparator.compare(AUSTRIA, SPAIN) < 0);
        assertTrue(comparator.compare(SPAIN, AUSTRIA) > 0);
        assertEquals(0, comparator.compare(SPAIN, SPAIN_AGAIN));
    }

    @Test
    void costComparatorNeverMixesCurrencies() {
        CostComparator comparator = new CostComparator();

        // 9000 UAH goes before 450 EUR because UAH is declared before EUR
        assertTrue(comparator.compare(POLAND, AUSTRIA) < 0);
        assertTrue(comparator.compare(AUSTRIA, POLAND) > 0);
    }
}
