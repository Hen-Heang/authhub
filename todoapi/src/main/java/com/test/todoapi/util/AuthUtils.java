package com.test.todoapi.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("No authenticated user found");
            }

            Object principal = authentication.getPrincipal();

            // Use reflection to call getId() method to avoid importing UserPrincipal
            return (Long) principal.getClass().getMethod("getId").invoke(principal);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get current user ID: " + e.getMessage(), e);
        }
    }

    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getName();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current username: " + e.getMessage(), e);
        }
    }
}