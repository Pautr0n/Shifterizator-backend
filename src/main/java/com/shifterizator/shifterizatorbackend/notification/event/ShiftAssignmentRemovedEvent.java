package com.shifterizator.shifterizatorbackend.notification.event;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ShiftAssignmentRemovedEvent extends ApplicationEvent {

    private final ShiftAssignment assignment;

    public ShiftAssignmentRemovedEvent(ShiftAssignment assignment) {
        super(assignment);
        this.assignment = assignment;
    }
}
