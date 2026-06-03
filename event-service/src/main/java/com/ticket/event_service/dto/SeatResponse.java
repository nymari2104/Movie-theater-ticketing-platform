package com.ticket.event_service.dto;

import java.util.UUID;

public class SeatResponse {
    private UUID id;
    private UUID showtimeId;
    private String seatNumber;
    private String status;

    public SeatResponse() {
    }

    public SeatResponse(UUID id, UUID showtimeId, String seatNumber, String status) {
        this.id = id;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(UUID showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
