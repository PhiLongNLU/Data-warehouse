package org.example.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FutaTable {
    String transitTime;
    String startCity;
    String endCity;
    String startPoint;
    String endPoint;
    String departureDate;
    String departureTime;
    String arrivalTime;
    String arrivalDate;
    String ticketPrice;
    String busType;
    String totalAvailableSeat;
    String dateGetData;
    String dateExpiredData;

    public FutaTable(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        this.arrivalDate = formatter.format(LocalDate.now());
        this.dateExpiredData = formatter.format(LocalDate.MAX);
        this.dateGetData = formatter.format(LocalDate.now());
    }

    public String getDateGetData() {
        return dateGetData;
    }

    public String getDateExpiredData() {
        return dateExpiredData;
    }

    public void setDateExpiredData(String dateExpiredData) {
        this.dateExpiredData = dateExpiredData;
    }

    public String getBusType() {
        return busType;
    }

    public String getTotalAvailableSeat() {
        return totalAvailableSeat;
    }

    public void setTotalAvailableSeat(String totalAvailableSeat) {
        this.totalAvailableSeat = totalAvailableSeat;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getTransitTime() {
        return transitTime;
    }

    public void setTransitTime(String transitTime) {
        this.transitTime = transitTime;
    }

    public String getStartCity() {
        return startCity;
    }

    public void setStartCity(String startCity) {
        this.startCity = startCity;
    }

    public String getEndCity() {
        return endCity;
    }

    public void setEndCity(String endCity) {
        this.endCity = endCity;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}
