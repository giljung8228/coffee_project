package com.ecommerce.coffeeproject.domain.point.dto;

public record PointChargeResponse(
        Long userId,
        Long chargedAmount,
        Long balance
) {
}
