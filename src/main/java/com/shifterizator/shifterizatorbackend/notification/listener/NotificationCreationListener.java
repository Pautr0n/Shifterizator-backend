package com.shifterizator.shifterizatorbackend.notification.listener;

import com.shifterizator.shifterizatorbackend.notification.event.ShiftAssignmentCreatedEvent;
import com.shifterizator.shifterizatorbackend.notification.event.ShiftAssignmentRemovedEvent;
import com.shifterizator.shifterizatorbackend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationCreationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShiftAssignmentCreated(ShiftAssignmentCreatedEvent event) {
        notificationService.createForShiftAssignmentCreated(event.getAssignment().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShiftAssignmentRemoved(ShiftAssignmentRemovedEvent event) {
        notificationService.createForShiftAssignmentRemoved(event.getAssignment().getId());
    }
}
