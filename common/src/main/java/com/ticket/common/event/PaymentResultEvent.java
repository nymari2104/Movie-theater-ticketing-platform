package com.ticket.common.event;

import java.util.UUID;

public class PaymentResultEvent {
    private UUID bookingId;
    private UUID paymentId;
    private String status; // SUCCESS or FAILED
    private String message;

    public PaymentResultEvent() {
    }

    public PaymentResultEvent(UUID bookingId, UUID paymentId, String status, String message) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.status = status;
        this.message = message;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
