package com.github.rinnn31.motelserver.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.SendMessageRequest;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.MessageInfoResponse;
import com.github.rinnn31.motelserver.entity.Message;
import com.github.rinnn31.motelserver.entity.MessageRecipient;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MessageRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.service.external.ObjectStorageService;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final ObjectStorageService storageService;

    private final RoomRepository roomRepository;

    private final RoomMemberRepository roomMemberRepository;

    private final MotelRepository motelRepository;

    public static final String SENT_BOX = "sent";

    public static final String RECEIVED_BOX = "received";

    public static final String ROOM_OBJECT_TYPE = "room";

    public static final String MOTEL_OBJECT_TYPE = "motel";

    private static final int MAX_ATTACHMENT_SIZE_BYTES = 50 * 1024 * 1024;

    private static final String[] ALLOWED_ATTACHMENT_TYPES = new String[] {
        "image/jpeg",
        "image/png",
        "video/mp4",
        "video/quicktime"
    };

    public MessageService(MessageRepository messageRepository, ObjectStorageService storageService, RoomRepository roomRepository, RoomMemberRepository roomMemberRepository, MotelRepository motelRepository) {
        this.messageRepository = messageRepository;
        this.storageService = storageService;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.motelRepository = motelRepository;
    }

    public List<MediaPresignedUrlResponse> sendMessage(UUID senderId, String sendObjectType, SendMessageRequest request) {
        switch (sendObjectType) {
            case ROOM_OBJECT_TYPE -> {
                return sendFromRoomToMotel(senderId, request);
            }
            case MOTEL_OBJECT_TYPE -> {
                return sendFromMotelToRoom(senderId, request);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
    }

    private List<MediaPresignedUrlResponse> sendFromMotelToRoom(UUID senderId, SendMessageRequest request) {
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

        var atatchmentUrls = createAttachmentUrls(request.attachmentContentTypes());

        var message = new Message();
        message.setTitle(request.title());
        message.setContent(request.content());
        message.setMotelSender(motel);
        message.setAttachmentUrls(atatchmentUrls.stream().map(MediaPresignedUrlResponse::key).toList());
        message.setRecipients(rooms.stream().map(room -> {
            var recipient = new MessageRecipient();
            recipient.setRoomRecipient(room);
            return recipient;
        }).toList());
        message.setCreatedAt(Instant.now());
        messageRepository.save(message);

        return atatchmentUrls;
    }

    private List<MediaPresignedUrlResponse> createAttachmentUrls(List<String> attachmentContentTypes) {
        if (attachmentContentTypes == null || attachmentContentTypes.isEmpty()) {
            return List.of();
        }

        List<MediaPresignedUrlResponse> attachmentUrls = new ArrayList<>();
        for (var contentType : attachmentContentTypes) {
            if (contentType == null || contentType.isBlank() || !List.of(ALLOWED_ATTACHMENT_TYPES).contains(contentType)) {
                throw new AppError(ErrorCode.INVALID_FILE_TYPE);
            }
            attachmentUrls.add(storageService.generatePresignedUrl(contentType, "attachments", MAX_ATTACHMENT_SIZE_BYTES));
        }
        return attachmentUrls;
    }

    private List<MediaPresignedUrlResponse> sendFromRoomToMotel(UUID senderId, SendMessageRequest request) {
        var roomMember = roomMemberRepository.findByUser_IdAndEndDateIsNull(senderId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        var room = roomMember.getRoom();
        var motel = room.getMotel();

        var atatchmentUrls = createAttachmentUrls(request.attachmentContentTypes());

        var recipient = new MessageRecipient();
        recipient.setMotelRecipient(motel);
        var message = new Message();
        message.setTitle(request.title());
        message.setContent(request.content());
        message.setRoomSender(room);
        message.setAttachmentUrls(atatchmentUrls.stream().map(MediaPresignedUrlResponse::key).toList());
        message.setRecipients(List.of(recipient));
        message.setCreatedAt(Instant.now());
        messageRepository.save(message);

        return atatchmentUrls;
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
                messages = messageRepository.findByRoomSender_IdOrderByCreatedAtDesc(roomId);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByRoomRecipient_Id(roomId);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                message.getContent(),
                message.getAttachmentUrls().stream().map(storageService::getPublicUrl).toList(),
                message.getCreatedAt().toEpochMilli(),
                SENT_BOX.equals(box) ? roomId.toString() : message.getMotelSender().getId().toString(),
                List.of(RECEIVED_BOX.equals(box) ? roomId.toString() : message.getRecipients().get(0).getMotelRecipient().getId().toString())
            )).toList();
    }

    public List<MessageInfoResponse> getMessagesForMotel(UUID requesterId, UUID motelId, String box) {
        if (!motelRepository.existsByIdAndOwner_Id(motelId, requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        List<Message> messages;
        switch (box) {
            case SENT_BOX -> {
                messages = messageRepository.findByMotelSender_IdOrderByCreatedAtDesc(motelId);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByMotelRecipient_Id(motelId);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                message.getContent(),
                message.getAttachmentUrls().stream().map(storageService::getPublicUrl).toList(),
                message.getCreatedAt().toEpochMilli(),
                SENT_BOX.equals(box) ? motelId.toString() : message.getRoomSender().getId().toString(),
                message.getRecipients().stream().map(recipient -> SENT_BOX.equals(box) ? recipient.getRoomRecipient().getId().toString() : recipient.getMotelRecipient().getId().toString()).toList()
            )).toList();
    }

}
