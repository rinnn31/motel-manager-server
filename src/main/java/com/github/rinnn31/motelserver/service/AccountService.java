package com.github.rinnn31.motelserver.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.UpdateProfileRequest;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class AccountService {
    public static final String VERIFY_CONTACTPOINT_ACTION = "contactpoint_verification";

    public static final int PENDING_PHONE_CHANGE_TTL_MINUTES = 30;

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, OtpService otpService, StringRedisTemplate redisTemplate, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }   

    public void deleteAccount(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppError(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public void changeContactpoint(UUID userId, String newPhoneNumber) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        }
        
        // If the user has already verified their account,
        // we need to verify the new phone number before changing it
        if (user.isVerified()) {
            redisTemplate.opsForValue().set("pending_phone_change:" + userId, newPhoneNumber, java.time.Duration.ofMinutes(PENDING_PHONE_CHANGE_TTL_MINUTES));
        } else {
            user.setPhoneNumber(newPhoneNumber);
            userRepository.save(user);
        }

    }

    public UserInfoResponse getUserInfo(UUID userId, boolean includesPrivateInfo) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        return new UserInfoResponse(
            includesPrivateInfo ? user.getPhoneNumber() : null,
            user.getFullName(),
            user.getGender(),
            user.getRole().name(),
            includesPrivateInfo ? user.isVerified() : null
        );
    }

    public void updateProfile(UUID userId, UpdateProfileRequest input) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (input.fullName() != null && !input.fullName().isBlank()) {
            user.setFullName(input.fullName());
        }
        if (input.gender() != null) {
            user.setGender(input.gender());
        }

        userRepository.save(user);
    }

    public void sendContactpointVerificationCode(UUID userId, String phoneNumber, Locale locale) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            String pendingPhoneNumber = redisTemplate.opsForValue().get("pending_phone_change:" + user.getId().toString());
            if (pendingPhoneNumber == null || !pendingPhoneNumber.equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        } else {
            if (!user.getPhoneNumber().equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        }
        otpService.sendOtp(user.getId().toString(), phoneNumber, VERIFY_CONTACTPOINT_ACTION, locale);
    }   

    public void verifyContactpoint(UUID userId, String phoneNumber, String otp) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            String pendingPhoneNumber = redisTemplate.opsForValue().get("pending_phone_change:" + userId);
            if (pendingPhoneNumber == null || !pendingPhoneNumber.equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        } else {
            if (!user.getPhoneNumber().equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        }
        if (!otpService.verifyOtp(userId.toString(), phoneNumber, VERIFY_CONTACTPOINT_ACTION, otp, true)) {
            throw new AppError(ErrorCode.VERIFY_FAILED);
        }

        user.setVerified(true);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }
}
