package com.github.rinnn31.motelserver.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.account.UserInfo;
import com.github.rinnn31.motelserver.dto.authentication.AuthenticationResult;
import com.github.rinnn31.motelserver.dto.authentication.LoginInput;
import com.github.rinnn31.motelserver.dto.authentication.RegisterInput;
import com.github.rinnn31.motelserver.dto.authentication.ResetPasswordInput;
import com.github.rinnn31.motelserver.dto.authentication.TokenResult;
import com.github.rinnn31.motelserver.dto.authentication.UserIdTokenInput;
import com.github.rinnn31.motelserver.entity.UserEntity;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class AuthenticationService {
    public static final String VERIFY_ACTION_RESET_PASSWORD = "reset_password";

    public static final String VERIFY_ACTION_ACCOUNT_VERIFICATION = "account_verification";

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

    public AuthenticationResult register(RegisterInput registerModel) {
        if (userRepository.existsByPhoneNumber(registerModel.phoneNumber())) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        }
        

        UserEntity user = new UserEntity();
        user.setFullName(registerModel.fullName());
        user.setPhoneNumber(registerModel.phoneNumber());
        user.setGender(registerModel.gender());
        user.setPassword(passwordEncoder.encode(registerModel.password()));
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

    public AuthenticationResult login(LoginInput data) {
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

    public void sendAccountVerificationCode(String phoneNumber, Locale locale) {
        var user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        otpService.sendOtp(
            user.getId().toString(), 
            user.getPhoneNumber(), 
            VERIFY_ACTION_ACCOUNT_VERIFICATION, 
            locale);
    }   

    public void verifyAccount(String userId, String otp) {
        var user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (!otpService.verifyOtp(
                user.getId().toString(), 
                user.getPhoneNumber(), 
                VERIFY_ACTION_ACCOUNT_VERIFICATION, 
                otp, 
                true)) {
            throw new AppError(ErrorCode.VERIFY_FAILED);
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    public void resetPassword(ResetPasswordInput data) {
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

    public void logout(UserIdTokenInput data) {
        sessionManagementService.invalidateSession(data.refreshToken(), data.userId());
    }

    public TokenResult refresh(UserIdTokenInput data) {
        String newAccessToken = sessionManagementService.refreshAccessToken(data.refreshToken(), data.userId());
        return new TokenResult(newAccessToken, data.refreshToken());
    }

    public void sendResetPasswordOtp(String phoneNumber, Locale locale) {
        var user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        otpService.sendOtp(user.getId().toString(), phoneNumber, VERIFY_ACTION_RESET_PASSWORD, locale);
    }   
}
