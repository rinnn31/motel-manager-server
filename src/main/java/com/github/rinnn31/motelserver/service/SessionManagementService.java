package com.github.rinnn31.motelserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.security.JwtUtils;


@Service
public class SessionManagementService {
    private final StringRedisTemplate redisTemplate;
    
    private final JwtUtils jwtUtils;

    @Value("${security.jwt.access-token-ttl}")
    private long accessTokenTtls;

    @Value("${security.jwt.refresh-token-ttl}")
    private long refreshTokenTtls;

    public SessionManagementService(StringRedisTemplate redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }   

    public String[] createSession(String userId) {
        String refreshToken = java.util.UUID.randomUUID().toString().replace("-", "");
        String accessToken = jwtUtils.generateToken(userId, accessTokenTtls);

        redisTemplate.opsForValue().set(refreshToken, userId, java.time.Duration.ofMillis(refreshTokenTtls));

        return new String[]{accessToken, refreshToken};
    }

    public String refreshAccessToken(String refreshToken) {
        String userId = redisTemplate.opsForValue().get(refreshToken);

        return jwtUtils.generateToken(userId, accessTokenTtls);
    }

    public void invalidateSession(String refreshToken, String userId) {
        String storedUserId = redisTemplate.opsForValue().get(refreshToken);
        if (userId.equals(storedUserId)) {
            redisTemplate.delete(refreshToken);
        }
    }

    public boolean isSessionValid(String refreshToken, String userId) {
        return redisTemplate.hasKey(refreshToken) && userId.equals(redisTemplate.opsForValue().get(refreshToken));
    }
}
