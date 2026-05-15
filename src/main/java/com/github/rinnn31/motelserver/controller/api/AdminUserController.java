package com.github.rinnn31.motelserver.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.CreateUserRequest;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AccountService accountService;

    public AdminUserController(AccountService accountService) {
        this.accountService = accountService;
    }

    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserInfoResponse> getUsers(
        @RequestParam(required = false) String filterNumber, 
        @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "50") int size
    ) {
        return accountService.getUsers(filterNumber, page, size);
    }

    @PostMapping
    public String createUser(@Valid @RequestBody CreateUserRequest request) {
        return accountService.createUser(request);
    }

    @PatchMapping("/{userId}/verify")
    public void verifyUser(@RequestParam String userId) {
        accountService.verifyUser(java.util.UUID.fromString(userId));
    }
}
