package com.ecommerce.coffeeproject.global.exception.domain;

import com.ecommerce.coffeeproject.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PointErrorCode implements ErrorCode {

    POINT_NOT_FOUND(HttpStatus.BAD_REQUEST, "POINT_NOT_FOUND", "포인트를 먼저 충전해주세요."),
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "POINT_NOT_ENOUGH", "포인트가 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PointErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}