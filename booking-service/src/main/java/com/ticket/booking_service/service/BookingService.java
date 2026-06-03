package com.ticket.booking_service.service;

import com.ticket.booking_service.dto.BookingRequest;
import com.ticket.booking_service.dto.BookingResponse;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse confirmBooking(UUID bookingId);
    BookingResponse cancelBooking(UUID bookingId);
    BookingResponse getBookingById(UUID bookingId);
}
