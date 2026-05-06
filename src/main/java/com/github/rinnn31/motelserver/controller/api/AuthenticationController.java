package com.github.rinnn31.motelserver.controller.api;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.AuthenticationResponse;
import com.github.rinnn31.motelserver.dto.response.TokenResponse;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.dto.request.DeviceRegisterRequest;
import com.github.rinnn31.motelserver.dto.request.LoginRequest;
import com.github.rinnn31.motelserver.dto.request.LogoutRequest;
import com.github.rinnn31.motelserver.dto.request.RefreshTokenRequest;
import com.github.rinnn31.motelserver.dto.request.RegisterRequest;
import com.github.rinnn31.motelserver.dto.request.ResetPasswordRequest;
import com.github.rinnn31.motelserver.dto.request.SendOtpRequest;
import com.github.rinnn31.motelserver.service.AuthenticationService;

import jakarta.validation.Valid;

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

    @PostMapping("/refresh-token")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody LogoutRequest request) {
        Requester requester = Requester.fromContext();
        authenticationService.logout(requester.userId(), request.refreshToken());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
    }

    @PostMapping("/request-reset-password")
    public void sendResetPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        authenticationService.sendResetPasswordOtp(request.phoneNumber(), locale);
    }

    @PostMapping("/register-device")
    public void registerDevice(@Valid @RequestBody DeviceRegisterRequest request) {
        Requester requester = Requester.fromContext();
        authenticationService.registerDevice(requester.userId(), request);
    }

}