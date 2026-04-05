package com.example.ordersaga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final SagaEventLogRepository sagaEventLogRepository;

    public Order createOrder(CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        Order order = new Order(
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getAmount(),
                OrderStatus.CREATED
        );
        orderRepository.save(order);

        SagaEvent event = new SagaEvent(
                "OrderCreated",
                order.getOrderId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                null
        );
        sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", event));
        orderProducer.publishOrderCreated(event);
        return order;
    }

    public void markStockReserved(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.STOCK_RESERVED);
            orderRepository.save(order);
            
            SagaEvent event = new SagaEvent(
                    "StockReserved",
                    orderId,
                    order.getProductId(),
                    order.getQuantity(),
                    order.getAmount(),
                    null
            );
            sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", event));
        }
    }

    public void markCompleted(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            
            SagaEvent event = new SagaEvent(
                    "OrderCompleted",
                    orderId,
                    order.getProductId(),
                    order.getQuantity(),
                    order.getAmount(),
                    null
            );
            sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", event));
        }
    }

    public void cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            SagaEvent cancelEvent = new SagaEvent(
                    "OrderCancelled",
                    order.getOrderId(),
                    order.getProductId(),
                    order.getQuantity(),
                    order.getAmount(),
                    reason
            );
            sagaEventLogRepository.save(SagaEventLog.of("ORDER_SERVICE", cancelEvent));
            orderProducer.publishOrderCancelled(cancelEvent);
        }
    }

    public void logIncomingEvent(String source, SagaEvent event) {
        sagaEventLogRepository.save(SagaEventLog.of(source, event));
    }

    public Collection<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<SagaEventLog> findAllEvents() {
        List<SagaEventLog> events = sagaEventLogRepository.findAllByOrderByCreatedAtDesc();
        boolean hasUpdates = false;

        for (SagaEventLog event : events) {
            if (isBlank(event.getSource())) {
                String inferredSource = inferSourceByEventType(event.getEventType());
                event.setSource(inferredSource);
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            sagaEventLogRepository.saveAll(events);
        }

        return events;
    }

    private String inferSourceByEventType(String eventType) {
        if (eventType == null) {
            return "UNKNOWN_SERVICE";
        }
        if (eventType.startsWith("Stock")) {
            return "STOCK_SERVICE";
        }
        if (eventType.startsWith("Payment")) {
            return "PAYMENT_SERVICE";
        }
        if (eventType.startsWith("Order")) {
            return "ORDER_SERVICE";
        }
        return "UNKNOWN_SERVICE";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}