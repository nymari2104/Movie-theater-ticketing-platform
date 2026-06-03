package com.ticket.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingResponse {
    private UUID id;
    private String userId;
    private UUID showtimeId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private List<BookingSeatResponse> seats;

    public BookingResponse() {
    }

    public BookingResponse(UUID id, String userId, UUID showtimeId, BigDecimal totalPrice, String status, LocalDateTime createdAt, List<BookingSeatResponse> seats) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.seats = seats;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<BookingSeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<BookingSeatResponse> seats) {
        this.seats = seats;
    }
}
