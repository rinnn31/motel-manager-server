package com.github.rinnn31.motelserver.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.response.MemberInfoResponse;
import com.github.rinnn31.motelserver.dto.response.MotelInfoResponse;
import com.github.rinnn31.motelserver.dto.response.RoomInfoResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.service.MemberService;
import com.github.rinnn31.motelserver.service.MotelService;
import com.github.rinnn31.motelserver.service.RoomService;

@RestController
@RequestMapping("/api/admin")
public class AdminMotelController {
    private final MotelService motelService;

    private final RoomService roomService;

    private final MemberService memberService;

    public AdminMotelController(MotelService motelService, RoomService roomService, MemberService memberService) {
        this.motelService = motelService;
        this.roomService = roomService;
        this.memberService = memberService;
    }

    @GetMapping("/motels")
    public List<MotelInfoResponse> getAllMotels(
        @RequestParam(required = false) UUID landlordId
    ) {
        if (landlordId != null) {
            return motelService.getMotelsOfLandlord(landlordId);
        } else {
            return motelService.getAllMotels();
        }
    }

    @GetMapping("/motels/joined")
    public MotelInfoResponse getJoinedMotel(
        @RequestParam UUID tenantId
    ) {
        return motelService.getJoinedMotelInfo(tenantId);
    }

    @GetMapping("/members/{memberId}")
    public MemberInfoResponse getMemberInfo(@PathVariable UUID memberId) {
        return memberService.getMemberInfo(memberId);
    }

    @GetMapping("/motels/{motelId}")
    public MotelInfoResponse getMotel(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return motelService.getMotelInfo(motelId, requester);
    }

    @GetMapping("/motels/{motelId}/landlord")
    public UserInfoResponse getMotelLandlord(@PathVariable UUID motelId) {
        return motelService.getMotelOwnerInfo(motelId, Requester.fromContext());
    }

    @GetMapping("/motels/{motelId}/rooms")
    public List<RoomInfoResponse> getRoomsInMotel(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return roomService.getRooms(requester, motelId);
    }

    @GetMapping("/rooms/{roomId}/members")
    public List<MemberInfoResponse> getMembersInRoom(@PathVariable UUID roomId) {
        Requester requester = Requester.fromContext();
        return memberService.getRoomMembersByRoomId(roomId, requester);
    }

    @GetMapping("/motels/{motelId}/members")
    public List<MemberInfoResponse> getMembersInMotel(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return memberService.getRoomMembersByMotelId(motelId, requester);
    }
}
