package com.touragency.sorting;

import com.touragency.model.Voucher;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Orders vouchers from the cheapest to the most expensive one.
 * Amounts in different currencies cannot be compared directly, so vouchers are
 * first grouped by currency and then ordered by the total cost inside the group.
 */
public final class CostComparator implements Comparator<Voucher>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Voucher first, Voucher second) {
        int byCurrency = first.getCost().currency().compareTo(second.getCost().currency());
        if (byCurrency != 0) {
            return byCurrency;
        }
        return first.getCost().total().compareTo(second.getCost().total());
    }
}
