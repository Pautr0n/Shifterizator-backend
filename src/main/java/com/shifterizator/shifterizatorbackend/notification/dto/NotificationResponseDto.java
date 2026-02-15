package com.shifterizator.shifterizatorbackend.notification.dto;

import com.shifterizator.shifterizatorbackend.notification.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        NotificationType type,
        String title,
        String body,
        Boolean read,
        LocalDateTime createdAt,
        String relatedEntityType,
        Long relatedEntityId
) {}
