package com.shifterizator.shifterizatorbackend.notification.service;

import com.shifterizator.shifterizatorbackend.notification.dto.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponseDto> findByUserId(Long userId, Pageable pageable);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    int countUnreadByUserId(Long userId);

    void createForShiftAssignmentCreated(Long assignmentId);

    void createForShiftAssignmentRemoved(Long assignmentId);
}
