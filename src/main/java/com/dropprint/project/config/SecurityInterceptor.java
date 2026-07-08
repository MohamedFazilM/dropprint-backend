package com.dropprint.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Value("${admin.token:admin123}")
    private String adminToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Add security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Allow CORS preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 1. Secure Admin APIs
        if (path.startsWith("/api/admin")) {
            String token = request.getHeader("X-Admin-Token");
            if (token == null || token.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated. Admin token is missing.");
                return false;
            }
            if (!adminToken.equals(token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden. Invalid admin token.");
                return false;
            }
        }

        // 2. Setup IDOR checks on Retrieve Order details
        if (path.startsWith("/api/orders/") && "GET".equalsIgnoreCase(request.getMethod())) {
            String adminTokenHeader = request.getHeader("X-Admin-Token");
            if (adminToken.equals(adminTokenHeader)) {
                return true; // Admin has full access bypass
            }

            String customerEmailHeader = request.getHeader("X-Customer-Email");
            String customerIdHeader = request.getHeader("X-Customer-Id");

            if (customerEmailHeader == null || customerIdHeader == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized. Authentication credentials missing.");
                return false;
            }
            
            // Pass values to controller for matching ownership
            request.setAttribute("req_customer_id", customerIdHeader);
            request.setAttribute("req_customer_email", customerEmailHeader);
        }

        return true;
    }
}
