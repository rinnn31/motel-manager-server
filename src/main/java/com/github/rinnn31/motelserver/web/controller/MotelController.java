package com.github.rinnn31.motelserver.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.CreateMotelRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateMotelNameRequest;
import com.github.rinnn31.motelserver.dto.response.MotelInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.MotelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/motels")
public class MotelController {
    private final MotelService motelService;

    public MotelController(MotelService motelService) {
        this.motelService = motelService;
    }

    @GetMapping
    public List<MotelInfoResponse> getMotels() {
        UUID userId = UserExtractor.extractUserIdFromContext();
        return motelService.getMotelsOfUser(userId);
    }

    @GetMapping("/joined")
    public MotelInfoResponse getJoinedMotel() {
        UUID userId = UserExtractor.extractUserIdFromContext();
        return motelService.getJoinedMotelInfo(userId);
    }

    @GetMapping("/{motelId}")
    public MotelInfoResponse getMotel(@PathVariable UUID motelId) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        return motelService.getMotelInfo(motelId, userId);
    }

    @PostMapping
    public void addMotel(@Valid @RequestBody CreateMotelRequest request) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        motelService.addMotel(userId, request.displayName());
    }

    @DeleteMapping("/{motelId}")
    public void deleteMotel(@PathVariable UUID motelId) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        motelService.deleteMotel(userId, motelId);
    }

    @PatchMapping("/{motelId}/name")
    public void updateMotelName(@PathVariable UUID motelId, @Valid @RequestBody UpdateMotelNameRequest request) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        motelService.updateMotelName(motelId, userId, request.newName());
    }

    @GetMapping("/{motelId}/owner")
    public UserInfoResponse getMotelOwnerInfo(@PathVariable UUID motelId) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        return motelService.getMotelOwnerInfo(motelId, userId); 
    }
}
