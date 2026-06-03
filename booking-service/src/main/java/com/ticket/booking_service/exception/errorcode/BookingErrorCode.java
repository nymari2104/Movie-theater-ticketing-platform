package com.ticket.booking_service.exception.errorcode;

import com.ticket.common.exception.errorcode.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum BookingErrorCode implements ErrorCode {
    SEAT_ALREADY_PROCESSED("SEAT_ALREADY_PROCESSED", "One of the selected seats is currently being processed by another transaction. Please try again.", HttpStatus.BAD_REQUEST),
    SEATS_LOCK_FAILED("SEATS_LOCK_FAILED", "Locking seats failed", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", "Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_STATUS("INVALID_BOOKING_STATUS", "Booking status is invalid for this operation", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatusCode statusCode;

    BookingErrorCode(String code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
