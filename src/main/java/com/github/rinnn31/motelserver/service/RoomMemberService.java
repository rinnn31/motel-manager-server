package com.github.rinnn31.motelserver.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.response.RoomMemberResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.RoomMember;
import com.github.rinnn31.motelserver.event.model.RoomMemberChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class RoomMemberService {
    private final RoomMemberRepository roomMemberRepository;

    private final RoomRepository roomRepository;

    private final MotelRepository motelRepository;

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    public RoomMemberService(
        RoomMemberRepository roomMemberRepository, 
        RoomRepository roomRepository, 
        MotelRepository motelRepository,
        UserRepository userRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.roomMemberRepository = roomMemberRepository;
        this.roomRepository = roomRepository;
        this.motelRepository = motelRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<RoomMemberResponse> getRoomMembersByRoomId(UUID roomId, UUID requesterId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var motel = room.getMotel();

        boolean isOwner = motel.getOwner().getId().equals(requesterId);
        boolean isRoomMember = roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId);

        if (!isOwner && !isRoomMember) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var members = roomMemberRepository.findByRoom_IdAndEndDateIsNull(roomId);
        return members.stream().map(member -> {
            var user = member.getUser();
            return new RoomMemberResponse(
                user.getId().toString(),
                room.getRoomNumber(),
                user.getFullName(),
                user.getPhoneNumber(),
                member.getStartDate()
            );
        }).toList();
    }

    public List<RoomMemberResponse> getRoomMembersByMotelId(UUID motelId, UUID requesterId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));

        boolean isOwner = motel.getOwner().getId().equals(requesterId);

        if (!isOwner) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var members = roomMemberRepository.findByRoom_Motel_IdAndEndDateIsNull(motelId);
        return members.stream().map(member -> {
            var user = member.getUser();
            var room = member.getRoom();
            return new RoomMemberResponse(
                user.getId().toString(),
                room.getRoomNumber(),
                user.getFullName(),
                user.getPhoneNumber(),
                member.getStartDate()
            );
        }).toList();
    }

    private Motel checkOwnershipAndGetMotel(UUID motelId, UUID ownerId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return motel;
    }

    public void addMember(UUID requesterId, UUID roomId, UUID userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        checkOwnershipAndGetMotel(room.getMotel().getId(), requesterId);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(userId, roomId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (roomMemberRepository.existsByUser_IdAndEndDateIsNull(userId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var member = new RoomMember();
        member.setRoom(room);
        member.setUser(user);
        member.setStartDate(LocalDate.now());
        roomMemberRepository.save(member);

        eventPublisher.publishEvent(new RoomMemberChangedEvent.Added(
            room.getMotel().getDisplayName(),
            room.getMotel().getId().toString(),
            room.getRoomNumber(),
            room.getId().toString(),
            userId.toString(),
            user.getFullName()
        ));
    }

    public void removeMember(UUID requesterId, UUID userId) {
        var member = roomMemberRepository.findByUser_IdAndEndDateIsNull(userId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));

        var room = member.getRoom();
        var motel = room.getMotel();

        if (!motel.getOwner().getId().equals(requesterId) && !member.getUser().getId().equals(requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        member.setEndDate(LocalDate.now());
        roomMemberRepository.save(member);

        eventPublisher.publishEvent(new RoomMemberChangedEvent.Removed(
            room.getMotel().getDisplayName(),
            room.getMotel().getId().toString(),
            room.getRoomNumber(),
            room.getId().toString(),
            userId.toString(),
            member.getUser().getFullName()
        ));
    }
}
