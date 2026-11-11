package com.touragency.model;

import java.util.Objects;

/** A tourist voucher: one {@code Voucher} element of the document. */
public class Voucher {

    private String id;
    private TourType type;
    private String country;
    private StayDuration duration;
    private Transport transport;
    private HotelCharacteristic hotel;
    private Cost cost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TourType getType() {
        return type;
    }

    public void setType(TourType type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public StayDuration getDuration() {
        return duration;
    }

    public void setDuration(StayDuration duration) {
        this.duration = duration;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public HotelCharacteristic getHotel() {
        return hotel;
    }

    public void setHotel(HotelCharacteristic hotel) {
        this.hotel = hotel;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Voucher that = (Voucher) obj;
        return Objects.equals(id, that.id)
                && type == that.type
                && Objects.equals(country, that.country)
                && Objects.equals(duration, that.duration)
                && transport == that.transport
                && Objects.equals(hotel, that.hotel)
                && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, country, duration, transport, hotel, cost);
    }

    @Override
    public String toString() {
        return "Voucher " + id + ": " + type + " in " + country + ", " + duration
                + ", by " + transport + ", " + hotel + ", cost " + cost;
    }
}
