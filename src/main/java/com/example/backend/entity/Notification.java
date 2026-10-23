package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Recipient of the notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Actor who triggered the notification (e.g. who followed, liked, or commented)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    // Type: "FOLLOW", "REVIEW_LIKE", "REVIEW_COMMENT", "COMMENT_REPLY", "SYSTEM"
    @Column(nullable = false, length = 50)
    private String type;

    // Friendly display message
    @Column(nullable = false, length = 500)
    private String message;

    // Related target IDs
    private Long targetId;        // e.g. reviewId or followId
    private Long targetMovieId;   // e.g. movieId for quick poster/link
    private String targetMovieTitle;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(User recipient, User actor, String type, String message, Long targetId, Long targetMovieId, String targetMovieTitle) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.targetId = targetId;
        this.targetMovieId = targetMovieId;
        this.targetMovieTitle = targetMovieTitle;
        this.isRead = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getTargetMovieId() {
        return targetMovieId;
    }

    public void setTargetMovieId(Long targetMovieId) {
        this.targetMovieId = targetMovieId;
    }

    public String getTargetMovieTitle() {
        return targetMovieTitle;
    }

    public void setTargetMovieTitle(String targetMovieTitle) {
        this.targetMovieTitle = targetMovieTitle;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
