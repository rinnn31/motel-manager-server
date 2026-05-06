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

import com.github.rinnn31.motelserver.dto.request.AddFeeRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateFeeRequest;
import com.github.rinnn31.motelserver.dto.response.FeeInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.service.MotelFeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fees")
public class MotelFeeController {
    private final MotelFeeService motelFeeService;

    public MotelFeeController(MotelFeeService chargeService) {
        this.motelFeeService = chargeService;
    }

    @GetMapping
    public List<FeeInfoResponse> getFees(@RequestParam UUID motelId) {
        Requester requester = Requester.fromContext();
        return motelFeeService.getFees(motelId, requester);
    }

    @PostMapping
    public void addFee(@RequestParam UUID motelId, @Valid @RequestBody AddFeeRequest request) {
        Requester requester = Requester.fromContext();
        motelFeeService.addFee(motelId, requester, request);
    }

    @DeleteMapping("/{feeId}")
    public void deleteFee(@PathVariable UUID feeId) {
        Requester requester = Requester.fromContext();
        motelFeeService.removeFee(feeId, requester);
    }

    @PatchMapping("/{feeId}")
    public void updateFee(@PathVariable UUID feeId, @Valid @RequestBody UpdateFeeRequest request) {
        Requester requester = Requester.fromContext();
        motelFeeService.updateFee(feeId, requester, request);
    }
}
