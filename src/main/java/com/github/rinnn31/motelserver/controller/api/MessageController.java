package com.github.rinnn31.motelserver.controller.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.SendMessageRequest;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.MessageInfoResponse;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public List<MediaPresignedUrlResponse> sendMessage(
        @RequestParam String sendObjectType,

        @Valid @RequestBody SendMessageRequest request
    ) {
        Requester requester = Requester.fromContext();
        return messageService.sendMessage(requester, sendObjectType, request);
    }

    @GetMapping
    public List<MessageInfoResponse> getMessages(
        @RequestParam UUID objectId, 
        @RequestParam String objectType,
        @RequestParam(defaultValue = MessageService.SENT_BOX) String box,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Requester requester = Requester.fromContext();
        return messageService.getMessages(requester, objectId, objectType, box, from, to, page, size);
    }

    @GetMapping("/{messageId}")
    public MessageInfoResponse getMessageDetails(@PathVariable UUID messageId) {
        Requester requester = Requester.fromContext();
        return messageService.getMessageDetails(requester, messageId);
    }

}
