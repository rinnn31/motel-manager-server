package com.github.rinnn31.motelserver.web.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.dto.request.ChangePasswordRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateProfileRequest;
import com.github.rinnn31.motelserver.dto.request.VerifyContactpointRequest;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/info")
    public UserInfoResponse getAccountInfo(@RequestParam String userId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        boolean includesPrivateInfo = requesterId != null && requesterId.equals(UUID.fromString(userId));
        return accountService.getUserInfo(java.util.UUID.fromString(userId), includesPrivateInfo);
    }

    @PostMapping("/change-contactpoint")
    public void changeContactpoint(@RequestParam String newPhoneNumber) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.changeContactpoint(requesterId, newPhoneNumber);
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.changePassword(requesterId, input.oldPassword(), input.newPassword());
    }

    @DeleteMapping("/delete")
    public void deleteAccount() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.deleteAccount(requesterId);
    }

    @PostMapping("/verify-contactpoint")
    public void verifyContactpoint(@Valid @RequestBody VerifyContactpointRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.verifyContactpoint(requesterId, input.phoneNumber(), input.otp());
    }

    @PatchMapping("/update-profile")
    public void updateProfile(@Valid @RequestBody UpdateProfileRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.updateProfile(requesterId, input);
    }
}
