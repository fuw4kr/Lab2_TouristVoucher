package com.touragency.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Elements of the voucher document together with the rule that tells how each
 * of them fills a {@link VoucherBuilder}. The table is used by every parser, so
 * the mapping "element -> field" is written only once.
 */
enum VoucherTag {
    TOURIST_VOUCHER("TouristVoucher", null),
    VOUCHER("Voucher", (builder, attributes, text) -> builder.id(attributes.get(Attribute.ID))),
    TYPE("Type", text(VoucherBuilder::type)),
    COUNTRY("Country", text(VoucherBuilder::country)),
    NUMBER_DAYS_NIGHTS("NumberDaysNights", (builder, attributes, text) ->
            builder.duration(attributes.get(Attribute.DAYS), attributes.get(Attribute.NIGHTS))),
    TRANSPORT("Transport", text(VoucherBuilder::transport)),
    HOTEL_CHARACTERISTIC("HotelCharacteristic", null),
    STARS("Stars", text(VoucherBuilder::stars)),
    MEAL("Meal", text(VoucherBuilder::meal)),
    ROOM_TYPE("RoomType", text(VoucherBuilder::roomPlaces)),
    TV("Tv", text(VoucherBuilder::tv)),
    AIR_CONDITIONER("AirConditioner", text(VoucherBuilder::airConditioner)),
    COST("Cost", (builder, attributes, text) -> builder.currency(attributes.get(Attribute.CURRENCY))),
    TOTAL("Total", text(VoucherBuilder::total)),
    SERVICE("Service", (builder, attributes, text) -> builder.service(text, attributes.get(Attribute.PRICE)));

    /** Names of the attributes used in the document. */
    static final class Attribute {
        static final String ID = "id";
        static final String DAYS = "days";
        static final String NIGHTS = "nights";
        static final String CURRENCY = "currency";
        static final String PRICE = "price";

        private Attribute() {
        }
    }

    /** Moves the data of one element (its attributes and text) into the builder. */
    @FunctionalInterface
    interface Binder {
        void bind(VoucherBuilder builder, Map<String, String> attributes, String text);
    }

    private static final List<VoucherTag> BOUND = Arrays.stream(values())
            .filter(VoucherTag::isBound)
            .toList();

    private final String xmlName;
    private final Binder binder;

    VoucherTag(String xmlName, Binder binder) {
        this.xmlName = xmlName;
        this.binder = binder;
    }

    /** A binder for elements that carry only text. */
    private static Binder text(BiConsumer<VoucherBuilder, String> setter) {
        return (builder, attributes, text) -> setter.accept(builder, text);
    }

    String xmlName() {
        return xmlName;
    }

    /** @return {@code false} for elements that only group other elements and hold no data themselves */
    boolean isBound() {
        return binder != null;
    }

    /**
     * Puts the data of an element of this kind into the builder.
     * Does nothing for elements that hold no data.
     */
    void bind(VoucherBuilder builder, Map<String, String> attributes, String text) {
        if (isBound()) {
            binder.bind(builder, attributes, text);
        }
    }

    static Optional<VoucherTag> find(String xmlName) {
        return Arrays.stream(values())
                .filter(tag -> tag.xmlName.equals(xmlName))
                .findFirst();
    }

    /** @return the elements that hold data, in the order of the schema */
    static List<VoucherTag> bound() {
        return BOUND;
    }
}
