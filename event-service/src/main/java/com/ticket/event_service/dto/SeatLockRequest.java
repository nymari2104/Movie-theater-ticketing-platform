package com.ticket.event_service.dto;

import java.util.List;
import java.util.UUID;

public class SeatLockRequest {
    private List<UUID> seatIds;

    public SeatLockRequest() {
    }

    public SeatLockRequest(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }

    public List<UUID> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }
}
