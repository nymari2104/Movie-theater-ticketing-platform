package com.ticket.booking_service.client;

import com.ticket.booking_service.dto.SeatLockRequest;
import com.ticket.booking_service.dto.SeatLockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "event-service")
public interface EventClient {

    @PostMapping("/api/events/seats/lock")
    ResponseEntity<SeatLockResponse> lockSeats(@RequestBody SeatLockRequest request);

    @PostMapping("/api/events/seats/unlock")
    ResponseEntity<SeatLockResponse> unlockSeats(@RequestBody SeatLockRequest request);

    @PostMapping("/api/events/seats/confirm")
    ResponseEntity<SeatLockResponse> confirmSeats(@RequestBody SeatLockRequest request);
}
