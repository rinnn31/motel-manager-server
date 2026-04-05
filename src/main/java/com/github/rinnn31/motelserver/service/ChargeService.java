package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddChargeRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateChargeRequest;
import com.github.rinnn31.motelserver.dto.response.ChargeInfoResponse;
import com.github.rinnn31.motelserver.entity.CalculationType;
import com.github.rinnn31.motelserver.entity.ChargeType;
import com.github.rinnn31.motelserver.entity.MotelCharge;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelChargeRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.utils.EnumHelper;

@Service
public class ChargeService {
    private final MotelChargeRepository motelChargeRepository;

    private final MotelRepository motelRepository;

    public ChargeService(MotelChargeRepository motelChargeRepository, MotelRepository motelRepository) {
        this.motelChargeRepository = motelChargeRepository;
        this.motelRepository = motelRepository;
    }

    public List<ChargeInfoResponse> getCharges(UUID motelId, UUID ownerId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        return motel.getCharges().stream().map(ChargeInfoResponse::new).toList();
    }

    public void addCharge(UUID motelId, UUID ownerId, AddChargeRequest request) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var charge = new MotelCharge();
        charge.setMotel(motel);
        charge.setType(EnumHelper.fromString(ChargeType.class, request.chargeType(), ChargeType.OTHER));
        charge.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        charge.setUnitPrice(request.unitPrice());
        charge.setDescription(request.description());
        motelChargeRepository.save(charge);
    }

    public void removeCharge(UUID chargeId, UUID ownerId) {
        var charge = motelChargeRepository.findById(chargeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!charge.getMotel().getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        motelChargeRepository.delete(charge);
    }

    public void updateCharge(UUID chargeId, UUID ownerId, UpdateChargeRequest request) {
        var charge = motelChargeRepository.findById(chargeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!charge.getMotel().getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        charge.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        charge.setUnitPrice(request.unitPrice());
        charge.setDescription(request.description());
        motelChargeRepository.save(charge);
    }
}
