package com.example.ordersaga;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCommandConsumer {

    private final OrderService orderService;

    public OrderCommandConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "order-events", groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderEvent(SagaMessage message) {
        orderService.recordTopicEvent("order-events", message);
    }

    @KafkaListener(topics = "stock-events", groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeStockEvent(SagaMessage message) {
        orderService.recordTopicEvent("stock-events", message);
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvent(SagaMessage message) {
        orderService.recordTopicEvent("payment-events", message);
    }

    @KafkaListener(topics = "order-commands", groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(SagaMessage message) {
        orderService.recordTopicEvent("order-commands", message);
        if ("CompleteOrderCommand".equals(message.getEventType())) {
            orderService.completeOrder(message.getOrderId());
        } else if ("FailOrderCommand".equals(message.getEventType())) {
            orderService.failOrder(message.getOrderId());
        }
    }
}