package com.ticket.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BookingRequest {
    private String userId;
    private UUID showtimeId;
    private List<UUID> seatIds;
    private List<String> seatNumbers;
    private LocalDate bookingDate;
    private BigDecimal totalPrice;

    public BookingRequest() {
    }

    public BookingRequest(String userId, UUID showtimeId, List<UUID> seatIds, List<String> seatNumbers, LocalDate bookingDate, BigDecimal totalPrice) {
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.seatNumbers = seatNumbers;
        this.bookingDate = bookingDate;
        this.totalPrice = totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UUID getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(UUID showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<UUID> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
