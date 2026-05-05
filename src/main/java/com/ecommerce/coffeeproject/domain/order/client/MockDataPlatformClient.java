package com.ecommerce.coffeeproject.domain.order.client;

import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import org.springframework.stereotype.Component;

@Component
public class MockDataPlatformClient implements DataPlatformClient {

    @Override
    public void send(OrderDataPlatformPayload payload) {
        System.out.println("[MockDataPlatform] 주문 데이터 전송 완료: " + payload);
    }
}
