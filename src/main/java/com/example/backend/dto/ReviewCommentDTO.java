package com.example.backend.dto;

public class ReviewCommentDTO {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String username;
    private String userName;
    private String userAvatar;
    private String content;
    private Long parentId;
    private String createdAt;
    private java.util.List<ReviewCommentDTO> replies = new java.util.ArrayList<>();

    public ReviewCommentDTO() {
    }

    public ReviewCommentDTO(Long id, Long reviewId, Long userId, String username, String userName, String userAvatar, String content, Long parentId, String createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.username = username;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.replies = new java.util.ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public java.util.List<ReviewCommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(java.util.List<ReviewCommentDTO> replies) {
        this.replies = replies;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
