package com.ticket.booking_service.service.impl;

import com.ticket.booking_service.client.EventClient;
import com.ticket.booking_service.dto.*;
import com.ticket.booking_service.model.Booking;
import com.ticket.booking_service.model.BookingSeat;
import com.ticket.booking_service.model.BookingStatus;
import com.ticket.booking_service.repository.BookingRepository;
import com.ticket.booking_service.repository.BookingSeatRepository;
import com.ticket.booking_service.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EventClient eventClient;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository, EventClient eventClient) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.eventClient = eventClient;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Bước 1: Gọi Feign Client sang event-service để thực hiện khóa tạm thời các ghế được chọn
        SeatLockRequest lockRequest = new SeatLockRequest(request.getSeatIds());
        try {
            ResponseEntity<SeatLockResponse> lockResponse = eventClient.lockSeats(lockRequest);
            if (lockResponse.getBody() == null || !lockResponse.getBody().isSuccess()) {
                throw new RuntimeException("Locking seats failed: " + 
                    (lockResponse.getBody() != null ? lockResponse.getBody().getMessage() : "Unknown error"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to lock seats. One or more seats might already be reserved or booked. " + e.getMessage());
        }

        // Bước 2: Tạo đơn đặt vé tạm thời với trạng thái PENDING
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setShowtimeId(request.getShowtimeId());
        booking.setTotalPrice(request.getTotalPrice());
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        // Bước 3: Tạo thông tin chi tiết các ghế của đơn hàng này
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (int i = 0; i < request.getSeatIds().size(); i++) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setSeatId(request.getSeatIds().get(i));
            bookingSeat.setSeatNumber(request.getSeatNumbers().get(i));
            bookingSeat.setBookingDate(request.getBookingDate());
            bookingSeats.add(bookingSeat);
        }
        bookingSeatRepository.saveAll(bookingSeats);

        return mapToResponse(savedBooking, bookingSeats);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        List<UUID> seatIds = seats.stream().map(BookingSeat::getSeatId).collect(Collectors.toList());

        // Gọi Feign Client xác nhận các ghế này chuyển sang trạng thái BOOKED vĩnh viễn
        ResponseEntity<SeatLockResponse> confirmResponse = eventClient.confirmSeats(new SeatLockRequest(seatIds));
        if (confirmResponse.getBody() == null || !confirmResponse.getBody().isSuccess()) {
            throw new RuntimeException("Confirming seats failed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, seats);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToResponse(booking, bookingSeatRepository.findByBookingId(bookingId));
        }

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        List<UUID> seatIds = seats.stream().map(BookingSeat::getSeatId).collect(Collectors.toList());

        // Giải phóng ghế từ RESERVED về AVAILABLE trong event-service
        eventClient.unlockSeats(new SeatLockRequest(seatIds));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, seats);
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        return mapToResponse(booking, seats);
    }

    private BookingResponse mapToResponse(Booking booking, List<BookingSeat> seats) {
        List<BookingSeatResponse> seatResponses = seats.stream().map(seat -> new BookingSeatResponse(
                seat.getId(),
                seat.getSeatId(),
                seat.getSeatNumber(),
                seat.getBookingDate()
        )).collect(Collectors.toList());

        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getShowtimeId(),
                booking.getTotalPrice(),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                seatResponses
        );
    }
}
