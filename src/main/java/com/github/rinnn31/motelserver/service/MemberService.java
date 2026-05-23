package com.github.rinnn31.motelserver.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.rinnn31.motelserver.dto.request.InviteMemberRequest;
import com.github.rinnn31.motelserver.dto.response.InviteInfoResponse;
import com.github.rinnn31.motelserver.dto.response.MemberInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.entity.Invite;
import com.github.rinnn31.motelserver.entity.Member;
import com.github.rinnn31.motelserver.event.model.InvitationEvent;
import com.github.rinnn31.motelserver.event.model.RoomMemberChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.InviteRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;
import com.github.rinnn31.motelserver.security.Requester;

@Service
public class MemberService {
    private static final int INVITE_EXPIRATION_HOURS = 24;

    private final MemberRepository memberRepository;

    private final RoomRepository roomRepository;

    private final MotelRepository motelRepository;

    private final UserRepository userRepository;

    private final InviteRepository inviteRepository;

    private final ApplicationEventPublisher eventPublisher;

    public MemberService(
        MemberRepository roomMemberRepository, 
        RoomRepository roomRepository, 
        MotelRepository motelRepository,
        UserRepository userRepository,
        InviteRepository inviteRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.memberRepository = roomMemberRepository;
        this.roomRepository = roomRepository;
        this.motelRepository = motelRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<MemberInfoResponse> getRoomMembersByRoomId(UUID roomId, Requester requester) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var motel = room.getMotel();

        boolean isOwnerOrAdmin = requester.isAdmin() || motel.getOwner().getId().equals(requester.userId());
        boolean isRoomMember = !isOwnerOrAdmin && memberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requester.userId(), roomId);

        if (!isOwnerOrAdmin && !isRoomMember) {
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

    public List<MemberInfoResponse> getRoomMembersByMotelId(UUID motelId, Requester requester) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));

        if (!requester.isAdmin() && !motel.getOwner().getId().equals(requester.userId())) {
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

    public void inviteMember(Requester requester, InviteMemberRequest request) {
        if (requester.isAdmin()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var room = roomRepository.findById(UUID.fromString(request.roomId()))
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        if (!room.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == com.github.rinnn31.motelserver.entity.UserRole.LANDLORD) {
            throw new AppError(ErrorCode.IS_LANDLORD);
        }
        if (memberRepository.existsByUser_IdAndEndDateIsNull(user.getId())) {
            throw new AppError(ErrorCode.USER_ALREADY_IN_ROOM);
        }
        LocalDateTime now = LocalDateTime.now();
        if (inviteRepository.existsByRoom_Motel_IdAndUser_IdAndExpiredAtAfter(room.getMotel().getId(), user.getId(), now)) {
            throw new AppError(ErrorCode.ALREADY_INVITED);
        }

        Invite invite = new Invite();
        invite.setRoom(room);
        invite.setUser(user);
        invite.setExpiredAt(now.plusHours(INVITE_EXPIRATION_HOURS));
        inviteRepository.save(invite);

        eventPublisher.publishEvent(new InvitationEvent.InvaitationSent(
            room.getMotel().getDisplayName(),
            room.getMotel().getOwner().getFullName(),
            user.getId()
        ));
    }

    @Transactional
    public void acceptInvite(Requester requester, UUID inviteId) {
        var invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        
        LocalDateTime now = LocalDateTime.now();
        if (
            !invite.getUser().getId().equals(requester.userId()) ||
            invite.getExpiredAt().isBefore(now) ||
            memberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requester.userId(), invite.getRoom().getId())
        ) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        var member = new Member();
        member.setRoom(invite.getRoom());
        member.setUser(invite.getUser());
        member.setStartDate(now.toLocalDate());
        memberRepository.save(member);

        // Delete all pending invites for the user since they can only be in one room at a time
        inviteRepository.deleteAllByUser_Id(requester.userId());

        var room = invite.getRoom();
        eventPublisher.publishEvent(new InvitationEvent.InvitationAccepted(
            requester.userId(),
            invite.getUser().getFullName(),
            invite.getRoom().getMotel().getId()
        ));

        eventPublisher.publishEvent(new RoomMemberChangedEvent.Added(
            room.getMotel().getDisplayName(),
            room.getMotel().getId().toString(),
            room.getRoomNumber(),
            room.getId().toString(),
            invite.getUser().getId().toString(),
            invite.getUser().getFullName()
        ));
    }

    @Transactional
    public void rejectInvite(Requester requester, UUID inviteId) {
        var invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
        if (!invite.getUser().getId().equals(requester.userId()) || invite.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        inviteRepository.delete(invite);

        eventPublisher.publishEvent(new InvitationEvent.InvitationDeclined(
            requester.userId(),
            invite.getUser().getFullName(),
            invite.getRoom().getMotel().getId()
        ));
    }

    public List<InviteInfoResponse> getInvites(Requester requester) {
        if (requester.isAdmin()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var invites = inviteRepository.findByUser_IdAndExpiredAtAfter(requester.userId(), LocalDateTime.now());
        return invites.stream().map(invite -> {
            var room = invite.getRoom();
            var motel = room.getMotel();
            return new InviteInfoResponse(
                invite.getId().toString(),
                motel.getDisplayName(),
                room.getRoomNumber(),
                motel.getOwner().getFullName(),
                invite.getExpiredAt().toInstant(ZoneOffset.UTC).toEpochMilli()
            );
        }).toList();
    }

    public void removeMember(Requester requester, UUID userId) {
        if (requester.isAdmin()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var member = memberRepository.findByUser_IdAndEndDateIsNull(userId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));

        var room = member.getRoom();
        var motel = room.getMotel();

        if (!motel.getOwner().getId().equals(requester.userId()) && !member.getUser().getId().equals(requester.userId())) {
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
            member.getUser().getFullName(),
            !member.getUser().getId().equals(requester.userId())
        ));
    }

    public MemberInfoResponse getMemberInfo(UUID memberId) {
        var member = memberRepository.findByUser_IdAndEndDateIsNull(memberId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_ID));
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
    }
}
