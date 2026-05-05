package com.ecommerce.coffeeproject.domain.order.dto;

import com.ecommerce.coffeeproject.domain.order.entity.CoffeeOrder;

import java.time.LocalDateTime;

public record OrderDataPlatformPayload(
        Long userId,
        Long menuId,
        Long paymentAmount,
        LocalDateTime orderedAt
) {

    public static OrderDataPlatformPayload from(CoffeeOrder order) {
        return new OrderDataPlatformPayload(
                order.getMember().getId(),
                order.getMenu().getId(),
                order.getPaymentAmount(),
                order.getOrderedAt()
        );
    }
}