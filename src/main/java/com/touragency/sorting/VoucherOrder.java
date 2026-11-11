package com.touragency.sorting;

import com.touragency.model.Voucher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** The ways a collection of vouchers can be sorted. */
public enum VoucherOrder {

    /** By country in alphabetical order. */
    COUNTRY(Comparator.comparing(Voucher::getCountry, String.CASE_INSENSITIVE_ORDER)),

    /** By type of the tour. */
    TYPE(Comparator.comparing(Voucher::getType)),

    /** From the shortest trip to the longest one. */
    DURATION(Comparator.comparingInt((Voucher voucher) -> voucher.getDuration().days())
            .thenComparingInt(voucher -> voucher.getDuration().nights())),

    /** From the best hotel to the worst one. */
    STARS(Comparator.comparingInt((Voucher voucher) -> voucher.getHotel().stars()).reversed()),

    /** From the cheapest voucher to the most expensive one, see {@link CostComparator}. */
    COST(new CostComparator());

    private final Comparator<Voucher> comparator;

    VoucherOrder(Comparator<Voucher> mainCriterion) {
        // the id makes the order of "equal" vouchers predictable
        this.comparator = mainCriterion.thenComparing(Voucher::getId);
    }

    public Comparator<Voucher> comparator() {
        return comparator;
    }

    /** @return a new sorted list; the given collection is not changed */
    public List<Voucher> sort(List<Voucher> vouchers) {
        List<Voucher> sorted = new ArrayList<>(vouchers);
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * @param name name of the order in any case, for example "cost"
     * @throws IllegalArgumentException if there is no such order
     */
    public static VoucherOrder parse(String name) {
        try {
            return valueOf(name.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sort order '" + name + "'", e);
        }
    }
}
