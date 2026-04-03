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

import com.github.rinnn31.motelserver.dto.authentication.AuthenticationResult;
import com.github.rinnn31.motelserver.dto.authentication.LoginForm;
import com.github.rinnn31.motelserver.dto.authentication.RegisterForm;
import com.github.rinnn31.motelserver.dto.authentication.ResetPasswordForm;
import com.github.rinnn31.motelserver.dto.authentication.TokenResult;
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
    public AuthenticationResult login(@Valid @RequestBody LoginForm body) {
        return authenticationService.login(body);
    }

    @PostMapping("/register")
    public AuthenticationResult register(@Valid @RequestBody RegisterForm body) {
        return authenticationService.register(body);
    }

    @PostMapping("/refresh")
    public TokenResult refresh(@NotBlank @RequestParam String refreshToken) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return authenticationService.refresh(requesterId, refreshToken);
    }

    @PostMapping("/logout")
    public void logout(@NotBlank(message = "Vui lòng cung cấp refresh token để đăng xuất") @RequestParam String refreshToken) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        authenticationService.logout(requesterId, refreshToken);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordForm resetPasswordModel) {
        authenticationService.resetPassword(resetPasswordModel);
    }

    @PostMapping("/send-reset-password-otp")
    public void sendResetPasswordOtp(@RequestParam String phoneNumber) {
        Locale locale = LocaleContextHolder.getLocale();
        authenticationService.sendResetPasswordOtp(phoneNumber, locale);
    }
}