package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.AddRoomRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateRoomRequest;
import com.github.rinnn31.motelserver.dto.response.RoomInfoResponse;
import com.github.rinnn31.motelserver.entity.Room;
import com.github.rinnn31.motelserver.event.model.RoomNameChangedEvent;
import com.github.rinnn31.motelserver.event.model.RoomPriceChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.security.Requester;

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

    public RoomInfoResponse getRoomInfo(UUID roomId, Requester requester) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var motel = room.getMotel();

        boolean isOwnerOrAdmin = requester.isAdmin() || motel.getOwner().getId().equals(requester.userId());
        boolean isRoomMember = !isOwnerOrAdmin && roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requester.userId(), roomId);

        if (!isOwnerOrAdmin && !isRoomMember) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        Integer memberCount = isOwnerOrAdmin || isRoomMember ? roomMemberRepository.countByRoom_IdAndEndDateIsNull(roomId) : null;
        return new RoomInfoResponse(
            room.getId().toString(), 
            room.getRoomNumber(), 
            isOwnerOrAdmin || isRoomMember ? room.getPrice() : null, 
            memberCount
        );
    }

    public List<RoomInfoResponse> getRooms(Requester requester, UUID motelId) {
        var motel = motelRepository.findById(motelId)
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));

        boolean isOwnerOrAdmin = requester.isAdmin() || motel.getOwner().getId().equals(requester.userId());
        if (!isOwnerOrAdmin) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var rooms = roomRepository.findByMotel_Id(motelId);
        return rooms.stream().map(room -> {
            Integer memberCount = roomMemberRepository.countByRoom_IdAndEndDateIsNull(room.getId());
            return new RoomInfoResponse(
                room.getId().toString(),
                room.getRoomNumber(),
                room.getPrice(),
                memberCount
            );
        }).toList();
    }

    public RoomInfoResponse getJoinedRoomInfo(UUID userId) {
        if (userId == null) {
            return null;
        }
        var member = roomMemberRepository.findByUser_IdAndEndDateIsNull(userId)
                .orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var room = member.getRoom();
        return new RoomInfoResponse(
            room.getId().toString(),
            room.getRoomNumber(),
            room.getPrice(),
            room.getMembers().size()
        );
    }

    public void addRoom(Requester requester, AddRoomRequest request) {
        if (requester.isAdmin()) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        var motel = motelRepository.findById(UUID.fromString(request.motelId()))
                .orElseThrow(() -> new AppError(ErrorCode.MOTEL_NOT_FOUND));
        if (!motel.getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        if (roomRepository.existsByMotel_IdAndRoomNumber(UUID.fromString(request.motelId()), request.roomNumber())) {
            throw new AppError(ErrorCode.ROOM_NUMBER_EXISTS);
        }

        var room = new Room();
        room.setMotel(motel);
        room.setRoomNumber(request.roomNumber());
        room.setPrice(request.price());
        roomRepository.save(room);
    }

    public void deleteRoom(Requester requester, UUID roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        if (!room.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        if (roomMemberRepository.existsByRoom_IdAndEndDateIsNull(roomId)) {
            throw new AppError(ErrorCode.ROOM_HAS_MEMBERS);
        }

        roomRepository.delete(room);
    }

    public void updateRoom(Requester requester, UUID roomId, UpdateRoomRequest request) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        if (!room.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

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
