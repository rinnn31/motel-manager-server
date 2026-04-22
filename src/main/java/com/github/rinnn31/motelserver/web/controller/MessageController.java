package com.github.rinnn31.motelserver.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        @RequestParam(required = false) Long from,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return messageService.getMessages(requesterId, objectId, objectType, box, from, page, size);
    }

    @GetMapping("/{messageId}")
    public MessageInfoResponse getMessageDetails(@RequestParam UUID messageId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return messageService.getMessageDetails(requesterId, messageId);
    }

}
