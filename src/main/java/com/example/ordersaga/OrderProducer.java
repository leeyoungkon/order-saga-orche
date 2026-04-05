package com.example.ordersaga;

import com.example.ordersaga.SagaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;

    public void publishOrderCreated(SagaEvent event) {
        kafkaTemplate.send("order-events", event.getOrderId(), event);
    }

    public void publishOrderCancelled(SagaEvent event) {
        kafkaTemplate.send("order-events", event.getOrderId(), event);
    }
}