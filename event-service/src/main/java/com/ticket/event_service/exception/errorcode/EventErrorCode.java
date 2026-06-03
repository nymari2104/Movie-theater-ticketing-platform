package com.ticket.event_service.exception.errorcode;

import com.ticket.common.exception.errorcode.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum EventErrorCode implements ErrorCode {
    MOVIE_NOT_FOUND("MOVIE_NOT_FOUND", "Movie not found", HttpStatus.NOT_FOUND),
    SHOWTIME_NOT_FOUND("SHOWTIME_NOT_FOUND", "Showtime not found", HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND("SEAT_NOT_FOUND", "Seat not found", HttpStatus.NOT_FOUND),
    SEATS_ALREADY_RESERVED("SEATS_ALREADY_RESERVED", "One or more selected seats are already reserved or booked.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatusCode statusCode;

    EventErrorCode(String code, String message, HttpStatusCode statusCode) {
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
