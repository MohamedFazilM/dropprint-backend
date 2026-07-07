package com.dropprint.project.controller;

import com.dropprint.project.model.Customer;
import com.dropprint.project.repository.CustomerRepository;
import com.dropprint.project.service.IdGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    private String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Customer loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        Optional<Customer> customerOpt = customerRepository.findByEmail(loginRequest.getEmail().trim());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String hashedInput = hashPassword(loginRequest.getPassword().trim());
            if (hashedInput.equals(customer.getPassword())) {
                return ResponseEntity.ok(customer);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials. Please verify your password.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer profile not found. Please sign up first.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Customer signupRequest) {
        if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (signupRequest.getName() == null || signupRequest.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        Optional<Customer> existing = customerRepository.findByEmail(signupRequest.getEmail().trim());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered. Please login instead.");
        }

        Customer c = new Customer();
        c.setId(idGeneratorService.generate("cust", "customer_id_seq"));
        c.setName(signupRequest.getName().trim());
        c.setEmail(signupRequest.getEmail().trim());
        c.setPhone(signupRequest.getPhone() != null ? signupRequest.getPhone().trim() : "");
        c.setAddress(signupRequest.getAddress() != null ? signupRequest.getAddress().trim() : "");
        c.setPassword(hashPassword(signupRequest.getPassword().trim()));

        Customer saved = customerRepository.save(c);
        return ResponseEntity.ok(saved);
    }
}
