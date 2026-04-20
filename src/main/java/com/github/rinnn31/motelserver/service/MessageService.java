package com.github.rinnn31.motelserver.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.rinnn31.motelserver.dto.request.SendMessageRequest;
import com.github.rinnn31.motelserver.dto.response.MessageInfoResponse;
import com.github.rinnn31.motelserver.entity.Message;
import com.github.rinnn31.motelserver.entity.MessageRecipient;
import com.github.rinnn31.motelserver.entity.ObjectType;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MessageRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.service.external.StorageService;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final StorageService storageService;

    private final RoomRepository roomRepository;

    private final RoomMemberRepository roomMemberRepository;

    private final MotelRepository motelRepository;

    public static final String SENT_BOX = "sent";

    public static final String RECEIVED_BOX = "received";

    public MessageService(MessageRepository messageRepository, StorageService storageService, RoomRepository roomRepository, RoomMemberRepository roomMemberRepository, MotelRepository motelRepository) {
        this.messageRepository = messageRepository;
        this.storageService = storageService;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.motelRepository = motelRepository;
    }

    public void sendMessage(UUID senderId, ObjectType senderType, SendMessageRequest request, List<MultipartFile> images) {
        if (senderType == ObjectType.MOTEL) {
            sendFromMotelToRoom(senderId, request, images);
        } else {
            sendFromRoomToMotel(senderId, request, images);
        }
    }

    private void sendFromMotelToRoom(UUID senderId, SendMessageRequest request, List<MultipartFile> images) {
        if (roomRepository.countDistinctMotelByIdIn(request.targetRoomIds().stream().map(UUID::fromString).toList()) != 1) {
            throw new AppError(ErrorCode.ROOM_NOT_SAME_MOTEL);
        }
        var rooms = roomRepository.findByIdIn(request.targetRoomIds().stream().map(UUID::fromString).toList());
        if (rooms.size() != request.targetRoomIds().size()) {
            throw new AppError(ErrorCode.ROOM_NOT_FOUND);
        }        
        var motel = rooms.get(0).getMotel();
        if (!motel.getOwner().getId().equals(senderId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }   
        
        var recipients = rooms.stream().map(room -> {
            var recipient = new MessageRecipient();
            recipient.setRecipientId(room.getId());
            return recipient;
        }).toList();
        saveMessage(request.title(), request.content(), senderId, uploadImages(images), ObjectType.MOTEL, recipients);
    
    }

    private void sendFromRoomToMotel(UUID senderId, SendMessageRequest request, List<MultipartFile> images) {
        var roomMember = roomMemberRepository.findByUser_IdAndEndDateIsNull(senderId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        var room = roomMember.getRoom();
        var motel = room.getMotel();

        var recipient = new MessageRecipient();
        recipient.setRecipientId(motel.getId());
        saveMessage(request.title(), request.content(), senderId, uploadImages(images), ObjectType.ROOM, List.of(recipient));
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<String> imageUrls = new ArrayList<>();
        for (var image : images) {
            String storedName = "attachments/" + UUID.randomUUID();
            String storedUrl;
            try {
                storedUrl = storageService.uploadFile(image.getBytes(), storedName);
            } catch (IOException e) {
                throw new AppError(ErrorCode.FILE_UPLOAD_FAILED);
            }
            imageUrls.add(storedUrl);
        }
        return imageUrls;
    }

    private void saveMessage(String title, String content, UUID senderId, List<String> imageUrls, ObjectType objectType, List<MessageRecipient> recipients) {
        var message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setSenderId(senderId);
        message.setImageUrls(imageUrls);
        message.setObjectType(objectType);
        message.setCreatedAt(Instant.now());
        message.setRecipients(recipients);

        messageRepository.save(message);
    }

    public List<MessageInfoResponse> getMessages(UUID requesterId, UUID objectId, String objectType, String box) {
        switch (objectType) {
            case "room" -> {
                return getMessagesForRoom(requesterId, objectId, box);
            }
            case "motel" -> {
                return getMessagesForMotel(requesterId, objectId, box);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
    }

    public List<MessageInfoResponse> getMessagesForRoom(UUID requesterId, UUID roomId, String box) {
        if (!roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        
        List<Message> messages;
        switch (box) {
            case SENT_BOX -> {
                messages = messageRepository.findBySenderIdAndObjectTypeOrderByCreatedAtDesc(roomId, ObjectType.ROOM);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByRecipientIdAndObjectTypeOrderByCreatedAtDesc(roomId, ObjectType.ROOM);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                message.getContent(),
                message.getImageUrls(),
                message.getCreatedAt().toEpochMilli(),
                message.getSenderId().toString(),
                List.of(RECEIVED_BOX.equals(box) ? roomId.toString() : message.getRecipients().get(0).getRecipientId().toString())
            )).toList();
    }

    public List<MessageInfoResponse> getMessagesForMotel(UUID requesterId, UUID motelId, String box) {
        if (!motelRepository.existsByIdAndOwner_Id(motelId, requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        List<Message> messages;
        switch (box) {
            case SENT_BOX -> {
                messages = messageRepository.findBySenderIdAndObjectTypeOrderByCreatedAtDesc(motelId, ObjectType.MOTEL);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByRecipientIdAndObjectTypeOrderByCreatedAtDesc(motelId, ObjectType.MOTEL);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                message.getContent(),
                message.getImageUrls(),
                message.getCreatedAt().toEpochMilli(),
                message.getSenderId().toString(),
                message.getRecipients().stream().map(recipient -> recipient.getRecipientId().toString()).toList()
            )).toList();
    }

}
