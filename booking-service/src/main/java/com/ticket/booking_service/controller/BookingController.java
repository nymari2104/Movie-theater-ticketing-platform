package com.ticket.booking_service.controller;

import com.ticket.common.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@RequestBody BookingRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Booking created successfully", bookingService.createBooking(request)), HttpStatus.CREATED);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", bookingService.confirmBooking(bookingId)));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", bookingService.cancelBooking(bookingId)));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Get booking details successfully", bookingService.getBookingById(bookingId)));
    }
}
