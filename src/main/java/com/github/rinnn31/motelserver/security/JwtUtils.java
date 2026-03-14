package com.github.rinnn31.motelserver.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() { 
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String id, long ttl) {
        return Jwts.builder()
            .setSubject(id)
            .signWith(getSigningKey())
            .setExpiration(java.util.Date.from(java.time.Instant.now().plusMillis(ttl)))
            .compact();
    }

    public String extractId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    @SuppressWarnings("UseSpecificCatch")
    public boolean isTokenValid(String token) {
        try {
            var parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return !parser.getBody().getExpiration().before(new java.util.Date());
        } catch (Exception ignored) {
            return false;
        }
    }

}
