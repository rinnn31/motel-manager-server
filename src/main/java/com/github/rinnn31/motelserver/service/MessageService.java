package com.github.rinnn31.motelserver.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.SendMessageRequest;
import com.github.rinnn31.motelserver.dto.response.AttachmentInfo;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.MessageInfoResponse;
import com.github.rinnn31.motelserver.dto.response.MessageTargetInfo;
import com.github.rinnn31.motelserver.dto.response.ObjectMetadata;
import com.github.rinnn31.motelserver.entity.Message;
import com.github.rinnn31.motelserver.entity.MessageRecipient;
import com.github.rinnn31.motelserver.event.model.MessageSentEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.MessageRepository;
import com.github.rinnn31.motelserver.repository.MotelRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.service.external.ObjectStorageService;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final ObjectStorageService storageService;

    private final RoomRepository roomRepository;

    private final MemberRepository roomMemberRepository;

    private final MotelRepository motelRepository;

    private final ApplicationEventPublisher eventPublisher;

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

    public MessageService(
        MessageRepository messageRepository, 
        ObjectStorageService storageService,
        RoomRepository roomRepository, 
        MemberRepository roomMemberRepository, 
        MotelRepository motelRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.messageRepository = messageRepository;
        this.storageService = storageService;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.motelRepository = motelRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<MediaPresignedUrlResponse> sendMessage(UUID senderId, String sendObjectType,
            SendMessageRequest request) {
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
        if (roomRepository
                .countDistinctMotelByIdIn(request.targetRoomIds().stream().map(UUID::fromString).toList()) != 1) {
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
            recipient.setMessage(message);
            recipient.setRoomRecipient(room);
            return recipient;
        }).toList());
        message.setCreatedAt(Instant.now());
        messageRepository.save(message);
        
        eventPublisher.publishEvent(new MessageSentEvent.FromMotel(
            motel.getId().toString(),
            motel.getDisplayName(),
            rooms.stream().map(room -> room.getId().toString()).toList(),
            message.getId().toString(),
            message.getTitle()
        ));

        return atatchmentUrls;
    }

    private List<MediaPresignedUrlResponse> createAttachmentUrls(List<String> attachmentContentTypes) {
        if (attachmentContentTypes == null || attachmentContentTypes.isEmpty()) {
            return List.of();
        }

        List<MediaPresignedUrlResponse> attachmentUrls = new ArrayList<>();
        for (var contentType : attachmentContentTypes) {
            if (contentType == null || contentType.isBlank()
                    || !List.of(ALLOWED_ATTACHMENT_TYPES).contains(contentType)) {
                throw new AppError(ErrorCode.INVALID_FILE_TYPE);
            }
            attachmentUrls
                    .add(storageService.generatePresignedUrl(contentType, "attachments", MAX_ATTACHMENT_SIZE_BYTES));
        }
        return attachmentUrls;
    }

    private List<MediaPresignedUrlResponse> sendFromRoomToMotel(UUID senderId, SendMessageRequest request) {
        var roomMember = roomMemberRepository.findByUser_IdAndEndDateIsNull(senderId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        var room = roomMember.getRoom();
        var motel = room.getMotel();

        var atatchmentUrls = createAttachmentUrls(request.attachmentContentTypes());


        var message = new Message();
        var motelRecipient = new MessageRecipient();
        motelRecipient.setMessage(message);
        motelRecipient.setMotelRecipient(motel);
        message.setTitle(request.title());
        message.setContent(request.content());
        message.setRoomSender(room);
        message.setAttachmentUrls(atatchmentUrls.stream().map(MediaPresignedUrlResponse::key).toList());
        message.setRecipients(List.of(motelRecipient));
        message.setCreatedAt(Instant.now());
        messageRepository.save(message);

        eventPublisher.publishEvent(new MessageSentEvent.FromRoom(
            motel.getId().toString(),
            motel.getDisplayName(),
            room.getId().toString(),
            room.getRoomNumber(),
            message.getId().toString(),
            message.getTitle()
        ));

        return atatchmentUrls;
    }

    public MessageInfoResponse getMessageDetails(UUID requesterId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppError(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getMotelSender() != null) {
            boolean isSender = message.getMotelSender().getOwner().getId().equals(requesterId);
            boolean isRecipient = message.getRecipients().stream().anyMatch(recipient -> recipient
                    .getRoomRecipient() != null
                    && recipient.getRoomRecipient().getMembers().stream().anyMatch(
                            member -> member.getUser().getId().equals(requesterId) && member.getEndDate() == null));
            if (!isSender && !isRecipient) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        } else if (message.getRoomSender() != null) {
            boolean isSender = message.getRoomSender().getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(requesterId) && member.getEndDate() == null);
            boolean isRecipient = message.getRecipients().stream()
                    .anyMatch(recipient -> recipient.getMotelRecipient() != null
                            && recipient.getMotelRecipient().getOwner().getId().equals(requesterId));
            if (!isSender && !isRecipient) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        }

        List<AttachmentInfo> attachments = null;
        if (message.getAttachmentUrls() != null && !message.getAttachmentUrls().isEmpty()) {
            attachments = message.getAttachmentUrls().stream().map(key -> {
                ObjectMetadata stat = storageService.statObject(key);
                String type = stat.contentType().split("/")[0];
                if (!List.of(ALLOWED_ATTACHMENT_TYPES).contains(stat.contentType())) {
                    return null;
                }
                return new AttachmentInfo(key, type);
            }).filter(Objects::nonNull).toList();
        }
        return new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                message.getContent(),
                attachments != null && !attachments.isEmpty() ? attachments : null,
                message.getCreatedAt().toEpochMilli(),
                message.getMotelSender() != null ? new MessageTargetInfo(
                        message.getMotelSender().getId().toString(),
                        message.getMotelSender().getDisplayName(),
                        MOTEL_OBJECT_TYPE)
                        : new MessageTargetInfo(
                                message.getRoomSender().getId().toString(),
                                message.getRoomSender().getRoomNumber(),
                                ROOM_OBJECT_TYPE),
                message.getRecipients().stream().map(recipient -> {
                    if (recipient.getMotelRecipient() != null) {
                        return new MessageTargetInfo(
                                recipient.getMotelRecipient().getId().toString(),
                                recipient.getMotelRecipient().getDisplayName(),
                                MOTEL_OBJECT_TYPE);
                    } else {
                        return new MessageTargetInfo(
                                recipient.getRoomRecipient().getId().toString(),
                                recipient.getRoomRecipient().getRoomNumber(),
                                ROOM_OBJECT_TYPE);
                    }
                }).toList());
    }

    public List<MessageInfoResponse> getMessages(UUID requesterId, UUID objectId, String objectType, String box,
            LocalDate from, LocalDate to, int page, int size) {
        switch (objectType) {
            case "room" -> {
                return getMessagesForRoom(requesterId, objectId, box, from, to, page, size);
            }
            case "motel" -> {
                return getMessagesForMotel(requesterId, objectId, box, from, to, page, size);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
    }

    public List<MessageInfoResponse> getMessagesForRoom(UUID requesterId, UUID roomId, String box, LocalDate from, LocalDate to, int page,
            int size) {
        var member = roomMemberRepository.findByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId)
                .orElseThrow(() -> new AppError(ErrorCode.INVALID_OPERATION));
        // Room member can only see messages from the time they joined
        Instant fromTime = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        Instant toTime = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.now().plusSeconds(1);
        if (fromTime.isAfter(Instant.now())) {
            fromTime = Instant.EPOCH;
        }
        if (member.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC).isAfter(fromTime)) {
            fromTime = member.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (fromTime.isAfter(toTime)) {
            toTime = fromTime.plusSeconds(1);
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages;
        switch (box) {
            case SENT_BOX -> {
                messages = messageRepository.findByRoomSender_IdAndCreatedAtBetweenOrderByCreatedAtDesc(roomId,
                        fromTime, toTime, pageable);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByRoomRecipient_Id(roomId, fromTime, toTime, pageable);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                null,
                null,
                message.getCreatedAt().toEpochMilli(),
                new MessageTargetInfo(
                        SENT_BOX.equals(box) ? message.getRoomSender().getId().toString()
                                : message.getMotelSender().getId().toString(),
                        SENT_BOX.equals(box) ? message.getRoomSender().getRoomNumber()
                                : message.getMotelSender().getDisplayName(),
                        SENT_BOX.equals(box) ? ROOM_OBJECT_TYPE : MOTEL_OBJECT_TYPE),
                message.getRecipients().stream().map(recipient -> new MessageTargetInfo(
                        SENT_BOX.equals(box) ? recipient.getMotelRecipient().getId().toString()
                                : recipient.getRoomRecipient().getId().toString(),
                        SENT_BOX.equals(box) ? recipient.getMotelRecipient().getDisplayName()
                                : recipient.getRoomRecipient().getRoomNumber(),
                        SENT_BOX.equals(box) ? MOTEL_OBJECT_TYPE : ROOM_OBJECT_TYPE)).toList()))
                .toList();
    }

    public List<MessageInfoResponse> getMessagesForMotel(UUID requesterId, UUID motelId, String box, LocalDate from, LocalDate to,
            int page, int size) {
        if (!motelRepository.existsByIdAndOwner_Id(motelId, requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);
        }

        Instant fromTime = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        Instant toTime = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.now().plusSeconds(1);
        if (fromTime.isAfter(Instant.now())) {
            fromTime = Instant.EPOCH;
        }
        if (fromTime.isAfter(toTime)) {
            toTime = fromTime.plusSeconds(1);
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages;
        switch (box) {
            case SENT_BOX -> {
                messages = messageRepository.findByMotelSender_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        motelId, fromTime, toTime, pageable);
            }
            case RECEIVED_BOX -> {
                messages = messageRepository.findByMotelRecipient_Id(motelId, fromTime, toTime, pageable);
            }
            default -> throw new AppError(ErrorCode.INVALID_OPERATION);
        }
        return messages.stream().map(message -> new MessageInfoResponse(
                message.getId().toString(),
                message.getTitle(),
                null,
                null,
                message.getCreatedAt().toEpochMilli(),
                new MessageTargetInfo(
                        SENT_BOX.equals(box) ? message.getMotelSender().getId().toString()
                                : message.getRoomSender().getId().toString(),
                        SENT_BOX.equals(box) ? message.getMotelSender().getDisplayName()
                                : message.getRoomSender().getRoomNumber(),
                        SENT_BOX.equals(box) ? MOTEL_OBJECT_TYPE : ROOM_OBJECT_TYPE),
                message.getRecipients().stream().map(recipient -> new MessageTargetInfo(
                        SENT_BOX.equals(box) ? recipient.getRoomRecipient().getId().toString()
                                : recipient.getMotelRecipient().getId().toString(),
                        SENT_BOX.equals(box) ? recipient.getRoomRecipient().getRoomNumber()
                                : recipient.getMotelRecipient().getDisplayName(),
                        SENT_BOX.equals(box) ? ROOM_OBJECT_TYPE : MOTEL_OBJECT_TYPE)).toList()))
                .toList();
    }
}
