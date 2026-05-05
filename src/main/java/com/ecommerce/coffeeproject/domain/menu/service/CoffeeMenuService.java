package com.ecommerce.coffeeproject.domain.menu.service;

import com.ecommerce.coffeeproject.domain.menu.dto.CoffeeMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoffeeMenuService {

    private final CoffeeMenuRepository coffeeMenuRepository;

    public List<CoffeeMenuResponse> getMenus() {
        return coffeeMenuRepository.findByStatus(MenuStatus.ON_SALE)
                .stream()
                .map(CoffeeMenuResponse::from)
                .toList();
    }
}