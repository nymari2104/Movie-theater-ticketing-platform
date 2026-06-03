package com.ticket.booking_service.controller;

import com.ticket.booking_service.dto.BookingRequest;
import com.ticket.booking_service.dto.BookingResponse;
import com.ticket.booking_service.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(request), HttpStatus.CREATED);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }
}
