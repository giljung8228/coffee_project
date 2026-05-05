package com.ecommerce.coffeeproject.domain.menu.repository;

import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoffeeMenuRepository extends JpaRepository<CoffeeMenu, Long> {

    List<CoffeeMenu> findByStatus(MenuStatus status);
}
