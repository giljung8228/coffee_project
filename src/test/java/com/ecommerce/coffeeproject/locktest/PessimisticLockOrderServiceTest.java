package com.ecommerce.coffeeproject.locktest;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import com.ecommerce.coffeeproject.domain.order.client.DataPlatformClient;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateRequest;
import com.ecommerce.coffeeproject.domain.order.repository.CoffeeOrderRepository;
import com.ecommerce.coffeeproject.domain.order.service.OrderService;
import com.ecommerce.coffeeproject.domain.point.entity.Point;
import com.ecommerce.coffeeproject.domain.point.repository.PointHistoryRepository;
import com.ecommerce.coffeeproject.domain.point.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("비관적 락 동시성 테스트")
class PessimisticLockOrderServiceTest {

    @MockitoBean
    private DataPlatformClient dataPlatformClient;

    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final CoffeeMenuRepository coffeeMenuRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;

    private Long memberId;
    private Long menuId;

    @Autowired
    PessimisticLockOrderServiceTest(
            OrderService orderService,
            MemberRepository memberRepository,
            CoffeeMenuRepository coffeeMenuRepository,
            PointRepository pointRepository,
            PointHistoryRepository pointHistoryRepository,
            CoffeeOrderRepository coffeeOrderRepository
    ) {
        this.orderService = orderService;
        this.memberRepository = memberRepository;
        this.coffeeMenuRepository = coffeeMenuRepository;
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
    }

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAllInBatch();
        coffeeOrderRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        coffeeMenuRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        Member member = memberRepository.save(
                new Member("test-user")
        );

        CoffeeMenu menu = coffeeMenuRepository.save(
                new CoffeeMenu("아메리카노", 1000L, MenuStatus.ON_SALE)
        );

        Point point = new Point(member);
        point.charge(10_000L);
        pointRepository.save(point);

        memberId = member.getId();
        menuId = menu.getId();
    }

    @Test
    void 동시에_20번_주문해도_포인트는_정확히_10번만_차감된다() throws InterruptedException {
        int requestCount = 20;
        long initialPoint = 10_000L;
        long menuPrice = 1_000L;

        int expectedSuccessCount = (int) (initialPoint / menuPrice);
        int expectedFailCount = requestCount - expectedSuccessCount;
        long expectedOrderCount = expectedSuccessCount;
        long expectedFinalPoint = initialPoint - (expectedSuccessCount * menuPrice);

        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        printScenario(
                requestCount,
                initialPoint,
                menuPrice,
                expectedSuccessCount,
                expectedFailCount,
                expectedOrderCount,
                expectedFinalPoint
        );

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    orderService.createOrder(new OrderCreateRequest(memberId, menuId));

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        long elapsedTimeMs = System.currentTimeMillis() - startTime;

        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        long actualOrderCount = coffeeOrderRepository.count();
        long actualFinalPoint = point.getBalance();

        printResult(
                requestCount,
                initialPoint,
                menuPrice,
                expectedSuccessCount,
                successCount.get(),
                expectedFailCount,
                failCount.get(),
                expectedOrderCount,
                actualOrderCount,
                expectedFinalPoint,
                actualFinalPoint,
                elapsedTimeMs
        );

        assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        assertThat(failCount.get()).isEqualTo(expectedFailCount);
        assertThat(actualOrderCount).isEqualTo(expectedOrderCount);
        assertThat(actualFinalPoint).isEqualTo(expectedFinalPoint);
    }

    private void printScenario(
            int requestCount,
            long initialPoint,
            long menuPrice,
            int expectedSuccessCount,
            int expectedFailCount,
            long expectedOrderCount,
            long expectedFinalPoint
    ) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("비관적 락 동시성 테스트 시나리오");
        System.out.println("========================================");
        System.out.println("테스트 대상       : 포인트 차감 주문 로직");
        System.out.println("락 방식           : 비관적 락 PESSIMISTIC_WRITE");
        System.out.println("동시 요청 수      : " + requestCount);
        System.out.println("초기 포인트       : " + initialPoint + "P");
        System.out.println("메뉴 가격         : " + menuPrice + "P");
        System.out.println("----------------------------------------");
        System.out.println("예상 성공 수      : " + expectedSuccessCount);
        System.out.println("예상 실패 수      : " + expectedFailCount);
        System.out.println("예상 주문 수      : " + expectedOrderCount);
        System.out.println("예상 최종 포인트  : " + expectedFinalPoint + "P");
        System.out.println("========================================");
        System.out.println();
    }

    private void printResult(
            int requestCount,
            long initialPoint,
            long menuPrice,
            int expectedSuccessCount,
            int actualSuccessCount,
            int expectedFailCount,
            int actualFailCount,
            long expectedOrderCount,
            long actualOrderCount,
            long expectedFinalPoint,
            long actualFinalPoint,
            long elapsedTimeMs
    ) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("비관적 락 동시성 테스트 결과");
        System.out.println("========================================");
        System.out.println("총 요청 수        : " + requestCount);
        System.out.println("초기 포인트       : " + initialPoint + "P");
        System.out.println("메뉴 가격         : " + menuPrice + "P");
        System.out.println("실행 시간         : " + elapsedTimeMs + "ms");
        System.out.println("----------------------------------------");
        System.out.println("예상 성공 수      : " + expectedSuccessCount);
        System.out.println("실제 성공 수      : " + actualSuccessCount);
        System.out.println("성공 수 검증      : " + toResultText(expectedSuccessCount == actualSuccessCount));
        System.out.println("----------------------------------------");
        System.out.println("예상 실패 수      : " + expectedFailCount);
        System.out.println("실제 실패 수      : " + actualFailCount);
        System.out.println("실패 수 검증      : " + toResultText(expectedFailCount == actualFailCount));
        System.out.println("----------------------------------------");
        System.out.println("예상 주문 수      : " + expectedOrderCount);
        System.out.println("실제 주문 수      : " + actualOrderCount);
        System.out.println("주문 수 검증      : " + toResultText(expectedOrderCount == actualOrderCount));
        System.out.println("----------------------------------------");
        System.out.println("예상 최종 포인트  : " + expectedFinalPoint + "P");
        System.out.println("실제 최종 포인트  : " + actualFinalPoint + "P");
        System.out.println("포인트 검증       : " + toResultText(expectedFinalPoint == actualFinalPoint));
        System.out.println("========================================");
        System.out.println();
    }

    private String toResultText(boolean matched) {
        if (matched) {
            return "통과";
        }

        return "실패";
    }
}