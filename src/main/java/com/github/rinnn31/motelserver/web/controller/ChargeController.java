package com.github.rinnn31.motelserver.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.UpdateChargeRequest;
import com.github.rinnn31.motelserver.dto.response.ChargeInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.ChargeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/charges")
public class ChargeController {
    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @GetMapping
    public List<ChargeInfoResponse> getCharges(@RequestParam UUID motelId) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        return chargeService.getCharges(motelId, userId);
    }

    @DeleteMapping("/{chargeId}")
    public void deleteCharge(@PathVariable UUID chargeId) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        chargeService.removeCharge(chargeId, userId);
    }

    @PatchMapping("/{chargeId}")
    public void updateCharge(@PathVariable UUID chargeId, @Valid @RequestBody UpdateChargeRequest request) {
        UUID userId = UserExtractor.extractUserIdFromContext();
        chargeService.updateCharge(chargeId, userId, request);
    }
}
