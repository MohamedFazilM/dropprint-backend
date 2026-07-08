package com.dropprint.project.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtUtil {

    @Value("${supabase.jwt-secret:}")
    private String jwtSecret;

    public Map<String, Object> validateAndExtractClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        }

        String token = authorizationHeader.substring(7).trim();
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token structure.");
        }

        // 1. Verify signature if jwtSecret is configured
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            System.out.println("[JWT] WARNING: SUPABASE_JWT_SECRET is not configured! Skipping signature verification (Development mode).");
        } else {
            boolean valid = verifyHS256(parts[0] + "." + parts[1], parts[2], jwtSecret);
            if (!valid) {
                throw new SecurityException("JWT signature verification failed.");
            }
        }

        // 2. Decode payload and parse using regex (zero-dependency)
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String jsonPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            Map<String, Object> claims = new HashMap<>();

            // Extract email
            String email = extractField(jsonPayload, "email");
            if (email != null) {
                claims.put("email", email);
            }

            // Extract sub (user ID)
            String sub = extractField(jsonPayload, "sub");
            if (sub != null) {
                claims.put("sub", sub);
            }

            // Extract role from user_metadata or app_metadata
            String role = extractRole(jsonPayload);
            if (role != null) {
                Map<String, Object> appMetadata = new HashMap<>();
                appMetadata.put("role", role);
                claims.put("app_metadata", appMetadata);
            }

            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWT payload: " + e.getMessage());
        }
    }

    private String extractField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractRole(String json) {
        Pattern pattern = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        String lastRole = null;
        while (matcher.find()) {
            String role = matcher.group(1);
            if ("admin".equalsIgnoreCase(role)) {
                return "admin";
            }
            lastRole = role;
        }
        return lastRole;
    }

    private boolean verifyHS256(String content, String signature, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            
            String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
