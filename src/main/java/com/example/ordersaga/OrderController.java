package com.example.ordersaga;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderSagaService orderSagaService;
    private final StockClient stockClient;

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        return orderSagaService.createOrder(request);
    }

    @GetMapping
    public Collection<Order> list() {
        return orderSagaService.findAll();
    }

    @GetMapping("/events")
    public List<SagaEventLog> events() {
        return orderSagaService.findAllEvents();
    }

    @GetMapping("/inventory/{productId}")
    public QuantityResponse getInventory(@PathVariable String productId) {
        return stockClient.getQuantity(productId);
    }
}