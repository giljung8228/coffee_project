package com.ecommerce.coffeeproject.domain.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(

        @NotNull(message = "사용자 식별값은 필수입니다.")
        Long userId,

        @NotNull(message = "충전금액은 필수입니다.")
        @Positive(message = "충전금액은 1원 이상이어야 합니다.")
        Long amount
) {
}
