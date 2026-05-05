package com.ecommerce.coffeeproject.domain.order;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
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
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=off",
        "logging.level.org.hibernate.orm.jdbc.bind=off"
})
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CoffeeMenuRepository coffeeMenuRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private CoffeeOrderRepository coffeeOrderRepository;

    private Member member;
    private CoffeeMenu menu;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAllInBatch();
        coffeeOrderRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        coffeeMenuRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(new Member("동시성테스트사용자"));
        menu = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));

        Point point = new Point(member);
        point.charge(4500L);
        pointRepository.save(point);
    }

    @Test
    @DisplayName("같은 사용자가 동시에 주문하면 포인트는 중복 차감되지 않는다")
    void sameUserConcurrentOrderTest() throws InterruptedException {
        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    orderService.createOrder(new OrderCreateRequest(member.getId(), menu.getId()));
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

        executorService.shutdown();

        Point point = pointRepository.findAll().get(0);
        long orderCount = coffeeOrderRepository.count();

        System.out.println();
        System.out.println("========== 동시성 테스트 결과 ==========");
        System.out.println("동시 주문 요청 수       : " + threadCount);
        System.out.println("성공한 주문 수          : " + successCount.get());
        System.out.println("실패한 주문 수          : " + failCount.get());
        System.out.println("최종 포인트 잔액        : " + point.getBalance());
        System.out.println("DB에 저장된 주문 수     : " + orderCount);
        System.out.println("검증 결과              : 성공 1건, 실패 1건, 잔액 0P, 주문 1건이면 정상");
        System.out.println("======================================");
        System.out.println();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
        assertThat(point.getBalance()).isEqualTo(0L);
        assertThat(orderCount).isEqualTo(1);
    }
}