package com.ecommerce.coffeeproject;

import com.ecommerce.coffeeproject.domain.member.entity.Member;
import com.ecommerce.coffeeproject.domain.member.repository.MemberRepository;
import com.ecommerce.coffeeproject.domain.menu.dto.CoffeeMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.dto.PopularMenuResponse;
import com.ecommerce.coffeeproject.domain.menu.entity.CoffeeMenu;
import com.ecommerce.coffeeproject.domain.menu.entity.MenuStatus;
import com.ecommerce.coffeeproject.domain.menu.repository.CoffeeMenuRepository;
import com.ecommerce.coffeeproject.domain.menu.service.CoffeeMenuService;
import com.ecommerce.coffeeproject.domain.order.client.DataPlatformClient;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateRequest;
import com.ecommerce.coffeeproject.domain.order.dto.OrderCreateResponse;
import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import com.ecommerce.coffeeproject.domain.order.repository.CoffeeOrderRepository;
import com.ecommerce.coffeeproject.domain.order.service.OrderService;
import com.ecommerce.coffeeproject.domain.point.dto.PointChargeRequest;
import com.ecommerce.coffeeproject.domain.point.dto.PointChargeResponse;
import com.ecommerce.coffeeproject.domain.point.entity.Point;
import com.ecommerce.coffeeproject.domain.point.entity.PointHistory;
import com.ecommerce.coffeeproject.domain.point.entity.PointHistoryType;
import com.ecommerce.coffeeproject.domain.point.repository.PointHistoryRepository;
import com.ecommerce.coffeeproject.domain.point.repository.PointRepository;
import com.ecommerce.coffeeproject.domain.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.main.banner-mode=off",
        "spring.jpa.show-sql=false",
        "logging.level.root=warn",
        "logging.level.org.hibernate.SQL=off",
        "logging.level.org.hibernate.orm.jdbc.bind=off",
        "logging.level.org.springframework=warn",
        "logging.level.com.zaxxer.hikari=warn"
})
class CoffeeProjectRequirementTest {

    @Autowired
    private CoffeeMenuService coffeeMenuService;

    @Autowired
    private PointService pointService;

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

    @Autowired
    private FakeDataPlatformClient fakeDataPlatformClient;

    @BeforeEach
    void setUp() {
        fakeDataPlatformClient.clear();

        pointHistoryRepository.deleteAllInBatch();
        coffeeOrderRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        coffeeMenuRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("커피 메뉴 목록 조회 - 판매중인 메뉴의 메뉴ID, 이름, 가격을 조회한다")
    void getMenus() {
        CoffeeMenu americano = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));
        CoffeeMenu latte = coffeeMenuRepository.save(new CoffeeMenu("카페라떼", 5000L, MenuStatus.ON_SALE));
        coffeeMenuRepository.save(new CoffeeMenu("콜드브루", 5500L, MenuStatus.HIDDEN));
        coffeeMenuRepository.save(new CoffeeMenu("카라멜마끼아또", 6000L, MenuStatus.SOLD_OUT));

        List<CoffeeMenuResponse> responses = coffeeMenuService.getMenus();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(CoffeeMenuResponse::menuId)
                .containsExactlyInAnyOrder(americano.getId(), latte.getId());
        assertThat(responses)
                .extracting(CoffeeMenuResponse::name)
                .containsExactlyInAnyOrder("아메리카노", "카페라떼");
        assertThat(responses)
                .extracting(CoffeeMenuResponse::price)
                .containsExactlyInAnyOrder(4500L, 5000L);

        printTestResult(
                "커피 메뉴 목록 조회",
                List.of(
                        "ON_SALE 메뉴 2개 저장",
                        "HIDDEN 메뉴 1개 저장",
                        "SOLD_OUT 메뉴 1개 저장"
                ),
                "coffeeMenuService.getMenus() 실행",
                List.of(
                        "조회된 메뉴 수 = " + responses.size(),
                        "조회된 메뉴 = " + menuNames(responses),
                        "검증 결과 = 판매중 메뉴만 조회됨"
                )
        );
    }

    @Test
    @DisplayName("포인트 충전 - 사용자 식별값과 충전금액을 입력받아 포인트를 충전한다")
    void chargePoint() {
        Member member = memberRepository.save(new Member("테스트사용자"));

        PointChargeResponse response = pointService.chargePoint(
                new PointChargeRequest(member.getId(), 10000L)
        );

        Point point = pointRepository.findAll().get(0);
        PointHistory history = pointHistoryRepository.findAll().get(0);

        assertThat(response.userId()).isEqualTo(member.getId());
        assertThat(response.chargedAmount()).isEqualTo(10000L);
        assertThat(response.balance()).isEqualTo(10000L);

        assertThat(point.getBalance()).isEqualTo(10000L);

        assertThat(history.getType()).isEqualTo(PointHistoryType.CHARGE);
        assertThat(history.getAmount()).isEqualTo(10000L);
        assertThat(history.getBalanceAfter()).isEqualTo(10000L);

        printTestResult(
                "포인트 충전",
                List.of(
                        "사용자 생성 = " + member.getId(),
                        "충전 요청 금액 = 10000P"
                ),
                "pointService.chargePoint() 실행",
                List.of(
                        "응답 충전 금액 = " + response.chargedAmount() + "P",
                        "응답 잔액 = " + response.balance() + "P",
                        "DB 포인트 잔액 = " + point.getBalance() + "P",
                        "포인트 이력 타입 = " + history.getType(),
                        "포인트 이력 금액 = " + history.getAmount() + "P",
                        "검증 결과 = 포인트 충전 및 충전 이력 저장 성공"
                )
        );
    }

    @Test
    @DisplayName("커피 주문/결제 - 사용자 식별값과 메뉴 ID로 주문하고 포인트를 차감한다")
    void createOrder() {
        Member member = memberRepository.save(new Member("테스트사용자"));
        CoffeeMenu menu = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));

        Point point = new Point(member);
        point.charge(10000L);
        pointRepository.save(point);

        OrderCreateResponse response = orderService.createOrder(
                new OrderCreateRequest(member.getId(), menu.getId())
        );

        Point savedPoint = pointRepository.findAll().get(0);
        List<PointHistory> histories = pointHistoryRepository.findAll();

        assertThat(response.userId()).isEqualTo(member.getId());
        assertThat(response.menuId()).isEqualTo(menu.getId());
        assertThat(response.menuName()).isEqualTo("아메리카노");
        assertThat(response.paymentAmount()).isEqualTo(4500L);
        assertThat(response.balance()).isEqualTo(5500L);
        assertThat(response.orderStatus()).isEqualTo("PAID");

        assertThat(savedPoint.getBalance()).isEqualTo(5500L);
        assertThat(coffeeOrderRepository.count()).isEqualTo(1);

        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getType()).isEqualTo(PointHistoryType.USE);
        assertThat(histories.get(0).getAmount()).isEqualTo(4500L);
        assertThat(histories.get(0).getBalanceAfter()).isEqualTo(5500L);

        printTestResult(
                "커피 주문/결제 성공",
                List.of(
                        "사용자 포인트 = 10000P",
                        "주문 메뉴 = 아메리카노",
                        "메뉴 가격 = 4500원"
                ),
                "orderService.createOrder() 실행",
                List.of(
                        "주문 상태 = " + response.orderStatus(),
                        "결제 금액 = " + response.paymentAmount() + "P",
                        "남은 포인트 = " + response.balance() + "P",
                        "DB 저장 주문 수 = " + coffeeOrderRepository.count(),
                        "포인트 이력 타입 = " + histories.get(0).getType(),
                        "포인트 이력 잔액 = " + histories.get(0).getBalanceAfter() + "P",
                        "검증 결과 = 포인트 차감, 주문 저장, 사용 이력 저장 성공"
                )
        );
    }

    @Test
    @DisplayName("커피 주문/결제 - 주문 성공 시 데이터 수집 플랫폼으로 주문 정보를 전송한다")
    void sendOrderDataToDataPlatform() {
        Member member = memberRepository.save(new Member("테스트사용자"));
        CoffeeMenu menu = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));

        Point point = new Point(member);
        point.charge(10000L);
        pointRepository.save(point);

        orderService.createOrder(new OrderCreateRequest(member.getId(), menu.getId()));

        List<OrderDataPlatformPayload> payloads = fakeDataPlatformClient.getPayloads();

        assertThat(payloads).hasSize(1);
        assertThat(payloads.get(0).userId()).isEqualTo(member.getId());
        assertThat(payloads.get(0).menuId()).isEqualTo(menu.getId());
        assertThat(payloads.get(0).paymentAmount()).isEqualTo(4500L);

        printTestResult(
                "주문 데이터 수집 플랫폼 전송",
                List.of(
                        "사용자 포인트 = 10000P",
                        "주문 메뉴 = 아메리카노",
                        "MockDataPlatformClient 사용"
                ),
                "주문 성공 후 DataPlatformClient.send() 호출",
                List.of(
                        "전송 데이터 수 = " + payloads.size(),
                        "전송 userId = " + payloads.get(0).userId(),
                        "전송 menuId = " + payloads.get(0).menuId(),
                        "전송 paymentAmount = " + payloads.get(0).paymentAmount(),
                        "검증 결과 = 주문 성공 시 데이터 플랫폼 전송 성공"
                )
        );
    }

    @Test
    @DisplayName("커피 주문/결제 - 포인트가 부족하면 주문이 생성되지 않는다")
    void createOrderFailByInsufficientPoint() {
        Member member = memberRepository.save(new Member("테스트사용자"));
        CoffeeMenu menu = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));

        Point point = new Point(member);
        point.charge(1000L);
        pointRepository.save(point);

        Throwable throwable = catchThrowable(() ->
                orderService.createOrder(new OrderCreateRequest(member.getId(), menu.getId()))
        );

        Point savedPoint = pointRepository.findAll().get(0);

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");

        assertThat(savedPoint.getBalance()).isEqualTo(1000L);
        assertThat(coffeeOrderRepository.count()).isEqualTo(0);
        assertThat(pointHistoryRepository.count()).isEqualTo(0);
        assertThat(fakeDataPlatformClient.getPayloads()).isEmpty();

        printTestResult(
                "포인트 부족 주문 실패",
                List.of(
                        "사용자 포인트 = 1000P",
                        "주문 메뉴 가격 = 4500원"
                ),
                "orderService.createOrder() 실행",
                List.of(
                        "발생 예외 = " + throwable.getMessage(),
                        "남은 포인트 = " + savedPoint.getBalance() + "P",
                        "DB 저장 주문 수 = " + coffeeOrderRepository.count(),
                        "포인트 사용 이력 수 = " + pointHistoryRepository.count(),
                        "데이터 플랫폼 전송 수 = " + fakeDataPlatformClient.getPayloads().size(),
                        "검증 결과 = 포인트 부족 시 주문, 이력, 전송 모두 생성되지 않음"
                )
        );
    }

    @Test
    @DisplayName("커피 주문/결제 - 판매중이 아닌 메뉴는 주문할 수 없다")
    void createOrderFailByNotOnSaleMenu() {
        Member member = memberRepository.save(new Member("테스트사용자"));
        CoffeeMenu menu = coffeeMenuRepository.save(new CoffeeMenu("품절메뉴", 4500L, MenuStatus.SOLD_OUT));

        Point point = new Point(member);
        point.charge(10000L);
        pointRepository.save(point);

        Throwable throwable = catchThrowable(() ->
                orderService.createOrder(new OrderCreateRequest(member.getId(), menu.getId()))
        );

        Point savedPoint = pointRepository.findAll().get(0);

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 주문할 수 없는 메뉴입니다.");

        assertThat(savedPoint.getBalance()).isEqualTo(10000L);
        assertThat(coffeeOrderRepository.count()).isEqualTo(0);
        assertThat(pointHistoryRepository.count()).isEqualTo(0);
        assertThat(fakeDataPlatformClient.getPayloads()).isEmpty();

        printTestResult(
                "판매중이 아닌 메뉴 주문 실패",
                List.of(
                        "사용자 포인트 = 10000P",
                        "메뉴 상태 = " + menu.getStatus()
                ),
                "orderService.createOrder() 실행",
                List.of(
                        "발생 예외 = " + throwable.getMessage(),
                        "남은 포인트 = " + savedPoint.getBalance() + "P",
                        "DB 저장 주문 수 = " + coffeeOrderRepository.count(),
                        "포인트 사용 이력 수 = " + pointHistoryRepository.count(),
                        "데이터 플랫폼 전송 수 = " + fakeDataPlatformClient.getPayloads().size(),
                        "검증 결과 = 판매중이 아닌 메뉴는 주문되지 않음"
                )
        );
    }

    @Test
    @DisplayName("인기 메뉴 목록 조회 - 최근 7일간 주문 수가 많은 메뉴 3개를 조회한다")
    void getPopularMenus() {
        Member member = memberRepository.save(new Member("테스트사용자"));

        CoffeeMenu americano = coffeeMenuRepository.save(new CoffeeMenu("아메리카노", 4500L, MenuStatus.ON_SALE));
        CoffeeMenu latte = coffeeMenuRepository.save(new CoffeeMenu("카페라떼", 5000L, MenuStatus.ON_SALE));
        CoffeeMenu vanillaLatte = coffeeMenuRepository.save(new CoffeeMenu("바닐라라떼", 5500L, MenuStatus.ON_SALE));
        CoffeeMenu coldBrew = coffeeMenuRepository.save(new CoffeeMenu("콜드브루", 6000L, MenuStatus.ON_SALE));

        Point point = new Point(member);
        point.charge(100000L);
        pointRepository.save(point);

        orderMany(member, americano, 3);
        orderMany(member, latte, 2);
        orderMany(member, vanillaLatte, 1);
        orderMany(member, coldBrew, 4);

        List<PopularMenuResponse> responses = coffeeMenuService.getPopularMenus();

        assertThat(responses).hasSize(3);

        assertThat(responses.get(0).menuId()).isEqualTo(coldBrew.getId());
        assertThat(responses.get(0).name()).isEqualTo("콜드브루");
        assertThat(responses.get(0).orderCount()).isEqualTo(4L);

        assertThat(responses.get(1).menuId()).isEqualTo(americano.getId());
        assertThat(responses.get(1).name()).isEqualTo("아메리카노");
        assertThat(responses.get(1).orderCount()).isEqualTo(3L);

        assertThat(responses.get(2).menuId()).isEqualTo(latte.getId());
        assertThat(responses.get(2).name()).isEqualTo("카페라떼");
        assertThat(responses.get(2).orderCount()).isEqualTo(2L);

        printTestResult(
                "인기 메뉴 목록 조회",
                List.of(
                        "콜드브루 주문 = 4건",
                        "아메리카노 주문 = 3건",
                        "카페라떼 주문 = 2건",
                        "바닐라라떼 주문 = 1건"
                ),
                "coffeeMenuService.getPopularMenus() 실행",
                List.of(
                        "조회된 인기 메뉴 수 = " + responses.size(),
                        "인기 메뉴 결과 = " + popularMenuResults(responses),
                        "검증 결과 = 최근 7일 주문 수 기준 Top 3 조회 성공"
                )
        );
    }

    private void orderMany(Member member, CoffeeMenu menu, int count) {
        for (int i = 0; i < count; i++) {
            orderService.createOrder(new OrderCreateRequest(member.getId(), menu.getId()));
        }
    }

    private String menuNames(List<CoffeeMenuResponse> responses) {
        return responses.stream()
                .map(CoffeeMenuResponse::name)
                .toList()
                .toString();
    }

    private String popularMenuResults(List<PopularMenuResponse> responses) {
        return responses.stream()
                .map(response -> response.name() + " " + response.orderCount() + "건")
                .toList()
                .toString();
    }

    private void printTestResult(
            String testName,
            List<String> given,
            String when,
            List<String> then
    ) {
        System.out.println();
        System.out.println("========== 요구사항 테스트 결과 ==========");
        System.out.println("테스트명 : " + testName);

        System.out.println("[준비]");
        given.forEach(value -> System.out.println("- " + value));

        System.out.println("[실행]");
        System.out.println("- " + when);

        System.out.println("[결과]");
        then.forEach(value -> System.out.println("- " + value));

        System.out.println("========================================");
    }

    @TestConfiguration
    static class FakeDataPlatformTestConfig {

        @Bean
        @Primary
        FakeDataPlatformClient fakeDataPlatformClient() {
            return new FakeDataPlatformClient();
        }
    }

    static class FakeDataPlatformClient implements DataPlatformClient {

        private final List<OrderDataPlatformPayload> payloads = new CopyOnWriteArrayList<>();

        @Override
        public void send(OrderDataPlatformPayload payload) {
            payloads.add(payload);
        }

        public List<OrderDataPlatformPayload> getPayloads() {
            return payloads;
        }

        public void clear() {
            payloads.clear();
        }
    }
}