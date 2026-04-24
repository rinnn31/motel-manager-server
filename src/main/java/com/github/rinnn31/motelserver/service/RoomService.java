package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddRoomRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateRoomRequest;
import com.github.rinnn31.motelserver.dto.response.RoomInfoResponse;
import com.github.rinnn31.motelserver.entity.Motel;
import com.github.rinnn31.motelserver.entity.Room;
import com.github.rinnn31.motelserver.event.model.RoomNameChangedEvent;
import com.github.rinnn31.motelserver.event.model.RoomPriceChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    private final MemberRepository roomMemberRepository;
    
    private final MotelRepository motelRepository;

    private final ApplicationEventPublisher eventPublisher;

    public RoomService(
        RoomRepository roomRepository,
        MemberRepository roomMemberRepository, 
        MotelRepository motelRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.motelRepository = motelRepository;
        this.eventPublisher = eventPublisher;
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

    public void updateRoom(UUID requesterId, UUID roomId, UpdateRoomRequest request) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        checkOwnershipAndGetMotel(room.getMotel().getId(), requesterId);

        if (request.roomNumber() != null) {
            if (roomRepository.existsByMotel_IdAndRoomNumber(room.getMotel().getId(), request.roomNumber())) {
                throw new AppError(ErrorCode.ROOM_NUMBER_EXISTS);
            }
            room.setRoomNumber(request.roomNumber());
            eventPublisher.publishEvent(new RoomNameChangedEvent(
                room.getMotel().getId().toString(),
                room.getId().toString(),
                request.roomNumber()
            ));
        }
        if (request.roomPrice() != null) {
            room.setPrice(request.roomPrice());
            eventPublisher.publishEvent(new RoomPriceChangedEvent(
                room.getMotel().getId().toString(),
                room.getId().toString(),
                request.roomPrice()
            ));
        }
    }
}
