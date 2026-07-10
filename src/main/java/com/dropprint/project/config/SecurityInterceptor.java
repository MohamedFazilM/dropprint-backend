package com.dropprint.project.config;

import com.dropprint.project.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Arrays;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${admin.email:admin@dropprint.com,mohamedfazil.m10@gmail.com}")
    private String adminEmails;

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

        // Exclude admin login route from interceptor check
        if (path.equals("/api/admin/login") || path.endsWith("/admin/login") || path.endsWith("/admin/login/")) {
            return true;
        }

        // Secure Admin APIs and retrieve order ownership details
        if (path.startsWith("/api/admin") || (path.startsWith("/api/orders/") && "GET".equalsIgnoreCase(request.getMethod()))) {
            String authHeader = request.getHeader("Authorization");

            // In Dev Mode, skip validation ONLY if this is NOT an admin route AND no Authorization header is provided.
            if (jwtUtil.isDevMode() && !path.startsWith("/api/admin") && (authHeader == null || !authHeader.startsWith("Bearer "))) {
                System.out.println("[JWT] DEV MODE BYPASS: Skipping non-admin authorization checks.");
                request.setAttribute("is_admin_bypass", true);
                request.setAttribute("req_customer_email", "admin@dropprint.com");
                return true;
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated. Authorization token is missing.");
                return false;
            }

            try {
                // Decode and verify JWT
                Map<String, Object> claims = jwtUtil.validateAndExtractClaims(authHeader);
                String email = (String) claims.get("email");

                // Get role from metadata
                Map<String, Object> appMetadata = (Map<String, Object>) claims.get("app_metadata");
                String role = appMetadata != null ? (String) appMetadata.get("role") : null;
                if (role == null) {
                    Map<String, Object> userMetadata = (Map<String, Object>) claims.get("user_metadata");
                    role = userMetadata != null ? (String) userMetadata.get("role") : null;
                }

                // Check admin authorization
                boolean isAdmin = "admin".equalsIgnoreCase(role) || 
                        (email != null && Arrays.asList(adminEmails.split(",")).contains(email.toLowerCase().trim()));

                System.out.println("[SecurityInterceptor] AUTHORIZATION CHECK - Path: " + path + ", Email: " + email + ", Role: " + role + ", Configured Admins: " + adminEmails + " -> Resolved isAdmin: " + isAdmin);

                if (path.startsWith("/api/admin")) {
                    if (!isAdmin) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden. Administrator privileges required.");
                        return false;
                    }
                }

                if (path.startsWith("/api/orders/") && "GET".equalsIgnoreCase(request.getMethod())) {
                    if (isAdmin) {
                        request.setAttribute("is_admin_bypass", true);
                    }
                    // Inject verified email from JWT
                    request.setAttribute("req_customer_email", email);
                }

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized. Token verification failed: " + e.getMessage());
                return false;
            }
        }

        return true;
    }
}
