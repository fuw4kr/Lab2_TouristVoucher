package com.touragency;

import com.touragency.model.Cost;
import com.touragency.model.Currency;
import com.touragency.model.HotelCharacteristic;
import com.touragency.model.IncludedService;
import com.touragency.model.Meal;
import com.touragency.model.StayDuration;
import com.touragency.model.TourType;
import com.touragency.model.Transport;
import com.touragency.model.Voucher;
import java.math.BigDecimal;
import java.util.List;

/** Vouchers shared by the tests. */
public final class Samples {

    private Samples() {
    }

    /** @return the voucher v-101 of the sample document */
    public static Voucher greece() {
        Voucher voucher = new Voucher();
        voucher.setId("v-101");
        voucher.setType(TourType.REST);
        voucher.setCountry("Greece");
        voucher.setDuration(new StayDuration(8, 7));
        voucher.setTransport(Transport.AIR);
        voucher.setHotel(new HotelCharacteristic(4, Meal.AI, 2, true, true));
        voucher.setCost(new Cost(Currency.EUR, new BigDecimal("1250.00"), List.of(
                new IncludedService("flight", new BigDecimal("420.00")),
                new IncludedService("hotel", new BigDecimal("760.00")),
                new IncludedService("transfer", new BigDecimal("70.00")))));
        return voucher;
    }

    /** @return a voucher with the given values of the fields used for sorting */
    public static Voucher voucher(String id, String country, TourType type, int days, int stars,
                                  Currency currency, String total) {
        Voucher voucher = new Voucher();
        voucher.setId(id);
        voucher.setCountry(country);
        voucher.setType(type);
        voucher.setDuration(new StayDuration(days, days - 1));
        voucher.setTransport(Transport.AUTO);
        voucher.setHotel(new HotelCharacteristic(stars, Meal.BB, 2, true, false));
        voucher.setCost(new Cost(currency, new BigDecimal(total),
                List.of(new IncludedService("package", new BigDecimal(total)))));
        return voucher;
    }
}
