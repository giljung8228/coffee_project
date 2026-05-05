package com.ecommerce.coffeeproject.domain.order.repository;

import com.ecommerce.coffeeproject.domain.menu.dto.PopularMenuResponse;
import com.ecommerce.coffeeproject.domain.order.entity.CoffeeOrder;
import com.ecommerce.coffeeproject.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CoffeeOrderRepository extends JpaRepository<CoffeeOrder, Long> {

    @Query("""
            select new com.ecommerce.coffeeproject.domain.menu.dto.PopularMenuResponse(
                menu.id,
                menu.name,
                menu.price,
                count(coffeeOrder.id)
            )
            from CoffeeOrder coffeeOrder
            join coffeeOrder.menu menu
            where coffeeOrder.status = :status
              and coffeeOrder.orderedAt >= :sevenDaysAgo
            group by menu.id, menu.name, menu.price
            order by count(coffeeOrder.id) desc, menu.id asc
            """)
    List<PopularMenuResponse> findPopularMenus(
            @Param("status") OrderStatus status,
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
            Pageable pageable
    );
}
