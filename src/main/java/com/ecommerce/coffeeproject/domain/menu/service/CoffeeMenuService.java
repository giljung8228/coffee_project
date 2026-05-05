package com.ecommerce.coffeeproject.domain.menu.service;

import com.ecommerce.coffeeproject.domain.menu.dto.CoffeeMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.dto.PopularMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.order.entity.OrderStatus;
import com.ecommerce.coffeeproject.domain.order.repository.CoffeeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoffeeMenuService {

    private final CoffeeMenuRepository coffeeMenuRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;

    public List<CoffeeMenuResponse> getMenus() {
        return coffeeMenuRepository.findByStatus(MenuStatus.ON_SALE)
                .stream()
                .map(CoffeeMenuResponse::from)
                .toList();
    }

    public List<PopularMenuResponse> getPopularMenus() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        return coffeeOrderRepository.findPopularMenus(
                OrderStatus.PAID,
                sevenDaysAgo,
                PageRequest.of(0, 3)
        );
    }
}