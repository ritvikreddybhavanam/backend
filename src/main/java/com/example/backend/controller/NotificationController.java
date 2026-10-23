package com.example.backend.controller;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public NotificationController(
            NotificationService notificationService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> getMyNotifications(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            List<NotificationDTO> notifications = notificationService.getUserNotifications(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            long count = notificationService.getUnreadCount(currentUser);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("unreadCount", 0));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            notificationService.markAsRead(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Marked as read", "success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            notificationService.markAllAsRead(currentUser);
            return ResponseEntity.ok(Map.of("message", "Marked all as read", "success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            notificationService.deleteNotification(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Notification deleted", "success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearAllNotifications(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            notificationService.clearAllNotifications(currentUser);
            return ResponseEntity.ok(Map.of("message", "All notifications cleared", "success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private User resolveUser(Authentication authentication, String authorizationHeader) {
        String email = null;
        if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            email = authentication.getName();
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            if (jwtService.isTokenValid(token)) {
                email = jwtService.extractEmail(token);
            }
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Authorization token is missing or invalid");
        }

        final String userEmail = email;
        return userRepository.findByEmailaddress(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }
}
