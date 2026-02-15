package com.shifterizator.shifterizatorbackend.notification.model;

/**
 * Notification type for in-app notifications. Add new values when introducing
 * new notification kinds (e.g. holiday request accepted, schedule published).
 */
public enum NotificationType {
    SHIFT_ASSIGNED,
    SHIFT_UNASSIGNED
}
