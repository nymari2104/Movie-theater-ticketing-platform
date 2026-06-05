package com.ticket.booking_service.service.impl;

import com.ticket.booking_service.client.EventClient;
import com.ticket.booking_service.dto.*;
import com.ticket.common.exception.AppException;
import com.ticket.booking_service.exception.errorcode.BookingErrorCode;
import com.ticket.booking_service.model.Booking;
import com.ticket.booking_service.model.BookingSeat;
import com.ticket.booking_service.model.BookingStatus;
import com.ticket.booking_service.repository.BookingRepository;
import com.ticket.booking_service.repository.BookingSeatRepository;
import com.ticket.booking_service.service.BookingService;
import com.ticket.booking_service.service.DistributedLockService;
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
    private final DistributedLockService distributedLockService;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    public BookingServiceImpl(BookingRepository bookingRepository, 
                              BookingSeatRepository bookingSeatRepository, 
                              EventClient eventClient,
                              DistributedLockService distributedLockService,
                              org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.eventClient = eventClient;
        this.distributedLockService = distributedLockService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        List<UUID> seatIds = request.getSeatIds();
        List<String> acquiredLocks = new ArrayList<>();
        String lockValue = UUID.randomUUID().toString(); // Định danh duy nhất cho request này

        try {
            // Bước 1: Acquire Distributed Lock cho từng ghế trong danh sách chọn
            for (UUID seatId : seatIds) {
                String lockKey = "lock:seat:" + seatId;
                boolean lockAcquired = distributedLockService.acquireLock(lockKey, lockValue, 5000); // Tự giải phóng sau 5 giây đề phòng sự cố
                if (!lockAcquired) {
                    throw new AppException(BookingErrorCode.SEAT_ALREADY_PROCESSED);
                }
                acquiredLocks.add(lockKey);
            }

            // Bước 2: Gọi Feign Client sang event-service để kiểm tra trạng thái và chuyển thành RESERVED trong DB
            SeatLockRequest lockRequest = new SeatLockRequest(seatIds);
            try {
                ResponseEntity<SeatLockResponse> lockResponse = eventClient.lockSeats(lockRequest);
                if (lockResponse.getBody() == null || !lockResponse.getBody().isSuccess()) {
                    throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, 
                        (lockResponse.getBody() != null ? lockResponse.getBody().getMessage() : "Unknown error"));
                }
            } catch (AppException ae) {
                throw ae;
            } catch (feign.FeignException e) {
                String errorMsg = "Unable to lock seats. One or more seats might already be reserved or booked.";
                try {
                    String content = e.contentUTF8();
                    if (content != null && !content.isEmpty()) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<?, ?> map = mapper.readValue(content, java.util.Map.class);
                        if (map.containsKey("message")) {
                            errorMsg = (String) map.get("message");
                        }
                    }
                } catch (Exception jsonEx) {
                    // ignore and use default
                }
                throw new AppException(BookingErrorCode.SEAT_ALREADY_PROCESSED, errorMsg);
            } catch (Exception e) {
                throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, "Unable to lock seats: " + e.getMessage());
            }

            // Bước 3: Tạo đơn đặt vé tạm thời với trạng thái PENDING
            Booking booking = new Booking();
            booking.setUserId(request.getUserId());
            booking.setShowtimeId(request.getShowtimeId());
            booking.setTotalPrice(request.getTotalPrice());
            booking.setStatus(BookingStatus.PENDING);

            Booking savedBooking = bookingRepository.save(booking);

            // Bước 4: Tạo thông tin chi tiết các ghế của đơn hàng này
            List<BookingSeat> bookingSeats = new ArrayList<>();
            for (int i = 0; i < seatIds.size(); i++) {
                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setBooking(savedBooking);
                bookingSeat.setSeatId(seatIds.get(i));
                bookingSeat.setSeatNumber(request.getSeatNumbers().get(i));
                bookingSeat.setBookingDate(request.getBookingDate());
                bookingSeats.add(bookingSeat);
            }
            bookingSeatRepository.saveAll(bookingSeats);

            // Gửi sự kiện tạo booking sang Kafka
            com.ticket.common.event.BookingCreatedEvent event = new com.ticket.common.event.BookingCreatedEvent(
                savedBooking.getId(),
                savedBooking.getUserId(),
                savedBooking.getShowtimeId(),
                savedBooking.getTotalPrice(),
                seatIds
            );
            kafkaTemplate.send("booking-created-events", savedBooking.getId().toString(), event);

            return mapToResponse(savedBooking, bookingSeats);
        } finally {
            // Bước 5: Đảm bảo giải phóng toàn bộ khóa phân tán sau khi kết thúc transaction
            for (String lockKey : acquiredLocks) {
                distributedLockService.releaseLock(lockKey, lockValue);
            }
        }
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND, "Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(BookingErrorCode.INVALID_BOOKING_STATUS, "Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        List<UUID> seatIds = seats.stream().map(BookingSeat::getSeatId).collect(Collectors.toList());

        // Gọi Feign Client xác nhận các ghế này chuyển sang trạng thái BOOKED vĩnh viễn
        try {
            ResponseEntity<SeatLockResponse> confirmResponse = eventClient.confirmSeats(new SeatLockRequest(seatIds));
            if (confirmResponse.getBody() == null || !confirmResponse.getBody().isSuccess()) {
                throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, "Confirming seats failed");
            }
        } catch (AppException ae) {
            throw ae;
        } catch (feign.FeignException e) {
            String errorMsg = "Confirming seats failed.";
            try {
                String content = e.contentUTF8();
                if (content != null && !content.isEmpty()) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<?, ?> map = mapper.readValue(content, java.util.Map.class);
                    if (map.containsKey("message")) {
                        errorMsg = (String) map.get("message");
                    }
                }
            } catch (Exception jsonEx) {
                // ignore
            }
            throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, errorMsg);
        } catch (Exception e) {
            throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, "Confirming seats failed: " + e.getMessage());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, seats);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND, "Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToResponse(booking, bookingSeatRepository.findByBookingId(bookingId));
        }

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        List<UUID> seatIds = seats.stream().map(BookingSeat::getSeatId).collect(Collectors.toList());

        // Giải phóng ghế từ RESERVED về AVAILABLE trong event-service
        try {
            eventClient.unlockSeats(new SeatLockRequest(seatIds));
        } catch (feign.FeignException e) {
            String errorMsg = "Unlocking seats failed.";
            try {
                String content = e.contentUTF8();
                if (content != null && !content.isEmpty()) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<?, ?> map = mapper.readValue(content, java.util.Map.class);
                    if (map.containsKey("message")) {
                        errorMsg = (String) map.get("message");
                    }
                }
            } catch (Exception jsonEx) {
                // ignore
            }
            throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, errorMsg);
        } catch (Exception e) {
            throw new AppException(BookingErrorCode.SEATS_LOCK_FAILED, "Unlocking seats failed: " + e.getMessage());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking, seats);
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND, "Booking not found with id: " + bookingId));
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
