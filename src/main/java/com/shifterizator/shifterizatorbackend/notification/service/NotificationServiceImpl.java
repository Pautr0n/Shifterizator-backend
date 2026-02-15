package com.shifterizator.shifterizatorbackend.notification.service;

import com.shifterizator.shifterizatorbackend.notification.dto.NotificationResponseDto;
import com.shifterizator.shifterizatorbackend.notification.exception.NotificationNotFoundException;
import com.shifterizator.shifterizatorbackend.notification.model.Notification;
import com.shifterizator.shifterizatorbackend.notification.model.NotificationType;
import com.shifterizator.shifterizatorbackend.notification.repository.NotificationRepository;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String RELATED_ENTITY_TYPE_SHIFT_ASSIGNMENT = "SHIFT_ASSIGNMENT";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationRepository notificationRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> findByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .filter(notification -> notification.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found or access denied"));
        n.setRead(true);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void createForShiftAssignmentCreated(Long assignmentId) {
        ShiftAssignment assignment = shiftAssignmentRepository
                .findByIdWithShiftInstanceAndEmployeeUser(assignmentId)
                .orElse(null);
        if (assignment == null || assignment.getEmployee().getUser() == null) {
            return;
        }
        User user = assignment.getEmployee().getUser();
        var si = assignment.getShiftInstance();
        String locationName = si.getLocation() != null ? si.getLocation().getName() : "—";
        String title = "New shift assignment";
        String body = String.format("You have been assigned to a shift on %s, %s–%s at %s.",
                si.getDate().format(DATE_FMT),
                si.getStartTime().format(TIME_FMT),
                si.getEndTime().format(TIME_FMT),
                locationName);
        create(user, NotificationType.SHIFT_ASSIGNED, title, body, RELATED_ENTITY_TYPE_SHIFT_ASSIGNMENT, assignment.getId());
    }

    @Override
    @Transactional
    public void createForShiftAssignmentRemoved(Long assignmentId) {
        ShiftAssignment assignment = shiftAssignmentRepository
                .findByIdWithShiftInstanceAndEmployeeUser(assignmentId)
                .orElse(null);
        if (assignment == null || assignment.getEmployee().getUser() == null) {
            return;
        }
        User user = assignment.getEmployee().getUser();
        var si = assignment.getShiftInstance();
        String locationName = si.getLocation() != null ? si.getLocation().getName() : "—";
        String title = "Shift unassigned";
        String body = String.format("You have been unassigned from the shift on %s, %s–%s at %s.",
                si.getDate().format(DATE_FMT),
                si.getStartTime().format(TIME_FMT),
                si.getEndTime().format(TIME_FMT),
                locationName);
        create(user, NotificationType.SHIFT_UNASSIGNED, title, body, RELATED_ENTITY_TYPE_SHIFT_ASSIGNMENT, assignment.getId());
    }

    private void create(User user, NotificationType type, String title, String body,
                        String relatedEntityType, Long relatedEntityId) {
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    private NotificationResponseDto toDto(Notification n) {
        return new NotificationResponseDto(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getRead(),
                n.getCreatedAt(),
                n.getRelatedEntityType(),
                n.getRelatedEntityId()
        );
    }
}
