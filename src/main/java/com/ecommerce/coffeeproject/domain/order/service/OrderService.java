package com.ecommerce.coffeeproject.domain.order.service;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import com.ecommerce.coffeeproject.domain.order.client.DataPlatformClient;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateRequest;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateResponse;
import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import com.ecommerce.coffeeproject.domain.order.entity.CoffeeOrder;
import com.ecommerce.coffeeproject.domain.order.repository.CoffeeOrderRepository;
import com.ecommerce.coffeeproject.domain.point.entity.Point;
import com.ecommerce.coffeeproject.domain.point.entity.PointHistory;
import com.ecommerce.coffeeproject.domain.point.repository.PointHistoryRepository;
import com.ecommerce.coffeeproject.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final MemberRepository memberRepository;
    private final CoffeeMenuRepository coffeeMenuRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;
    private final DataPlatformClient dataPlatformClient;

    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        return createOrder(request, LockStrategy.PESSIMISTIC);
    }

    public OrderCreateResponse createOrderWithOptimisticLock(OrderCreateRequest request) {
        return createOrder(request, LockStrategy.OPTIMISTIC);
    }

    private OrderCreateResponse createOrder(OrderCreateRequest request, LockStrategy lockStrategy) {
        Member member = memberRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        CoffeeMenu menu = coffeeMenuRepository.findById(request.menuId())
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        if (menu.getStatus() != MenuStatus.ON_SALE) {
            throw new IllegalArgumentException("현재 주문할 수 없는 메뉴입니다.");
        }

        Point point = findPoint(member.getId(), lockStrategy);

        point.use(menu.getPrice());

        CoffeeOrder order = CoffeeOrder.createPaid(
                member,
                menu,
                menu.getPrice()
        );

        coffeeOrderRepository.save(order);

        PointHistory pointHistory = PointHistory.createUse(
                member,
                point,
                menu.getPrice(),
                point.getBalance()
        );

        pointHistoryRepository.save(pointHistory);

        if (lockStrategy == LockStrategy.OPTIMISTIC) {
            pointRepository.flush();
        }

        dataPlatformClient.send(OrderDataPlatformPayload.from(order));

        return OrderCreateResponse.of(order, point.getBalance());
    }

    private Point findPoint(Long memberId, LockStrategy lockStrategy) {
        if (lockStrategy == LockStrategy.OPTIMISTIC) {
            return pointRepository.findByMemberIdWithOptimisticLock(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("포인트를 먼저 충전해주세요."));
        }

        return pointRepository.findByMemberIdWithLock(memberId)
                .orElseThrow(() -> new IllegalArgumentException("포인트를 먼저 충전해주세요."));
    }

    private enum LockStrategy {
        PESSIMISTIC,
        OPTIMISTIC
    }
}
