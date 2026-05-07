package com.ecommerce.coffeeproject.domain.order.service;

import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateRequest;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DistributedLockOrderFacade {

    private static final String POINT_LOCK_PREFIX = "lock:point:";
    private static final long WAIT_TIME = 3L;
    private static final long LEASE_TIME = 5L;

    private final RedissonClient redissonClient;
    private final OrderService orderService;

    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        String lockKey = POINT_LOCK_PREFIX + request.userId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean available = false;

        try {
            available = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

            if (!available) {
                throw new IllegalStateException("주문 요청이 많아 잠시 후 다시 시도해주세요.");
            }

            return orderService.createOrderWithoutLock(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("분산락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (available && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}