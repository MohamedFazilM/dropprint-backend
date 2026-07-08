package com.dropprint.project.controller;

import com.dropprint.project.dto.OrderRequestDTO;
import com.dropprint.project.model.Order;
import com.dropprint.project.repository.OrderRepository;
import com.dropprint.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public Order createOrder(@jakarta.validation.Valid @RequestBody OrderRequestDTO dto) {
        return orderService.createOrder(dto);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id, jakarta.servlet.http.HttpServletRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String reqCustomerId = (String) request.getAttribute("req_customer_id");
        if (reqCustomerId != null) {
            if (order.getCustomer() == null || !order.getCustomer().getId().equals(reqCustomerId)) {
                throw new RuntimeException("Access denied. You do not own this order.");
            }
        }
        return order;
    }

    @GetMapping("/track")
    public Order trackOrder(@RequestParam String orderId, @RequestParam String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found. Please check your Order ID."));

        if (order.getCustomer() == null || !order.getCustomer().getPhone().equals(phone)) {
            throw new RuntimeException("Phone number does not match this order.");
        }

        return order;
    }
}