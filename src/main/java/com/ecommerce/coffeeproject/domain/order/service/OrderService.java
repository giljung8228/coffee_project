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
import com.ecommerce.coffeeproject.global.exception.BusinessException;
import com.ecommerce.coffeeproject.global.exception.domain.MemberErrorCode;
import com.ecommerce.coffeeproject.global.exception.domain.MenuErrorCode;
import com.ecommerce.coffeeproject.global.exception.domain.PointErrorCode;
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
        Member member = memberRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.USER_NOT_FOUND));

        CoffeeMenu menu = coffeeMenuRepository.findById(request.menuId())
                .orElseThrow(() -> new BusinessException(MenuErrorCode.MENU_NOT_FOUND));

        if (menu.getStatus() != MenuStatus.ON_SALE) {
            throw new BusinessException(MenuErrorCode.MENU_NOT_ON_SALE);
        }

        Point point = pointRepository.findByMemberIdWithLock(member.getId())
                .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));

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

        dataPlatformClient.send(OrderDataPlatformPayload.from(order));

        return OrderCreateResponse.of(order, point.getBalance());
    }
}