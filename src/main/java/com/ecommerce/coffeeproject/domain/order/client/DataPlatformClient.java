package com.ecommerce.coffeeproject.domain.order.client;

import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;

public interface DataPlatformClient {
    void send(OrderDataPlatformPayload payload);
}
