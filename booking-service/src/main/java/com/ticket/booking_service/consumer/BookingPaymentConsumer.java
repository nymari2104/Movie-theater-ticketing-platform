package com.ticket.booking_service.consumer;

import com.ticket.common.event.PaymentResultEvent;
import com.ticket.booking_service.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookingPaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingPaymentConsumer.class);

    private final BookingService bookingService;

    public BookingPaymentConsumer(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @KafkaListener(topics = "payment-result-events", groupId = "booking-group")
    public void consumePaymentResult(PaymentResultEvent event) {
        log.info("Received PaymentResultEvent: Booking ID = {}, Status = {}, Message = {}", 
                event.getBookingId(), event.getStatus(), event.getMessage());

        try {
            if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                log.info("Confirming booking asynchronously for Booking ID = {}", event.getBookingId());
                bookingService.confirmBooking(event.getBookingId());
            } else {
                log.warn("Cancelling booking asynchronously for Booking ID = {} due to payment failure", event.getBookingId());
                bookingService.cancelBooking(event.getBookingId());
            }
        } catch (Exception e) {
            log.error("Error processing booking status update for Booking ID = {}: {}", event.getBookingId(), e.getMessage());
        }
    }
}
