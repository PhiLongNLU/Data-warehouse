package org.example.model;

import java.time.LocalDate;
import java.util.Objects;

public class FactTrip {
    private  int transitKey;
    private  LocalDate dateExpiredData;
    private  double ticketPrice;
    private  int totalAvailableSeat;
    public FactTrip(int transitKey, LocalDate dateExpiredData, double ticketPrice, int totalAvailableSeat) {
        this.transitKey = transitKey;
        this.dateExpiredData = dateExpiredData;
        this.ticketPrice = ticketPrice;
        this.totalAvailableSeat = totalAvailableSeat;
    }

    public FactTrip() {
    }
    public int getTransitKey() {
        return transitKey;
    }

    public void setTransitKey(int transitKey) {
        this.transitKey = transitKey;
    }


    public LocalDate getDateExpiredData() {
        return dateExpiredData;
    }

    public void setDateExpiredData(LocalDate dateExpiredData) {
        this.dateExpiredData = dateExpiredData;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getTotalAvailableSeat() {
        return totalAvailableSeat;
    }

    public void setTotalAvailableSeat(int totalAvailableSeat) {
        this.totalAvailableSeat = totalAvailableSeat;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactTrip factTrip = (FactTrip) o;
        return transitKey == factTrip.transitKey && Double.compare(ticketPrice, factTrip.ticketPrice) == 0 && totalAvailableSeat == factTrip.totalAvailableSeat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitKey, ticketPrice, totalAvailableSeat);
    }
}
