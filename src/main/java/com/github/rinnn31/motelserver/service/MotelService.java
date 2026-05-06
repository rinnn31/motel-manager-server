package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.response.MotelInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.UserRole;
import com.github.rinnn31.motelserver.event.model.MotelNameChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;
import com.github.rinnn31.motelserver.security.Requester;

@Service
public class MotelService {
    private final MotelRepository motelRepository;

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    public MotelService(
        MotelRepository motelRepository, 
        UserRepository userRepository, 
        MemberRepository memberRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.motelRepository = motelRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
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

    public MotelInfoResponse getMotelInfo(UUID motelId, Requester requester) {
        var motel = getMotelIfAccessible(motelId, requester);
        
        int memberCount = memberRepository.countByRoom_Motel_IdAndEndDateIsNull(motelId);
        return new MotelInfoResponse(motel.getId().toString(), motel.getDisplayName(), memberCount);
    }

    public MotelInfoResponse getJoinedMotelInfo(UUID userId) {
        var roomMember = memberRepository.findByUser_IdAndEndDateIsNull(userId).orElse(null);
        if (roomMember == null) {
            return null;
        }

        int memberCount = memberRepository.countByRoom_Motel_IdAndEndDateIsNull(roomMember.getRoom().getMotel().getId());
        return new MotelInfoResponse(
            roomMember.getRoom().getMotel().getId().toString(), 
            roomMember.getRoom().getMotel().getDisplayName(), 
            memberCount
        );
    }

    public void addMotel(Requester requester, String displayName) {
        if (requester.isAdmin()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var user = userRepository.findById(requester.userId())
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (!UserRole.LANDLORD.equals(user.getRole())) {
            throw new AppError(ErrorCode.USER_NOT_LANDLORD);
        }   
        if (motelRepository.existsByOwner_IdAndDisplayName(requester.userId(), displayName)) {
            throw new AppError(ErrorCode.MOTEL_NAME_EXISTS);
        }

        var motel = new Motel();
        motel.setOwner(user);
        motel.setDisplayName(displayName);
        motelRepository.save(motel);
    }

    public void deleteMotel(UUID motelId, Requester requester) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        if (memberRepository.existsByRoom_Motel_IdAndEndDateIsNull(motelId)) {
            throw new AppError(ErrorCode.MOTEL_HAS_MEMBERS);
        }

        motelRepository.delete(motel);
    }

    public void updateMotelName(UUID motelId, Requester requester, String newDisplayName) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (motelRepository.existsByOwner_IdAndDisplayName(requester.userId(), newDisplayName)) {
            throw new AppError(ErrorCode.MOTEL_NAME_EXISTS);
        }

        motel.setDisplayName(newDisplayName);
        motelRepository.save(motel);

        eventPublisher.publishEvent(new MotelNameChangedEvent(motel.getId().toString(), newDisplayName));
    }

    public List<MotelInfoResponse> getMotelsOfLandlord(UUID userId) {
        var motels = motelRepository.findByOwner_Id(userId);
        return motels.stream().map(motel -> {
            int memberCount = memberRepository.countByRoom_Motel_IdAndEndDateIsNull(motel.getId());
            return new MotelInfoResponse(motel.getId().toString(), motel.getDisplayName(), memberCount);
        }).toList();
    }

    public UserInfoResponse getMotelOwnerInfo(UUID motelId, Requester requester) {
        var motel = getMotelIfAccessible(motelId, requester);

        var landlord = motel.getOwner();
        return new UserInfoResponse(
            landlord.getId().toString(),
            landlord.getPhoneNumber(),
            landlord.getFullName(),
            landlord.getGender(),
            landlord.getRole().name(),
            null,
            landlord.getAvatarUrl()
        );
    }

    public List<MotelInfoResponse> getAllMotels() {
        var motels = motelRepository.findAll();
        return motels.stream().map(motel -> {
            int memberCount = memberRepository.countByRoom_Motel_IdAndEndDateIsNull(motel.getId());
            return new MotelInfoResponse(motel.getId().toString(), motel.getDisplayName(), memberCount);
        }).toList();
    }
}
