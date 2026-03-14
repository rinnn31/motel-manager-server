package com.github.rinnn31.motelserver.web.controller;

import java.util.Locale;
import java.util.UUID;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.authentication.AuthenticationResult;
import com.github.rinnn31.motelserver.dto.authentication.LoginInput;
import com.github.rinnn31.motelserver.dto.authentication.RegisterInput;
import com.github.rinnn31.motelserver.dto.authentication.ResetPasswordInput;
import com.github.rinnn31.motelserver.dto.authentication.TokenResult;
import com.github.rinnn31.motelserver.dto.authentication.UserIdTokenInput;
import com.github.rinnn31.motelserver.service.AccountService;
import com.github.rinnn31.motelserver.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    private final AccountService accountService;

    public AuthenticationController(AuthenticationService authenticationService, AccountService accountService) {
        this.authenticationService = authenticationService;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public AuthenticationResult login(@Valid @RequestBody LoginInput body) {
        return authenticationService.login(body);
    }

    @PostMapping("/register")
    public AuthenticationResult register(@Valid @RequestBody RegisterInput body) {
        return authenticationService.register(body);
    }

    @PostMapping("/refresh")
    public TokenResult refresh(@Valid @RequestBody UserIdTokenInput body) {
        return authenticationService.refresh(body);
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody UserIdTokenInput body) {
        authenticationService.logout(body);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordInput resetPasswordModel) {
        authenticationService.resetPassword(resetPasswordModel);
    }

    @PostMapping("/send-reset-password-otp")
    public void sendResetPasswordOtp(@RequestParam String phoneNumber) {
        Locale locale = LocaleContextHolder.getLocale();
        authenticationService.sendResetPasswordOtp(phoneNumber, locale);
    }

    @PostMapping("/send-verification-otp")
    public void sendVerificationOtp(@RequestParam String phoneNumber) {
        Locale locale = LocaleContextHolder.getLocale();
        authenticationService.sendAccountVerificationCode(phoneNumber, locale);
    }

    @PostMapping("/change-contactpoint")
    public void changeVerificationContactpoint(@RequestParam String newPhoneNumber) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.changeContactpoint(UUID.fromString(userId), newPhoneNumber);
    }

    @PostMapping("/verify-account")
    public void verifyAccount(@RequestParam String otp) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        authenticationService.verifyAccount(userId, otp);
    }
}