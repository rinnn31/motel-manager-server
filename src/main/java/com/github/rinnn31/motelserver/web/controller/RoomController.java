package com.github.rinnn31.motelserver.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.AddRoomRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateRoomRequest;
import com.github.rinnn31.motelserver.dto.response.RoomInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomInfoResponse> getRooms(@RequestParam UUID motelId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return roomService.getRooms(requesterId, motelId);
    }

    @GetMapping("/{roomId}")
    public RoomInfoResponse getRoom(@PathVariable UUID roomId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return roomService.getRoomInfo(requesterId, roomId);
    }

    @GetMapping("/joined")
    public RoomInfoResponse getJoinedRoom() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return roomService.getJoinedRoomInfo(requesterId);
    }

    @PostMapping
    public void addRoom(@Valid @RequestBody AddRoomRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomService.addRoom(requesterId, request);
    }

    @PatchMapping("/{roomId}")
    public void updateRoom(@PathVariable UUID roomId, @Valid @RequestBody UpdateRoomRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomService.updateRoom(requesterId, roomId, request);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable UUID roomId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomService.deleteRoom(requesterId, roomId);
    }
}
