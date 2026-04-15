package com.github.rinnn31.motelserver.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.rinnn31.motelserver.dto.request.SendMessageRequest;
import com.github.rinnn31.motelserver.entity.ObjectType;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.MessageService;
import com.github.rinnn31.motelserver.utils.ValidFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendMessage(
        @RequestPart("sendType") ObjectType sendType,

        @RequestPart("request")
        @Valid SendMessageRequest request, 
        
        @RequestPart(value = "images", required = false)
        @Size(max = 5, message = "Không được gửi quá 5 hình ảnh")
        List<@ValidFile(allowedTypes = {"image/jpeg", "image/png"}, message = "Chỉ cho phép gửi file hình ảnh có định dạng JPEG hoặc PNG") MultipartFile> imageFiles
    ) {
        UUID senderId = UserExtractor.extractUserIdFromContext();
        messageService.sendMessage(senderId, sendType, request, imageFiles);
    }

    @GetMapping
    public List<String> getMessages(@RequestParam UUID objectId, @RequestParam ObjectType objectType, @RequestParam String box) {
        return null;
    }

}
