package com.github.rinnn31.motelserver.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddRoomRequest;
import com.github.rinnn31.motelserver.dto.response.RoomInfoResponse;
import com.github.rinnn31.motelserver.dto.response.RoomMemberResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.Room;
import com.github.rinnn31.motelserver.entity.RoomMember;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    private final UserRepository userRepository;

    private final RoomMemberRepository roomMemberRepository;
    
    private final MotelRepository motelRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, RoomMemberRepository roomMemberRepository, MotelRepository motelRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.motelRepository = motelRepository;
    }

    public RoomInfoResponse getRoomInfo(UUID roomId, UUID requesterId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var motel = room.getMotel();

        boolean isOwner = motel.getOwner().getId().equals(requesterId);
        boolean isRoomMember = roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId);

        if (!isOwner && !isRoomMember) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        Integer memberCount = isOwner || isRoomMember ? roomMemberRepository.countByRoom_IdAndEndDateIsNull(roomId) : null;
        return new RoomInfoResponse(
            room.getId().toString(), 
            room.getRoomNumber(), 
            isOwner || isRoomMember ? room.getPrice() : null, 
            memberCount
        );
    }

    public List<RoomInfoResponse> getRooms(UUID requesterId, UUID motelId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));

        boolean isOwner = motel.getOwner().getId().equals(requesterId);
        boolean isMotelMember = roomMemberRepository.existsByRoom_Motel_IdAndUser_IdAndEndDateIsNull(motelId, requesterId);

        if (!isOwner && !isMotelMember) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var rooms = roomRepository.findByMotel_Id(motelId);
        return rooms.stream().map(room -> {
            boolean isRoomMember = roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, room.getId());
            Integer memberCount = isRoomMember || isOwner ? roomMemberRepository.countByRoom_IdAndEndDateIsNull(room.getId()) : null;
            return new RoomInfoResponse(
                room.getId().toString(),
                room.getRoomNumber(),
                isOwner || isRoomMember ? room.getPrice() : null,
                memberCount
            );
        }).toList();
    }

    public RoomInfoResponse getJoinedRoomInfo(UUID requesterId) {
        var member = roomMemberRepository.findByUser_IdAndEndDateIsNull(requesterId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var room = member.getRoom();
        return new RoomInfoResponse(
            room.getId().toString(),
            room.getRoomNumber(),
            room.getPrice(),
            room.getMembers().size()
        );
    }

    private Motel checkOwnershipAndGetMotel(UUID motelId, UUID ownerId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(ownerId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return motel;
    }

    public void addRoom(UUID ownerId, AddRoomRequest request) {   
        var motel = checkOwnershipAndGetMotel(UUID.fromString(request.motelId()), ownerId);
        if (roomRepository.existsByMotel_IdAndRoomNumber(UUID.fromString(request.motelId()), request.roomNumber())) {
            throw new AppError(ErrorCode.ROOM_NUMBER_EXISTS);
        }

        var room = new Room();
        room.setMotel(motel);
        room.setRoomNumber(request.roomNumber());
        room.setPrice(request.price());
        roomRepository.save(room);
    }

    public void deleteRoom(UUID ownerId, UUID roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        checkOwnershipAndGetMotel(room.getMotel().getId(), ownerId);

        if (roomMemberRepository.existsByRoom_IdAndEndDateIsNull(roomId)) {
            throw new AppError(ErrorCode.ROOM_HAS_MEMBERS);
        }

        roomRepository.delete(room);
    }

    public void changeRoomPrice(UUID ownerId, UUID roomId, int newPrice) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        checkOwnershipAndGetMotel(room.getMotel().getId(), ownerId);

        room.setPrice(newPrice);
        roomRepository.save(room);
    }

    public void changeRoomNumber(UUID ownerId, UUID roomId, String newRoomNumber) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        var motelId = room.getMotel().getId();
        checkOwnershipAndGetMotel(motelId, ownerId);

        if (roomRepository.existsByMotel_IdAndRoomNumber(motelId, newRoomNumber)) {
            throw new AppError(ErrorCode.ROOM_NUMBER_EXISTS);
        }

        room.setRoomNumber(newRoomNumber);
        roomRepository.save(room);
    }

    public void addMember(UUID ownerId, UUID roomId, UUID userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        checkOwnershipAndGetMotel(room.getMotel().getId(), ownerId);

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
    }

    public void removeMember(UUID landlordId, UUID roomId, UUID userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        checkOwnershipAndGetMotel(room.getMotel().getId(), landlordId);

        var member = roomMemberRepository.findByUser_IdAndRoom_IdAndEndDateIsNull(userId, roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        member.setEndDate(LocalDate.now());
        roomMemberRepository.save(member);
    }

    public List<RoomMemberResponse> getRoomMembers(UUID requesterId, UUID roomId) {
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
                user.getFullName(),
                user.getPhoneNumber(),
                member.getStartDate()
            );
        }).toList();
    }
}
