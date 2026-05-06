package com.github.rinnn31.motelserver.controller.api;

import java.util.List;
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

import com.github.rinnn31.motelserver.dto.request.CreateMotelRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateMotelNameRequest;
import com.github.rinnn31.motelserver.dto.response.MotelInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
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
        Requester requester = Requester.fromContext();
        return motelService.getMotelsOfLandlord(requester.userId());
    }

    @GetMapping("/joined")
    public MotelInfoResponse getJoinedMotel(@RequestParam(required = false) UUID tenantId) {
        Requester requester = Requester.fromContext();
        return motelService.getJoinedMotelInfo(requester.userId());
    }

    @GetMapping("/{motelId}")
    public MotelInfoResponse getMotel(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return motelService.getMotelInfo(motelId, requester);
    }

    @PostMapping
    public void addMotel(@Valid @RequestBody CreateMotelRequest request) {
        Requester requester = Requester.fromContext();
        motelService.addMotel(requester, request.displayName());
    }

    @DeleteMapping("/{motelId}")
    public void deleteMotel(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        motelService.deleteMotel(motelId, requester);
    }

    @PatchMapping("/{motelId}/name")
    public void updateMotelName(@PathVariable UUID motelId, @Valid @RequestBody UpdateMotelNameRequest request) {
        Requester requester = Requester.fromContext();
        motelService.updateMotelName(motelId, requester, request.newName());
    }

    @GetMapping("/{motelId}/owner")
    public UserInfoResponse getMotelOwnerInfo(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return motelService.getMotelOwnerInfo(motelId, requester); 
    }
}
