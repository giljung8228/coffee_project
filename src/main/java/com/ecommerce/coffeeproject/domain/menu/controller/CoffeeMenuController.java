package com.ecommerce.coffeeproject.domain.menu.controller;

import com.ecommerce.coffeeproject.domain.menu.dto.CoffeeMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.service.CoffeeMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class CoffeeMenuController {

    private final CoffeeMenuService coffeeMenuService;

    @GetMapping
    public List<CoffeeMenuResponse> getMenus(){
        return coffeeMenuService.getMenus();
    }
}
