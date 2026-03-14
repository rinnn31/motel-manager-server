package com.github.rinnn31.motelserver.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserSecurityService userSecurityService;

    private final JwtUtils jwtUtils;
    
    public JwtAuthenticationFilter(UserSecurityService userSecurityService, JwtUtils jwtUtils) {
        this.userSecurityService = userSecurityService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        SecurityContext context = SecurityContextHolder.getContext();

        if (context.getAuthentication() == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtils.isTokenValid(token)) {
                String userId = jwtUtils.extractId(token);
                UserDetails userDetails = userSecurityService.loadUserById(userId);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    context.setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
    
}
