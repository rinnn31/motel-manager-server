package com.github.rinnn31.motelserver.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.account.UserInfo;
import com.github.rinnn31.motelserver.dto.authentication.AuthenticationResult;
import com.github.rinnn31.motelserver.dto.authentication.LoginForm;
import com.github.rinnn31.motelserver.dto.authentication.RegisterForm;
import com.github.rinnn31.motelserver.dto.authentication.ResetPasswordForm;
import com.github.rinnn31.motelserver.dto.authentication.TokenResult;
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

    public AuthenticationResult register(RegisterForm registerModel) {
        if (userRepository.existsByPhoneNumber(registerModel.phoneNumber())) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        } 

        User user = new User();
        user.setFullName(registerModel.fullName());
        user.setPhoneNumber(registerModel.phoneNumber());
        user.setGender(registerModel.gender());
        user.setPassword(passwordEncoder.encode(registerModel.password()));
        user.setRole(registerModel.role() == 0 ? UserRole.LANDLORD : UserRole.TENANT);
        userRepository.save(user);

        String[] tokens = sessionManagementService.createJwtSession(user.getId().toString());
        return new AuthenticationResult(
            tokens[0],
            tokens[1],
            user.getId().toString(),
            true,
            UserInfo.fromEntity(user)
        );
    }

    public AuthenticationResult login(LoginForm data) {
        var user = userRepository.findByPhoneNumber(data.phoneNumber())
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new AppError(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isVerified()) {
            throw new AppError(
                ErrorCode.USER_NOT_VERIFIED, 
                new AuthenticationResult(
                    null, 
                    null, 
                    user.getId().toString(), 
                    true, 
                    UserInfo.fromEntity(user)));
        }

        String[] tokens = sessionManagementService.createJwtSession(user.getId().toString());
        return new AuthenticationResult(
            tokens[0],
            tokens[1],
            user.getId().toString(),
            false,
            UserInfo.fromEntity(user)
        );
    }
    
    public void resetPassword(ResetPasswordForm data) {
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

    public TokenResult refresh(UUID userId, String refreshToken) {
        String newAccessToken = sessionManagementService.refreshAccessToken(refreshToken, userId.toString());
        return new TokenResult(newAccessToken, refreshToken);
    }

    public void sendResetPasswordOtp(String phoneNumber, Locale locale) {
        var user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        otpService.sendOtp(user.getId().toString(), phoneNumber, VERIFY_ACTION_RESET_PASSWORD, locale);
    }   
}
