package com.ecommerce.coffeeproject.domain.order.client;

import com.ecommerce.coffeeproject.domain.order.dto.OrderDataPlatformPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.data-platform.kafka-listener.enabled", havingValue = "true", matchIfMissing = true)
public class OrderDataPlatformKafkaListener {

    @KafkaListener(topics = "${app.kafka.topics.order-created}")
    public void consume(
            OrderDataPlatformPayload payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info(
                "주문 데이터 Kafka 수신 완료. topic={}, partition={}, offset={}, payload={}",
                topic,
                partition,
                offset,
                payload
        );
    }
}