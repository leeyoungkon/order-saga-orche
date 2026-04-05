package com.example.ordersaga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders_tbl")
public class Order {
    @Id
    private String orderId;
    private String productId;
    private Integer quantity;
    private Integer amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}