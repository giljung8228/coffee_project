package com.ecommerce.coffeeproject.domain.order.client;

import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.data-platform.client", havingValue = "mock")
public class MockDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(OrderDataPlatformPayload payload) {
        System.out.println("[MockDataPlatform] 주문 데이터 전송 완료: " + payload);
    }
}
