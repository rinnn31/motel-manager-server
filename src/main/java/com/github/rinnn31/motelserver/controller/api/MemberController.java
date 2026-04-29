package com.github.rinnn31.motelserver.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.AddMemberRequest;
import com.github.rinnn31.motelserver.dto.response.MemberInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService roomMemberService;

    public MemberController(MemberService roomMemberService) {
        this.roomMemberService = roomMemberService;
    }

    @GetMapping("/by-room/{roomId}")
    public List<MemberInfoResponse> getRoomMembersByRoomId(@PathVariable String roomId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return roomMemberService.getRoomMembersByRoomId(java.util.UUID.fromString(roomId), requesterId);
    }

    @GetMapping("/by-motel/{motelId}")
    public List<MemberInfoResponse> getRoomMembersByMotelId(@PathVariable String motelId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return roomMemberService.getRoomMembersByMotelId(java.util.UUID.fromString(motelId), requesterId);
    }

    @PostMapping
    public void addRoomMember(@Valid @RequestBody AddMemberRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomMemberService.addMember(requesterId, request);
    }

    @PostMapping("/leave")
    public void leaveRoom() {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomMemberService.removeMember(requesterId, requesterId);
    }

    @PostMapping("/remove")
    public void removeMember(@RequestParam String userId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        roomMemberService.removeMember(requesterId, UUID.fromString(userId));
    }
}
