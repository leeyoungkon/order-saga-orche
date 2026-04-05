package com.example.ordersaga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String eventType;
    private String orderId;
    private String productId;
    private Integer quantity;
    private Integer amount;
    private String reason;
}