package com.ticket.event_service.dto;

public class SeatLockResponse {
    private boolean success;
    private String message;

    public SeatLockResponse() {
    }

    public SeatLockResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
