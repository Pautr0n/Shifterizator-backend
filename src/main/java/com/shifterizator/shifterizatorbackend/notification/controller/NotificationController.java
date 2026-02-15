package com.shifterizator.shifterizatorbackend.notification.controller;

import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.notification.dto.NotificationResponseDto;
import com.shifterizator.shifterizatorbackend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications for the current user (e.g. shift assigned/unassigned)")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @Operation(
            summary = "List my notifications",
            description = "Returns paginated notifications for the current user, newest first.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of notifications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> listMine(Pageable pageable) {
        Long userId = currentUserService.getCurrentUser().getId();
        Page<NotificationResponseDto> page = notificationService.findByUserId(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Unread count",
            description = "Returns the number of unread notifications for the current user.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> unreadCount() {
        Long userId = currentUserService.getCurrentUser().getId();
        int count = notificationService.countUnreadByUserId(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Marks a single notification as read. Only the owner can mark it.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable Long id
    ) {
        Long userId = currentUserService.getCurrentUser().getId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mark all as read",
            description = "Marks all notifications of the current user as read.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All marked as read"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = currentUserService.getCurrentUser().getId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
