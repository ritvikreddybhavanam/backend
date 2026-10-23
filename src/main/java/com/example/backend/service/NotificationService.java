package com.example.backend.service;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.Notification;
import com.example.backend.entity.User;
import com.example.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(User recipient, User actor, String type, String message, Long targetId, Long targetMovieId, String targetMovieTitle) {
        // Do not notify if user is acting on their own content
        if (recipient == null || actor == null || recipient.getId().equals(actor.getId())) {
            return;
        }

        Notification notification = new Notification(recipient, actor, type, message, targetId, targetMovieId, targetMovieTitle);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(User user) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        return list.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByRecipient(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void clearAllNotifications(User user) {
        notificationRepository.deleteByRecipient(user);
    }

    private NotificationDTO toDTO(Notification n) {
        User actor = n.getActor();
        String actorName = actor != null ? ((actor.getFirstname() != null ? actor.getFirstname() : "") + " " + (actor.getLastname() != null ? actor.getLastname() : "")).trim() : "Cinephile";
        String actorUsername = actor != null && actor.getUsername() != null ? actor.getUsername() : (actor != null ? "user" + actor.getId() : "");
        String actorAvatar = actor != null && actor.getAvatarUrl() != null ? actor.getAvatarUrl() : "";

        return new NotificationDTO(
                n.getId(),
                n.getRecipient().getId(),
                actor != null ? actor.getId() : null,
                actorUsername,
                !actorName.isBlank() ? actorName : "Cinephile",
                actorAvatar,
                n.getType(),
                n.getMessage(),
                n.getTargetId(),
                n.getTargetMovieId(),
                n.getTargetMovieTitle(),
                n.getIsRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
        );
    }
}
