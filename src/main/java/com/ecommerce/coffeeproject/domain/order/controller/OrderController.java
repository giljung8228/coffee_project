package com.ecommerce.coffeeproject.domain.order.controller;

import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateRequest;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateResponse;
import com.ecommerce.coffeeproject.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request);
    }
}