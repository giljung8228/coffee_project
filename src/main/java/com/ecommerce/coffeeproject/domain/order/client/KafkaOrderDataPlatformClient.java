package com.ecommerce.coffeeproject.domain.order.client;

import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-platform.client", havingValue = "kafka", matchIfMissing = true)
public class KafkaOrderDataPlatformClient implements DataPlatformClient {

    private final KafkaTemplate<String, OrderDataPlatformPayload> kafkaTemplate;

    @Value("${app.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Override
    public void send(OrderDataPlatformPayload payload) {
        String key = String.valueOf(payload.userId());

        kafkaTemplate.send(orderCreatedTopic, key, payload)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("주문 데이터 Kafka 전송 실패. topic={}, key={}, payload={}", orderCreatedTopic, key, payload, exception);
                        return;
                    }

                    log.info(
                            "주문 데이터 Kafka 전송 완료. topic={}, partition={}, offset={}, key={}, payload={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key,
                            payload
                    );
                });
    }
}
