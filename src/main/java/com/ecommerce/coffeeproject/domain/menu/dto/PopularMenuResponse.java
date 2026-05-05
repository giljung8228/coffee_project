package com.ecommerce.coffeeproject.domain.menu.dto;

public record PopularMenuResponse(
        Long menuId,
        String name,
        Long price,
        Long orderCount
) {
}
