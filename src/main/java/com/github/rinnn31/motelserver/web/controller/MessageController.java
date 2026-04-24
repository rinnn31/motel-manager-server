package com.github.rinnn31.motelserver.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.github.rinnn31.motelserver.security.UserExtractor;
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
        UUID senderId = UserExtractor.extractUserIdFromContext();
        return messageService.sendMessage(senderId, sendObjectType, request);
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
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return messageService.getMessages(requesterId, objectId, objectType, box, from, to, page, size);
    }

    @GetMapping("/{messageId}")
    public MessageInfoResponse getMessageDetails(@PathVariable UUID messageId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return messageService.getMessageDetails(requesterId, messageId);
    }

}
