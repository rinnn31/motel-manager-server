package com.github.rinnn31.motelserver.service;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.config.OtpProperties;
import com.github.rinnn31.motelserver.dto.common.SendOtpResult;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.service.support.ISmsSender;

@Service
public class OtpService {
    private final ISmsSender smsSender;

    private final StringRedisTemplate redisTemplate;

    private final MessageSource messageSource;

    private final OtpProperties otpProperties;

    private final SecureRandom random = new SecureRandom();
    
    public OtpService(ISmsSender smsSender, StringRedisTemplate redisTemplate, MessageSource messageSource, OtpProperties otpProperties) {
        this.smsSender = smsSender;
        this.redisTemplate = redisTemplate;
        this.messageSource = messageSource;
        this.otpProperties = otpProperties;
    }

    public void sendOtp(String userId, String phoneNumber, String action, Locale locale) {
        String otpKey = String.format("otp:%s:%s:%s", userId, phoneNumber, action);
        String attemptKey = "otp:attempts:" + userId;
        String cooldownKey = "otp:cooldown:" + userId;
        
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        
        if (redisTemplate.hasKey(cooldownKey)) {
            long cooldown = redisTemplate.getExpire(cooldownKey);
            throw new AppError(
                ErrorCode.OTP_NOT_READY, 
                new SendOtpResult(false, otpProperties.maxAttemptsPerDay() - attempts, (int)cooldown));
        }
        if (attempts >= otpProperties.maxAttemptsPerDay()) {
            throw new AppError(ErrorCode.MAX_OTP_ATTEMPTS);
        }
        
        String otp = String.format("%06d", random.nextInt(1000000));
        String message = createMessage(action, otp, locale);
        if (!smsSender.sendMessage(phoneNumber, message)) {
            throw new AppError(ErrorCode.OTP_SENDING_FAILED);
        }

        redisTemplate.opsForValue().set(otpKey, otp, java.time.Duration.ofMinutes(otpProperties.expirationMinutes()));
        redisTemplate.opsForValue().increment(attemptKey);
        if (attempts == 0) {
            redisTemplate.expire(attemptKey, java.time.Duration.ofDays(1));
        }
        redisTemplate.opsForValue().set(cooldownKey, "1", java.time.Duration.ofSeconds(otpProperties.resendCooldownSeconds()));
    }

    public boolean verifyOtp(String userId, String phoneNumber, String action, String otp, boolean consume) {
        String key = String.format("otp:%s:%s:%s", userId, phoneNumber, action);
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            if (consume) {
                redisTemplate.delete(key);
            }
            return true;
        }
        return false;
    }

    public void invalidateOtps(String userId, String phoneNumber, String action) {
        String key = String.format("otp:%s:%s:%s", userId, phoneNumber, action);
        redisTemplate.delete(key);
    }

    private String createMessage(String action, String otp, Locale locale) {
        return messageSource.getMessage("otp.message", new Object[]{action, otp}, locale);
    }
}
