package com.example.backend.dto;

public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private Long actorId;
    private String actorUsername;
    private String actorName;
    private String actorAvatar;
    private String type; // FOLLOW, REVIEW_LIKE, REVIEW_COMMENT, COMMENT_REPLY
    private String message;
    private Long targetId;
    private Long targetMovieId;
    private String targetMovieTitle;
    private Boolean isRead;
    private String createdAt;

    public NotificationDTO() {
    }

    public NotificationDTO(Long id, Long recipientId, Long actorId, String actorUsername, String actorName, String actorAvatar, String type, String message, Long targetId, Long targetMovieId, String targetMovieTitle, Boolean isRead, String createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.actorName = actorName;
        this.actorAvatar = actorAvatar;
        this.type = type;
        this.message = message;
        this.targetId = targetId;
        this.targetMovieId = targetMovieId;
        this.targetMovieTitle = targetMovieTitle;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorAvatar() {
        return actorAvatar;
    }

    public void setActorAvatar(String actorAvatar) {
        this.actorAvatar = actorAvatar;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
