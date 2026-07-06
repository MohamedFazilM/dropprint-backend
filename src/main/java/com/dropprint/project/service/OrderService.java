package com.dropprint.project.service;

import com.dropprint.project.dto.OrderRequestDTO;
import com.dropprint.project.dto.OrderItemRequestDTO;
import com.dropprint.project.model.*;
import com.dropprint.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DesignRepository designRepository;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private IdGeneratorService idGeneratorService;

    public Order createOrder(OrderRequestDTO dto) {

        Customer customer = customerRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setId(idGeneratorService.generate("cust", "customer_id_seq"));
                    c.setName(dto.getName());
                    c.setEmail(dto.getEmail());
                    c.setPhone(dto.getPhone());
                    c.setAddress(dto.getAddress());
                    Customer saved = customerRepository.save(c);
                    ledgerService.log("customer", saved.getId(), "customer_created", null, "New customer registered: " + saved.getName());
                    return saved;
                });

        Order order = new Order();
        order.setId(idGeneratorService.generate("odr", "order_id_seq"));
        order.setCustomer(customer);
        order.setStatus("placed");
        order.setPaymentStatus("pending");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            OrderItem item = new OrderItem();
            item.setId(idGeneratorService.generate("oit", "order_item_id_seq"));
            item.setOrder(order);
            item.setProduct(product);
            item.setSize(itemDto.getSize());
            item.setQty(itemDto.getQty());
            item.setPrice(itemDto.getPrice());

            if (itemDto.getDesignId() != null) {
                Design design = designRepository.findById(itemDto.getDesignId())
                        .orElseThrow(() -> new RuntimeException("Design not found: " + itemDto.getDesignId()));
                item.setDesign(design);
            }

            orderItems.add(item);
            total += itemDto.getPrice() * itemDto.getQty();
        }

        order.setItems(orderItems);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        ledgerService.log("order", savedOrder.getId(), "order_placed", total,
                "Order placed by " + customer.getName() + " via COD");

        return savedOrder;
    }
}