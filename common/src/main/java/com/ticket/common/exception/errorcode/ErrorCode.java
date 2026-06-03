package com.ticket.common.exception.errorcode;

import org.springframework.http.HttpStatusCode;

public interface ErrorCode {
    String getCode();
    String getMessage();
    HttpStatusCode getStatusCode();
}
