package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddFeeRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateFeeRequest;
import com.github.rinnn31.motelserver.dto.response.FeeInfoResponse;
import com.github.rinnn31.motelserver.entity.CalculationType;
import com.github.rinnn31.motelserver.entity.MotelFee;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelFeeRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.utils.EnumHelper;

@Service
public class MotelFeeService {
    private final MotelFeeRepository motelFeeRepository;

    private final MotelRepository motelRepository;

    public MotelFeeService(MotelFeeRepository motelChargeRepository, MotelRepository motelRepository) {
        this.motelFeeRepository = motelChargeRepository;
        this.motelRepository = motelRepository;
    }

    public List<FeeInfoResponse> getFees(UUID motelId, UUID ownerId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        return motel.getFees().stream().map(FeeInfoResponse::new).toList();
    }

    public void addFee(UUID motelId, UUID ownerId, AddFeeRequest request) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var fee = new MotelFee();
        fee.setMotel(motel);
        fee.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        fee.setUnitPrice(request.unitPrice());
        fee.setName(request.name());
        motelFeeRepository.save(fee);
    }

    public void removeFee(UUID feeId, UUID ownerId) {
        var fee = motelFeeRepository.findById(feeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!fee.getMotel().getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        motelFeeRepository.delete(fee);
    }

    public void updateFee(UUID feeId, UUID ownerId, UpdateFeeRequest request) {
        var fee = motelFeeRepository.findById(feeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!fee.getMotel().getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        fee.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        fee.setUnitPrice(request.unitPrice());
        motelFeeRepository.save(fee);
    }
}
