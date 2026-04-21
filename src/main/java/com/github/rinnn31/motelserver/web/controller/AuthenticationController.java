package com.github.rinnn31.motelserver.web.controller;

import java.util.Locale;
import java.util.UUID;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.AuthenticationResponse;
import com.github.rinnn31.motelserver.dto.response.TokenResponse;
import com.github.rinnn31.motelserver.dto.request.DeviceRegisterRequest;
import com.github.rinnn31.motelserver.dto.request.LoginRequest;
import com.github.rinnn31.motelserver.dto.request.RegisterRequest;
import com.github.rinnn31.motelserver.dto.request.ResetPasswordRequest;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.AuthenticationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest body) {
        return authenticationService.login(body);
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest body) {
        return authenticationService.register(body);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@NotBlank @RequestParam String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    public void logout(@NotBlank(message = "Vui lòng cung cấp refresh token để đăng xuất") @RequestParam String refreshToken) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        authenticationService.logout(requesterId, refreshToken);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordModel) {
        authenticationService.resetPassword(resetPasswordModel);
    }

    @PostMapping("/send-reset-password-otp")
    public void sendResetPasswordOtp(@RequestParam String phoneNumber) {
        Locale locale = LocaleContextHolder.getLocale();
        authenticationService.sendResetPasswordOtp(phoneNumber, locale);
    }

    @PostMapping("/register-device")
    public void registerDevice(@Valid @RequestBody DeviceRegisterRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        authenticationService.registerDevice(requesterId, request);
    }

}