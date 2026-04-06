package com.github.rinnn31.motelserver.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.LoginRequest;
import com.github.rinnn31.motelserver.dto.request.RegisterRequest;
import com.github.rinnn31.motelserver.dto.request.ResetPasswordRequest;
import com.github.rinnn31.motelserver.dto.response.AuthenticationResponse;
import com.github.rinnn31.motelserver.dto.response.TokenResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.entity.User;
import com.github.rinnn31.motelserver.entity.UserRole;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class AuthenticationService {
    public static final String VERIFY_ACTION_RESET_PASSWORD = "reset_password";

    private final UserRepository userRepository;

    private final OtpService otpService;
    
    private final PasswordEncoder passwordEncoder;

    private final SessionManagementService sessionManagementService;

    public AuthenticationService(UserRepository userRepository, OtpService otpVerificationService, PasswordEncoder passwordEncoder, SessionManagementService sessionManagementService) {
        this.userRepository = userRepository;
        this.otpService = otpVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.sessionManagementService = sessionManagementService;
    }

    public AuthenticationResponse register(RegisterRequest registerModel) {
        if (userRepository.existsByPhoneNumber(registerModel.phoneNumber())) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        } 

        User user = new User();
        user.setFullName(registerModel.fullName());
        user.setPhoneNumber(registerModel.phoneNumber());
        user.setGender(registerModel.gender());
        user.setPassword(passwordEncoder.encode(registerModel.password()));
        user.setRole(registerModel.role() == 0 ? UserRole.LANDLORD : UserRole.TENANT);
        user = userRepository.save(user);

        String[] tokens = sessionManagementService.createJwtSession(user.getId().toString());
        return new AuthenticationResponse(
            tokens[0],
            tokens[1],
            user.getId().toString(),
            true,
            UserInfoResponse.fromEntity(user)
        );
    }

    public AuthenticationResponse login(LoginRequest data) {
        var user = userRepository.findByPhoneNumber(data.phoneNumber())
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new AppError(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isVerified()) {
            throw new AppError(
                ErrorCode.USER_NOT_VERIFIED, 
                new AuthenticationResponse(
                    null, 
                    null, 
                    user.getId().toString(), 
                    true, 
                    UserInfoResponse.fromEntity(user)));
        }

        String[] tokens = sessionManagementService.createJwtSession(user.getId().toString());
        return new AuthenticationResponse(
            tokens[0],
            tokens[1],
            user.getId().toString(),
            false,
            UserInfoResponse.fromEntity(user)
        );
    }
    
    public void resetPassword(ResetPasswordRequest data) {
        var user = userRepository.findByPhoneNumber(data.phoneNumber())
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (!otpService.verifyOtp(
                user.getId().toString(),
                user.getPhoneNumber(),
                VERIFY_ACTION_RESET_PASSWORD,
                data.verificationCode(),
                false)) {
            throw new AppError(ErrorCode.VERIFY_FAILED);
        }

        // Allow client to verify OTP only, so client can separate 
        // the flow of verifying OTP and resetting password.
        if (data.newPassword() != null && !data.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(data.newPassword()));
            userRepository.save(user);
            otpService.invalidateOtps(user.getId().toString(), user.getPhoneNumber(), VERIFY_ACTION_RESET_PASSWORD);
        }
        
    }

    public void logout(UUID userId, String refreshToken) {
        sessionManagementService.invalidateSession(refreshToken, userId.toString());
    }

    public TokenResponse refresh(UUID userId, String refreshToken) {
        String newAccessToken = sessionManagementService.refreshAccessToken(refreshToken, userId.toString());
        return new TokenResponse(newAccessToken, refreshToken);
    }

    public void sendResetPasswordOtp(String phoneNumber, Locale locale) {
        var user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        otpService.sendOtp(user.getId().toString(), phoneNumber, VERIFY_ACTION_RESET_PASSWORD, locale);
    }   
}
