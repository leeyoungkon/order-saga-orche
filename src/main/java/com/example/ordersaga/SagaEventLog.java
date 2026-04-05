package com.example.ordersaga;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "saga_event_logs")
public class SagaEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;
    private String eventType;
    private String orderId;
    private String productId;
    private Integer quantity;
    private Integer amount;
    private String reason;
    private LocalDateTime createdAt;

    public static SagaEventLog of(String source, SagaEvent event) {
        return new SagaEventLog(
                null,
                source,
                event.getEventType(),
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getAmount(),
                event.getReason(),
                LocalDateTime.now()
        );
    }
}
