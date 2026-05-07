package com.ecommerce.coffeeproject.global.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String path){
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().name(),
                errorCode.getCode(),
                errorCode.getMessage(),
                path
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path){
        return new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().name(),
                errorCode.getCode(),
                message,
                path
        );
    }
}
