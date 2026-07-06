package com.dropprint.project.controller;

import com.dropprint.project.model.Ledger;
import com.dropprint.project.model.Order;
import com.dropprint.project.model.Product;
import com.dropprint.project.repository.LedgerRepository;
import com.dropprint.project.repository.OrderRepository;
import com.dropprint.project.repository.ProductRepository;
import com.dropprint.project.service.IdGeneratorService;
import com.dropprint.project.service.LedgerService;
import com.dropprint.project.service.SupabaseStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private IdGeneratorService idGeneratorService;

    // ---- Orders ----
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @PatchMapping("/orders/{id}/status")
    public Order updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String newStatus = body.get("status");
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        ledgerService.log("order", id, "status_updated", null, "Status changed to: " + newStatus);

        if ("delivered".equals(newStatus)) {
            order.setPaymentStatus("paid");
            orderRepository.save(order);
            ledgerService.log("order", id, "payment_received", order.getTotalPrice(), "COD payment collected on delivery");
        }

        return updated;
    }

    // ---- Products ----
    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        product.setId(idGeneratorService.generate("pdt", "product_id_seq"));
        Product saved = productRepository.save(product);
        ledgerService.log("product", saved.getId(), "product_created", saved.getBasePrice(), "Product added: " + saved.getName());
        return saved;
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product updated) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(updated.getName());
        product.setColor(updated.getColor());
        product.setBasePrice(updated.getBasePrice());
        product.setFabricInfo(updated.getFabricInfo());
        product.setGsm(updated.getGsm());
        product.setImageMain(updated.getImageMain());
        product.setImageBack(updated.getImageBack());
        product.setStatus(updated.getStatus());
        Product saved = productRepository.save(product);

        ledgerService.log("product", id, "product_updated", saved.getBasePrice(), "Product details updated");

        return saved;
    }

    @Autowired
    private SupabaseStorageService storageService;

    @PostMapping("/products/upload-image")
    public Map<String, String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = storageService.uploadFile(file, "products");
            return Map.of("url", fileUrl);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
    
    // ---- Ledger ----
    @GetMapping("/ledger")
    public List<Ledger> getAllLedgerEntries() {
        return ledgerRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/ledger/{entityId}")
    public List<Ledger> getLedgerByEntityId(@PathVariable String entityId) {
        return ledgerRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
    }
}