package com.github.rinnn31.motelserver.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageInfoResponse(
    String id,
    String title,
    String content,
    List<String> attachmentKeys,
    Long createdAt,
    MessageTargetInfo sender,
    List<MessageTargetInfo> recipients
) {
    
}
