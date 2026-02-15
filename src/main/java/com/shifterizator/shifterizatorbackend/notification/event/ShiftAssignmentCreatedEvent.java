package com.shifterizator.shifterizatorbackend.notification.event;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ShiftAssignmentCreatedEvent extends ApplicationEvent {

    private final ShiftAssignment assignment;

    public ShiftAssignmentCreatedEvent(ShiftAssignment assignment) {
        super(assignment);
        this.assignment = assignment;
    }
}
