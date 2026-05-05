package com.ecommerce.coffeeproject.domain.order.dto;

import com.ecommerce.coffeeproject.domain.order.entity.CoffeeOrder;

import java.time.LocalDateTime;

public record OrderCreateResponse(
        Long orderId,
        Long userId,
        Long menuId,
        String menuName,
        Long paymentAmount,
        Long balance,
        String orderStatus,
        LocalDateTime orderedAt
) {

    public static OrderCreateResponse of(CoffeeOrder order, Long balance) {
        return new OrderCreateResponse(
                order.getId(),
                order.getMember().getId(),
                order.getMenu().getId(),
                order.getMenu().getName(),
                order.getPaymentAmount(),
                balance,
                order.getStatus().name(),
                order.getOrderedAt()
        );
    }
}