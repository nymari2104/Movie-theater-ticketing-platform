package com.ticket.booking_service.dto;

import java.time.LocalDate;
import java.util.UUID;

public class BookingSeatResponse {
    private UUID id;
    private UUID seatId;
    private String seatNumber;
    private LocalDate bookingDate;

    public BookingSeatResponse() {
    }

    public BookingSeatResponse(UUID id, UUID seatId, String seatNumber, LocalDate bookingDate) {
        this.id = id;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
}
