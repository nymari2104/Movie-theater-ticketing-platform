package com.ticket.common.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BookingCreatedEvent {
    private UUID bookingId;
    private String userId;
    private UUID showtimeId;
    private BigDecimal totalPrice;
    private List<UUID> seatIds;

    public BookingCreatedEvent() {
    }

    public BookingCreatedEvent(UUID bookingId, String userId, UUID showtimeId, BigDecimal totalPrice, List<UUID> seatIds) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.seatIds = seatIds;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<UUID> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }
}
