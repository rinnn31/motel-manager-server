package com.github.rinnn31.motelserver.web.controller;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.account.ChangePasswordInput;
import com.github.rinnn31.motelserver.dto.account.UpdateProfileInput;
import com.github.rinnn31.motelserver.dto.account.UserInfo;
import com.github.rinnn31.motelserver.dto.account.VerifyContactpointInput;
import com.github.rinnn31.motelserver.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    private UUID getRequesterId() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return java.util.UUID.fromString(userDetails.getUsername());
            }
        }
        return null;
    }

    @GetMapping("/info")
    public UserInfo getAccountInfo(@RequestParam String userId) {
        UUID requesterId = getRequesterId();
        boolean includesPrivateInfo = requesterId != null && requesterId.equals(UUID.fromString(userId));
        return accountService.getUserInfo(java.util.UUID.fromString(userId), includesPrivateInfo);
    }

    @PostMapping("/change-contactpoint")
    public void changeContactpoint(@RequestParam String newPhoneNumber) {
        UUID requesterId = getRequesterId();
        accountService.changeContactpoint(requesterId, newPhoneNumber);
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordInput input) {
        UUID requesterId = getRequesterId();
        accountService.changePassword(requesterId, input.oldPassword(), input.newPassword());
    }

    @DeleteMapping("/delete")
    public void deleteAccount() {
        UUID requesterId = getRequesterId();
        accountService.deleteAccount(requesterId);
    }

    @PostMapping("/verify-contactpoint")
    public void verifyContactpoint(@Valid @RequestBody VerifyContactpointInput input) {
        UUID requesterId = getRequesterId();
        accountService.verifyContactpoint(requesterId, input.phoneNumber(), input.otp());
    }

    @PatchMapping("/update-profile")
    public void updateProfile(@Valid @RequestBody UpdateProfileInput input) {
        UUID requesterId = getRequesterId();
        accountService.updateProfile(requesterId, input);
    }
}
