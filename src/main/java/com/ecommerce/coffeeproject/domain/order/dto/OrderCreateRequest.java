package com.ecommerce.coffeeproject.domain.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(

        @NotNull(message = "사용자 식별값은 필수입니다.")
        Long userId,

        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId
) {
}