package com.github.rinnn31.motelserver.security;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserExtractor {
    public static UUID extractUserIdFromContext() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return java.util.UUID.fromString(userDetails.getUsername());
            }
        }
        return null;
    }
}
