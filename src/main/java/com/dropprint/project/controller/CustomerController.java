package com.dropprint.project.controller;

import com.dropprint.project.model.Customer;
import com.dropprint.project.repository.CustomerRepository;
import com.dropprint.project.service.IdGeneratorService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dropprint.project.util.JwtUtil;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private JwtUtil jwtUtil;

    private String hashPassword(String password) {
        if (password == null) return "";
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Customer loginRequest, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Fast JWT-based verification bypass
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Map<String, Object> claims = jwtUtil.validateAndExtractClaims(authHeader);
                String emailFromJwt = (String) claims.get("email");
                if (emailFromJwt != null && emailFromJwt.equalsIgnoreCase(loginRequest.getEmail().trim())) {
                    Optional<Customer> customerOpt = customerRepository.findByEmailIgnoreCase(loginRequest.getEmail().trim());
                    if (customerOpt.isPresent()) {
                        return ResponseEntity.ok(customerOpt.get());
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer profile not found. Please sign up first.");
                    }
                }
            } catch (Exception e) {
                // Fallback to password check if JWT verification fails
            }
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        Optional<Customer> customerOpt = customerRepository.findByEmailIgnoreCase(loginRequest.getEmail().trim());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String plainPassword = loginRequest.getPassword().trim();
            if (BCrypt.checkpw(plainPassword, customer.getPassword())) {
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

        Optional<Customer> existing = customerRepository.findByEmailIgnoreCase(signupRequest.getEmail().trim());
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
