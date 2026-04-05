package com.example.ordersaga;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "stock-events", groupId = "order-service-group")
    public void consumeStockEvent(SagaEvent event) {
        log.info("[ORDER] stock event received = {}", event);
        orderSagaService.logIncomingEvent("STOCK_SERVICE", event);

        switch (event.getEventType()) {
            case "StockReserved" -> orderSagaService.markStockReserved(event.getOrderId());
            case "StockFailed" -> orderSagaService.cancelOrder(event.getOrderId(), event.getReason());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void consumePaymentEvent(SagaEvent event) {
        log.info("[ORDER] payment event received = {}", event);
        orderSagaService.logIncomingEvent("PAYMENT_SERVICE", event);

        switch (event.getEventType()) {
            case "PaymentCompleted" -> orderSagaService.markCompleted(event.getOrderId());
            case "PaymentFailed" -> orderSagaService.cancelOrder(event.getOrderId(), event.getReason());
        }
    }
}