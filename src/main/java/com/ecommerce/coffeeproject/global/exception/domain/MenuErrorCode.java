package com.ecommerce.coffeeproject.global.exception.domain;

import com.ecommerce.coffeeproject.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MenuErrorCode implements ErrorCode {
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."),
    MENU_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "MENU_NOT_ON_SALE", "현재 주문할 수 없는 메뉴입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MenuErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
