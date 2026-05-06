package com.github.rinnn31.motelserver.security;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public record Requester(
    boolean isAdmin,
    UUID userId
) {
    public static Requester fromContext() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                
                return isAdmin
                    ? new Requester(true, new UUID(0, 0))
                    : new Requester(false, UUID.fromString(userDetails.getUsername()));

            }
        }
        return null;
    }
}
