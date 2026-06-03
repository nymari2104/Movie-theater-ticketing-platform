package com.ticket.event_service.service;

import com.ticket.event_service.dto.SeatLockRequest;
import com.ticket.event_service.dto.SeatLockResponse;

public interface SeatService {
    SeatLockResponse lockSeats(SeatLockRequest request);
    SeatLockResponse unlockSeats(SeatLockRequest request);
    SeatLockResponse confirmSeats(SeatLockRequest request);
}
