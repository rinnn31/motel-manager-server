package com.github.rinnn31.motelserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
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

    public String[] createJwtSession(String userId) {
        String refreshToken = java.util.UUID.randomUUID().toString().replace("-", "");
        String accessToken = jwtUtils.generateToken(userId, accessTokenTtls);
        
        String redisKey = "session:" + refreshToken;
        redisTemplate.opsForValue().set(redisKey, userId, java.time.Duration.ofMillis(refreshTokenTtls));

        return new String[]{accessToken, refreshToken};
    }

    public String refreshAccessToken(String refreshToken, String userId) {
        String storedUserId = redisTemplate.opsForValue().get("session:" + refreshToken);
        if (!userId.equals(storedUserId)) {
            throw new AppError(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return jwtUtils.generateToken(userId, accessTokenTtls);
    }

    public void invalidateSession(String refreshToken, String userId) {
        String redisKey = "session:" + refreshToken;
        String storedUserId = redisTemplate.opsForValue().get(redisKey);
        if (userId.equals(storedUserId)) {
            redisTemplate.delete(redisKey);
        }
    }
}
