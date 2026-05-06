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

import com.github.rinnn31.motelserver.dto.request.InviteMemberRequest;
import com.github.rinnn31.motelserver.dto.response.InviteInfoResponse;
import com.github.rinnn31.motelserver.dto.response.MemberInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.service.MemberService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/rooms/{roomId}/members")
    public List<MemberInfoResponse> getRoomMembersByRoomId(@PathVariable UUID roomId) {
        Requester requester = Requester.fromContext();
        return memberService.getRoomMembersByRoomId(roomId, requester);
    }

    @GetMapping("/motels/{motelId}/members")
    public List<MemberInfoResponse> getRoomMembersByMotelId(@PathVariable UUID motelId) {
        Requester requester = Requester.fromContext();
        return memberService.getRoomMembersByMotelId(motelId, requester);
    }

    @PostMapping("/members/leave")
    public void leaveRoom() {
        Requester requester = Requester.fromContext();
        memberService.removeMember(requester, requester.userId());
    }

    @PostMapping("/members/remove")
    public void removeMember(@RequestParam UUID userId) {
        Requester requester = Requester.fromContext();
        memberService.removeMember(requester, userId);
    }

    @PostMapping("/members/accept")
    public void acceptInvite(@RequestParam UUID inviteId) {
        Requester requester = Requester.fromContext();
        memberService.acceptInvite(requester, inviteId);
    }

    @PostMapping("/members/reject")
    public void rejectInvite(@RequestParam String inviteId) {
        Requester requester = Requester.fromContext();
        memberService.rejectInvite(requester, UUID.fromString(inviteId));
    }

    @PostMapping("/members/invite")
    public void inviteMember(@Valid @RequestBody InviteMemberRequest request) {
        Requester requester = Requester.fromContext();
        memberService.inviteMember(requester, request);
    }

    @GetMapping("/members/invites")
    public List<InviteInfoResponse> getInvites() {
        Requester requester = Requester.fromContext();
        return memberService.getInvites(requester);
    }
}
