package com.ticket.event_service.controller;

import com.ticket.event_service.dto.SeatLockRequest;
import com.ticket.event_service.dto.SeatLockResponse;
import com.ticket.event_service.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/lock")
    public ResponseEntity<SeatLockResponse> lockSeats(@RequestBody SeatLockRequest request) {
        SeatLockResponse response = seatService.lockSeats(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/unlock")
    public ResponseEntity<SeatLockResponse> unlockSeats(@RequestBody SeatLockRequest request) {
        return ResponseEntity.ok(seatService.unlockSeats(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<SeatLockResponse> confirmSeats(@RequestBody SeatLockRequest request) {
        return ResponseEntity.ok(seatService.confirmSeats(request));
    }
}
