package com.ticket.booking_service.exception;

import com.ticket.common.dto.ApiResponse;
import com.ticket.common.exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.error(exception.getErrorCode(), exception.getErrorMessage()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String fieldError = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", fieldError != null ? fieldError : "Validation failed"));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handlingException(Exception exception) {
        // Log the exception
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("UNCATEGORIZED_EXCEPTION", exception.getMessage()));
    }
}
