package com.github.rinnn31.motelserver.controller.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.ChangePasswordRequest;
import com.github.rinnn31.motelserver.dto.request.ContactpointRequest;
import com.github.rinnn31.motelserver.dto.request.SendOtpRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateProfileRequest;
import com.github.rinnn31.motelserver.dto.request.VerifyContactpointRequest;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
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

    @GetMapping("/me")
    public UserInfoResponse getAccountInfo() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return accountService.getUserInfo(requesterId, true);
    }

    @GetMapping("/{userId}")
    public UserInfoResponse getUserInfo(@PathVariable String userId) {
        return accountService.getUserInfo(java.util.UUID.fromString(userId), false);
    }

    @PatchMapping("/me/contactpoint")
    public void changeContactpoint(@Valid @RequestBody ContactpointRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.changeContactpoint(requesterId, request.phoneNumber());
    }

    @PatchMapping("/me/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.changePassword(requesterId, input.oldPassword(), input.newPassword());
    }

    @DeleteMapping("/me")
    public void deleteAccount() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.deleteAccount(requesterId);
    }

    @PostMapping("/me/contactpoint/otp")
    public void sendContactpointVerificationOtp(@Valid @RequestBody SendOtpRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.sendContactpointVerificationCode(requesterId, request.phoneNumber(), null);
    }

    @PostMapping("/me/contactpoint/verify")
    public void verifyContactpoint(@Valid @RequestBody VerifyContactpointRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.verifyContactpoint(requesterId, input.phoneNumber(), input.otp());
    }

    @PatchMapping("/me")
    public void updateProfile(@Valid @RequestBody UpdateProfileRequest input) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.updateProfile(requesterId, input);
    }

    @PostMapping("/me/avatar/upload-url")
    public MediaPresignedUrlResponse getAvatarUploadPresignedUrl(@RequestParam String imageType) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return accountService.getAvatarUploadPresignedUrl(requesterId, imageType);
    }

    @PatchMapping("/me/avatar")
    public void updateAvatar(@RequestParam String avatarKey) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        accountService.updateAvatarUrl(requesterId, avatarKey);
    }
}
