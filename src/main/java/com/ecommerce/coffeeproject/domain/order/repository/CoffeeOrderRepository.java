package com.ecommerce.coffeeproject.domain.order.repository;

import com.ecommerce.coffeeproject.domain.order.entity.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeOrderRepository extends JpaRepository<CoffeeOrder, Long> {
}
