package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddFeeRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateFeeRequest;
import com.github.rinnn31.motelserver.dto.response.FeeInfoResponse;
import com.github.rinnn31.motelserver.entity.CalculationType;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.MotelFee;
import com.github.rinnn31.motelserver.event.model.MotelFeeChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelFeeRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.utils.EnumHelper;

@Service
public class MotelFeeService {
    private final MotelFeeRepository motelFeeRepository;

    private final MotelRepository motelRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final MemberRepository memberRepository;

    public MotelFeeService(MotelFeeRepository motelChargeRepository, MotelRepository motelRepository, ApplicationEventPublisher eventPublisher, MemberRepository memberRepository) {
        this.motelFeeRepository = motelChargeRepository;
        this.motelRepository = motelRepository;
        this.eventPublisher = eventPublisher;
        this.memberRepository = memberRepository;

    }

    private Motel getMotelIfAccessible(UUID motelId, Requester requester) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        
        if (
            requester.isAdmin() ||
            motel.getOwner().getId().equals(requester.userId()) ||
            memberRepository.existsByRoom_Motel_IdAndUser_IdAndEndDateIsNull(motelId, requester.userId())
        ) {
            return motel;
        } else {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
    }

    public List<FeeInfoResponse> getFees(UUID motelId, Requester requester) {
        var motel = getMotelIfAccessible(motelId, requester);
        return motel.getFees().stream().map(FeeInfoResponse::new).toList();
    }

    public void addFee(UUID motelId, Requester requester, AddFeeRequest request) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (motelFeeRepository.existsByMotel_IdAndNameIgnoreCase(motelId, request.name())) {
            throw new AppError(ErrorCode.FEE_NAME_ALREADY_EXISTS);
        }

        var fee = new MotelFee();
        fee.setMotel(motel);
        fee.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        fee.setUnitPrice(request.unitPrice());
        fee.setName(request.name());
        motelFeeRepository.save(fee);

        eventPublisher.publishEvent(new MotelFeeChangedEvent(motelId.toString()));
    }

    public void removeFee(UUID feeId, Requester requester) {
        var fee = motelFeeRepository.findById(feeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!fee.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        motelFeeRepository.delete(fee);

        eventPublisher.publishEvent(new MotelFeeChangedEvent(fee.getMotel().getId().toString()));
    }

    public void updateFee(UUID feeId, Requester requester, UpdateFeeRequest request) {
        var fee = motelFeeRepository.findById(feeId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!fee.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        fee.setCalculationType(EnumHelper.fromString(CalculationType.class, request.calculationType(), CalculationType.FIXED));
        fee.setUnitPrice(request.unitPrice());
        motelFeeRepository.save(fee);

        eventPublisher.publishEvent(new MotelFeeChangedEvent(fee.getMotel().getId().toString()));
    }
}
