package com.example.ordersaga;


import java.math.BigDecimal;

public class OrderRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal amount;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}