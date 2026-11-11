package com.touragency.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class XmlNamedTest {

    @ParameterizedTest
    @EnumSource(TourType.class)
    void tourTypeIsFoundByXmlName(TourType type) {
        assertSame(type, XmlNamed.fromXml(TourType.class, type.xmlName()));
    }

    @ParameterizedTest
    @EnumSource(Transport.class)
    void transportIsFoundByXmlName(Transport transport) {
        assertSame(transport, XmlNamed.fromXml(Transport.class, transport.xmlName()));
    }

    @ParameterizedTest
    @EnumSource(Meal.class)
    void mealIsFoundByXmlName(Meal meal) {
        assertSame(meal, XmlNamed.fromXml(Meal.class, meal.xmlName()));
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void currencyIsFoundByXmlName(Currency currency) {
        assertSame(currency, XmlNamed.fromXml(Currency.class, currency.xmlName()));
    }

    @Test
    void xmlNamesAreTheSameAsInTheSchema() {
        assertEquals("weekend", TourType.WEEKEND.xmlName());
        assertEquals("excursion", TourType.EXCURSION.xmlName());
        assertEquals("rest", TourType.REST.xmlName());
        assertEquals("pilgrimage", TourType.PILGRIMAGE.xmlName());
        assertEquals("air", Transport.AIR.xmlName());
        assertEquals("railway", Transport.RAILWAY.xmlName());
        assertEquals("auto", Transport.AUTO.xmlName());
        assertEquals("liner", Transport.LINER.xmlName());
        assertEquals("none", Meal.NONE.xmlName());
        assertEquals("BB", Meal.BB.xmlName());
        assertEquals("HB", Meal.HB.xmlName());
        assertEquals("AI", Meal.AI.xmlName());
        assertEquals("UAH", Currency.UAH.xmlName());
        assertEquals("USD", Currency.USD.xmlName());
        assertEquals("EUR", Currency.EUR.xmlName());
    }

    @Test
    void unknownNameIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> XmlNamed.fromXml(Transport.class, "bicycle"));

        assertEquals("'bicycle' is not a valid Transport", e.getMessage());
    }

    @Test
    void lookupIsCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> XmlNamed.fromXml(Meal.class, "ai"));
        assertThrows(IllegalArgumentException.class, () -> XmlNamed.fromXml(TourType.class, "REST"));
    }

    @Test
    void onlyNoneMeansMealsAreNotIncluded() {
        assertFalse(Meal.NONE.isIncluded());
        assertTrue(Meal.BB.isIncluded());
        assertTrue(Meal.HB.isIncluded());
        assertTrue(Meal.AI.isIncluded());
    }
}
