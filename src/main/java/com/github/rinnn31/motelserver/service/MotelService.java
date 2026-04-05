package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.response.MotelInfoResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.UserRole;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class MotelService {
    private final MotelRepository motelRepository;

    private final UserRepository userRepository;

    private final RoomMemberRepository roomMemberRepository;

    public MotelService(MotelRepository motelRepository, UserRepository userRepository, RoomMemberRepository roomMemberRepository) {
        this.motelRepository = motelRepository;
        this.userRepository = userRepository;
        this.roomMemberRepository = roomMemberRepository;
    }

    public MotelInfoResponse getMotelInfo(UUID motelId, UUID requesterId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(requesterId) && !roomMemberRepository.existsByRoom_Motel_IdAndEndDateIsNull(motelId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        int memberCount = roomMemberRepository.countByRoom_Motel_IdAndEndDateIsNull(motelId);
        return new MotelInfoResponse(motel.getId().toString(), motel.getDisplayName(), memberCount);
    }

    public void addMotel(UUID ownerId, String displayName) {
        var user = userRepository.findById(ownerId)
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (!UserRole.LANDLORD.equals(user.getRole())) {
            throw new AppError(ErrorCode.USER_NOT_LANDLORD);
        }

        var motel = new Motel();
        motel.setOwner(user);
        motel.setDisplayName(displayName);
        motelRepository.save(motel);
    }

    public void deleteMotel(UUID motelId, UUID ownerId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        if (roomMemberRepository.existsByRoom_Motel_IdAndEndDateIsNull(motelId)) {
            throw new AppError(ErrorCode.MOTEL_HAS_MEMBERS);
        }

        motelRepository.delete(motel);
    }

    public void updateMotel(UUID motelId, UUID ownerId, String newDisplayName) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (motelRepository.existsByOwner_IdAndDisplayName(ownerId, newDisplayName)) {
            throw new AppError(ErrorCode.MOTEL_NAME_EXISTS);
        }

        motel.setDisplayName(newDisplayName);
        motelRepository.save(motel);
    }

    public List<MotelInfoResponse> getMotelsOfUser(UUID userId) {
        var motels = motelRepository.findByOwner_Id(userId);
        return motels.stream().map(motel -> {
            int memberCount = roomMemberRepository.countByRoom_Motel_IdAndEndDateIsNull(motel.getId());
            return new MotelInfoResponse(motel.getId().toString(), motel.getDisplayName(), memberCount);
        }).toList();
    }
}
