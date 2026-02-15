package com.shifterizator.shifterizatorbackend.notification.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class NotificationNotFoundException extends DomainNotFoundException {

    public NotificationNotFoundException(String message) {
        super(message);
    }
}
