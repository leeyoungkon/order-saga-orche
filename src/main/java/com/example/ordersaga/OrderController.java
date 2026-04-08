package com.example.ordersaga;

import com.example.ordersaga.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<Order> list() {
        return orderService.listOrders();
    }

    @GetMapping("/{orderId}")
    public Order get(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/events")
    public List<SagaEventLog> events() {
        return orderService.listEvents();
    }

    @GetMapping("/inventory/{productId}")
    public InventoryResponse inventory(@PathVariable String productId) {
        return orderService.getInventory(productId);
    }
}