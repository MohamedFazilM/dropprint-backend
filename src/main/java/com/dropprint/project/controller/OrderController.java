package com.dropprint.project.controller;

import com.dropprint.project.dto.OrderRequestDTO;
import com.dropprint.project.model.Order;
import com.dropprint.project.repository.OrderRepository;
import com.dropprint.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @GetMapping("/my-orders")
    public List<Order> getMyOrders(jakarta.servlet.http.HttpServletRequest request) {
        String reqCustomerEmail = (String) request.getAttribute("req_customer_email");
        if (reqCustomerEmail == null) {
            throw new RuntimeException("Unauthorized. Session invalid or missing.");
        }
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(reqCustomerEmail);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id, jakarta.servlet.http.HttpServletRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Admin bypass
        if (request.getAttribute("is_admin_bypass") != null) {
            return order;
        }

        String reqCustomerEmail = (String) request.getAttribute("req_customer_email");
        if (reqCustomerEmail != null) {
            if (order.getCustomer() == null || !order.getCustomer().getEmail().equalsIgnoreCase(reqCustomerEmail)) {
                throw new RuntimeException("Access denied. You do not own this order.");
                
            }
        }
        return order;
    }

    @GetMapping("/track")
    public Order trackOrder(@RequestParam String orderId, @RequestParam String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found. Please check your Order ID."));

        if (order.getCustomer() == null) {
            throw new RuntimeException("Phone number does not match this order.");
        }

        String storedPhoneClean = order.getCustomer().getPhone().replaceAll("[^0-9]", "");
        String queryPhoneClean = phone.replaceAll("[^0-9]", "");

        if (storedPhoneClean.length() >= 10) {
            storedPhoneClean = storedPhoneClean.substring(storedPhoneClean.length() - 10);
        }
        if (queryPhoneClean.length() >= 10) {
            queryPhoneClean = queryPhoneClean.substring(queryPhoneClean.length() - 10);
        }

        if (!storedPhoneClean.equals(queryPhoneClean)) {
            throw new RuntimeException("Phone number does not match this order.");
        }

        return order;
    }
}