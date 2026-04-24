package com.github.rinnn31.motelserver.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddMemberRequest;
import com.github.rinnn31.motelserver.dto.response.MemberInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.Member;
import com.github.rinnn31.motelserver.event.model.RoomMemberChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    private final RoomRepository roomRepository;

    private final MotelRepository motelRepository;

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    public MemberService(
        MemberRepository roomMemberRepository, 
        RoomRepository roomRepository, 
        MotelRepository motelRepository,
        UserRepository userRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.memberRepository = roomMemberRepository;
        this.roomRepository = roomRepository;
        this.motelRepository = motelRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<MemberInfoResponse> getRoomMembersByRoomId(UUID roomId, UUID requesterId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var motel = room.getMotel();

        boolean isOwner = motel.getOwner().getId().equals(requesterId);
        boolean isRoomMember = memberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId);

        if (!isOwner && !isRoomMember) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var members = memberRepository.findByRoom_IdAndEndDateIsNull(roomId);
        return members.stream().map(member -> {
            var user = member.getUser();
            return new MemberInfoResponse(
                new UserInfoResponse(
                    user.getId().toString(),
                    user.getPhoneNumber(),
                    user.getFullName(),
                    user.getGender(),
                    user.getRole().name(),
                    null,
                    user.getAvatarUrl()
                ),
                room.getRoomNumber(),
                member.getStartDate()
            );
        }).toList();
    }

    public List<MemberInfoResponse> getRoomMembersByMotelId(UUID motelId, UUID requesterId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));

        boolean isOwner = motel.getOwner().getId().equals(requesterId);

        if (!isOwner) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var members = memberRepository.findByRoom_Motel_IdAndEndDateIsNull(motelId);
        return members.stream().map(member -> {
            var user = member.getUser();
            var room = member.getRoom();
            return new MemberInfoResponse(
                new UserInfoResponse(
                    user.getId().toString(),
                    user.getPhoneNumber(),
                    user.getFullName(),
                    user.getGender(),
                    user.getRole().name(),
                    null,
                    user.getAvatarUrl()
                ),
                room.getRoomNumber(),
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

    public void addMember(UUID requesterId, AddMemberRequest request) {
        var room = roomRepository.findById(UUID.fromString(request.roomId()))
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        checkOwnershipAndGetMotel(room.getMotel().getId(), requesterId);

        var user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (memberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(user.getId(), UUID.fromString(request.roomId()))) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (memberRepository.existsByUser_IdAndEndDateIsNull(user.getId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var member = new Member();
        member.setRoom(room);
        member.setUser(user);
        member.setStartDate(LocalDate.now());
        memberRepository.save(member);

        eventPublisher.publishEvent(new RoomMemberChangedEvent.Added(
            room.getMotel().getDisplayName(),
            room.getMotel().getId().toString(),
            room.getRoomNumber(),
            room.getId().toString(),
            user.getId().toString(),
            user.getFullName()
        ));
    }

    public void removeMember(UUID requesterId, UUID userId) {
        var member = memberRepository.findByUser_IdAndEndDateIsNull(userId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));

        var room = member.getRoom();
        var motel = room.getMotel();

        if (!motel.getOwner().getId().equals(requesterId) && !member.getUser().getId().equals(requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        member.setEndDate(LocalDate.now());
        memberRepository.save(member);

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
