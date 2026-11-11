package com.touragency.parser;

import com.touragency.model.Cost;
import com.touragency.model.Currency;
import com.touragency.model.HotelCharacteristic;
import com.touragency.model.IncludedService;
import com.touragency.model.Meal;
import com.touragency.model.StayDuration;
import com.touragency.model.TourType;
import com.touragency.model.Transport;
import com.touragency.model.Voucher;
import com.touragency.model.XmlNamed;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the strings read from XML and turns them into a {@link Voucher}.
 * All conversions from text to numbers, flags and enums live here, the parsers
 * themselves never deal with types.
 *
 * <p>Every method throws {@link IllegalArgumentException} if the text cannot be converted.
 */
final class VoucherBuilder {

    private String id;
    private TourType type;
    private String country;
    private StayDuration duration;
    private Transport transport;
    private Integer stars;
    private Meal meal;
    private Integer roomPlaces;
    private Boolean tv;
    private Boolean airConditioner;
    private Currency currency;
    private BigDecimal total;
    private final List<IncludedService> services = new ArrayList<>();

    void id(String value) {
        id = clean(value, VoucherTag.VOUCHER);
    }

    void type(String value) {
        type = XmlNamed.fromXml(TourType.class, clean(value, VoucherTag.TYPE));
    }

    void country(String value) {
        country = clean(value, VoucherTag.COUNTRY);
    }

    void duration(String days, String nights) {
        duration = new StayDuration(
                toInt(days, VoucherTag.NUMBER_DAYS_NIGHTS),
                toInt(nights, VoucherTag.NUMBER_DAYS_NIGHTS));
    }

    void transport(String value) {
        transport = XmlNamed.fromXml(Transport.class, clean(value, VoucherTag.TRANSPORT));
    }

    void stars(String value) {
        stars = toInt(value, VoucherTag.STARS);
    }

    void meal(String value) {
        meal = XmlNamed.fromXml(Meal.class, clean(value, VoucherTag.MEAL));
    }

    void roomPlaces(String value) {
        roomPlaces = toInt(value, VoucherTag.ROOM_TYPE);
    }

    void tv(String value) {
        tv = toBoolean(value, VoucherTag.TV);
    }

    void airConditioner(String value) {
        airConditioner = toBoolean(value, VoucherTag.AIR_CONDITIONER);
    }

    void currency(String value) {
        currency = XmlNamed.fromXml(Currency.class, clean(value, VoucherTag.COST));
    }

    void total(String value) {
        total = toMoney(value, VoucherTag.TOTAL);
    }

    void service(String name, String price) {
        services.add(new IncludedService(clean(name, VoucherTag.SERVICE), toMoney(price, VoucherTag.SERVICE)));
    }

    /** @throws IllegalArgumentException if some mandatory part of the voucher has not been read */
    Voucher build() {
        Voucher voucher = new Voucher();
        voucher.setId(present(id, VoucherTag.VOUCHER));
        voucher.setType(present(type, VoucherTag.TYPE));
        voucher.setCountry(present(country, VoucherTag.COUNTRY));
        voucher.setDuration(present(duration, VoucherTag.NUMBER_DAYS_NIGHTS));
        voucher.setTransport(present(transport, VoucherTag.TRANSPORT));
        voucher.setHotel(new HotelCharacteristic(
                present(stars, VoucherTag.STARS),
                present(meal, VoucherTag.MEAL),
                present(roomPlaces, VoucherTag.ROOM_TYPE),
                present(tv, VoucherTag.TV),
                present(airConditioner, VoucherTag.AIR_CONDITIONER)));
        voucher.setCost(new Cost(
                present(currency, VoucherTag.COST),
                present(total, VoucherTag.TOTAL),
                services));
        return voucher;
    }

    private <T> T present(T value, VoucherTag tag) {
        if (value == null) {
            throw new IllegalArgumentException("Voucher " + id + " has no data for <" + tag.xmlName() + ">");
        }
        return value;
    }

    /** Trims the text and makes sure there is something left. */
    private static String clean(String value, VoucherTag tag) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("<" + tag.xmlName() + "> has an empty value or attribute");
        }
        return value.strip();
    }

    private static int toInt(String value, VoucherTag tag) {
        String text = clean(value, tag);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("<" + tag.xmlName() + ">: '" + text + "' is not an integer", e);
        }
    }

    private static BigDecimal toMoney(String value, VoucherTag tag) {
        String text = clean(value, tag);
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("<" + tag.xmlName() + ">: '" + text + "' is not a number", e);
        }
    }

    /** xs:boolean may be written as true / false / 1 / 0. */
    private static boolean toBoolean(String value, VoucherTag tag) {
        String text = clean(value, tag);
        if (text.equals("true") || text.equals("1")) {
            return true;
        }
        if (text.equals("false") || text.equals("0")) {
            return false;
        }
        throw new IllegalArgumentException("<" + tag.xmlName() + ">: '" + text + "' is not a boolean");
    }
}
