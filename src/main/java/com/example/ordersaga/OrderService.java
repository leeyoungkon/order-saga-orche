package com.example.ordersaga;

import com.example.ordersaga.SagaMessage;
import com.example.ordersaga.OrderRequest;
import com.example.ordersaga.Order;
import com.example.ordersaga.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SagaEventLogRepository sagaEventLogRepository;
    private final KafkaTemplate<String, SagaMessage> kafkaTemplate;
    private final RestClient stockRestClient;

    public OrderService(OrderRepository orderRepository,
                        SagaEventLogRepository sagaEventLogRepository,
                        KafkaTemplate<String, SagaMessage> kafkaTemplate,
                        RestClient stockRestClient) {
        this.orderRepository = orderRepository;
        this.sagaEventLogRepository = sagaEventLogRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.stockRestClient = stockRestClient;
    }

    public Order createOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        String sagaId = UUID.randomUUID().toString();

        Order order = new Order(
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getAmount(),
                "PENDING"
        );
        orderRepository.save(order);
            recordEvent("ORDER_SERVICE", "order-api", eventForOrder(order, sagaId, "OrderPending", null), order.getStatus());

        SagaMessage event = new SagaMessage(
                sagaId,
                "OrderCreated",
                orderId,
                request.getProductId(),
                request.getQuantity(),
                request.getAmount(),
                null
        );

        kafkaTemplate.send("order-events", orderId, event);
        return order;
    }

    public void completeOrder(String orderId) {
        updateOrderStatus(orderId, "COMPLETED", "OrderCompleted", null);
    }

    public void failOrder(String orderId) {
        updateOrderStatus(orderId, "FAILED", "OrderFailed", null);
    }

    public void markStockReserved(String orderId) {
        updateOrderStatus(orderId, "STOCK_RESERVED", "StockReserved", null);
    }

    public void markPaymentApproved(String orderId) {
        updateOrderStatus(orderId, "PAYMENT_APPROVED", "PaymentCompleted", null);
    }

    public void recordTopicEvent(String topic, SagaMessage message) {
        if (message.getOrderId() != null) {
            syncOrderStatusFromMessage(message);
        }
        String status = message.getOrderId() == null ? null : orderRepository.findById(message.getOrderId())
                .map(Order::getStatus)
                .orElse(null);
        recordEvent(inferSource(topic, message.getEventType()), topic, message, status);
    }

    public List<Order> listOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"));
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow();
    }

    public List<SagaEventLog> listEvents() {
        return sagaEventLogRepository.findTop200ByOrderByCreatedAtDesc();
    }

    public InventoryResponse getInventory(String productId) {
        return stockRestClient.get()
                .uri("/api/inventory/{productId}", productId)
                .retrieve()
                .body(InventoryResponse.class);
    }

    private void syncOrderStatusFromMessage(SagaMessage message) {
        String eventType = message.getEventType();
        if ("StockReserved".equals(eventType)) {
            updateOrderStatus(message.getOrderId(), "STOCK_RESERVED", null, null);
        } else if ("PaymentCompleted".equals(eventType)) {
            updateOrderStatus(message.getOrderId(), "PAYMENT_APPROVED", null, null);
        } else if ("StockFailed".equals(eventType) || "PaymentFailed".equals(eventType)
                || "FailOrderCommand".equals(eventType)) {
            updateOrderStatus(message.getOrderId(), "FAILED", null, message.getReason());
        } else if ("CompleteOrderCommand".equals(eventType)) {
            updateOrderStatus(message.getOrderId(), "COMPLETED", null, null);
        }
    }

    private void updateOrderStatus(String orderId, String status, String localEventType, String reason) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }

        if (!status.equals(order.getStatus())) {
            order.setStatus(status);
            orderRepository.save(order);
        }

        if (localEventType != null) {
            recordEvent(
                    "ORDER_SERVICE",
                    "order-service",
                    eventForOrder(order, UUID.randomUUID().toString(), localEventType, reason),
                    order.getStatus()
            );
        }
    }

    private SagaMessage eventForOrder(Order order, String sagaId, String eventType, String reason) {
        return new SagaMessage(
                sagaId,
                eventType,
                order.getOrderId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                reason
        );
    }

    private void recordEvent(String source, String topic, SagaMessage message, String status) {
        sagaEventLogRepository.save(new SagaEventLog(source, topic, message, status));
    }

    private String inferSource(String topic, String eventType) {
        if (topic == null) {
            return "UNKNOWN_SERVICE";
        }
        if (topic.startsWith("stock")) {
            return "STOCK_SERVICE";
        }
        if (topic.startsWith("payment")) {
            return "PAYMENT_SERVICE";
        }
        if (topic.startsWith("order")) {
            if ("CompleteOrderCommand".equals(eventType) || "FailOrderCommand".equals(eventType)) {
                return "ORCHESTRATOR";
            }
            return "ORDER_SERVICE";
        }
        return "UNKNOWN_SERVICE";
    }
}