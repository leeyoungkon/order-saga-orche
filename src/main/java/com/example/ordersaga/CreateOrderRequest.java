package com.example.ordersaga;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String productId;
    private Integer quantity;
    private Integer amount;
}