package com.ticket.common.exception;

import com.ticket.common.exception.errorcode.ErrorCode;
import org.springframework.http.HttpStatusCode;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        if (errorCode != null) {
            return errorCode.getCode();
        }
        return "UNKNOWN_ERROR";
    }

    public String getErrorMessage() {
        String customMessage = super.getMessage();
        if (errorCode != null) {
            return customMessage != null && !customMessage.equals(errorCode.getMessage()) ?
                    customMessage : errorCode.getMessage();
        }
        return customMessage != null ? customMessage : "Unknown error occurred";
    }

    public HttpStatusCode getStatusCode() {
        if (errorCode != null) {
            return errorCode.getStatusCode();
        }
        return org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
