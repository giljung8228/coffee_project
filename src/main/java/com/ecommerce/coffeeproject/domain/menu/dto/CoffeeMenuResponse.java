package com.ecommerce.coffeeproject.domain.menu.dto;

import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;

public record CoffeeMenuResponse(
        Long menuId,
        String name,
        Long price
) {

    public static CoffeeMenuResponse from(CoffeeMenu coffeeMenu) {
        return new CoffeeMenuResponse(
                coffeeMenu.getId(),
                coffeeMenu.getName(),
                coffeeMenu.getPrice()
        );
    }
}