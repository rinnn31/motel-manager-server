package com.github.rinnn31.motelserver.controller.api;

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
import com.github.rinnn31.motelserver.security.Requester;
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
        Requester requester = Requester.fromContext();
        return roomService.getRooms(requester, motelId);
    }

    @GetMapping("/{roomId}")
    public RoomInfoResponse getRoom(@PathVariable UUID roomId) {
        Requester requester = Requester.fromContext();
        return roomService.getRoomInfo(roomId, requester);
    }

    @GetMapping("/joined")
    public RoomInfoResponse getJoinedRoom(@RequestParam(required = false) UUID tenantId) {
        Requester requester = Requester.fromContext();
        if (requester.isAdmin()) {
            return roomService.getJoinedRoomInfo(tenantId);
        } else {
            return roomService.getJoinedRoomInfo(requester.userId());
        }
    }

    @PostMapping
    public void addRoom(@Valid @RequestBody AddRoomRequest request) {
        Requester requester = Requester.fromContext();
        roomService.addRoom(requester, request);
    }

    @PatchMapping("/{roomId}")
    public void updateRoom(@PathVariable UUID roomId, @Valid @RequestBody UpdateRoomRequest request) {
        Requester requester = Requester.fromContext();
        roomService.updateRoom(requester, roomId, request);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable UUID roomId) {
        Requester requester = Requester.fromContext();
        roomService.deleteRoom(requester, roomId);
    }
}
