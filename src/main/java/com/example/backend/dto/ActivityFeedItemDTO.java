package com.example.backend.dto;

import java.time.LocalDateTime;

public class ActivityFeedItemDTO {

    private String id;
    private String type; // "DIARY_LOG", "REVIEW", "LIST_CREATED", "LIKED_MOVIE"
    private LocalDateTime timestamp;

    // User details
    private Long userId;
    private String userName;
    private String userUsername;
    private String userAvatar;

    // Movie details (if applicable)
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private Integer movieYear;

    // Activity specific details
    private Double rating; // 0.5 to 5.0
    private String reviewText;
    private Boolean isRewatch;
    private String venueOrPlatform;
    private String notes;

    // List specific details (if type is "LIST_CREATED")
    private Long listId;
    private String listName;
    private String listDescription;
    private Integer listItemCount;

    public ActivityFeedItemDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        this.moviePoster = moviePoster;
    }

    public Integer getMovieYear() {
        return movieYear;
    }

    public void setMovieYear(Integer movieYear) {
        this.movieYear = movieYear;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Boolean getIsRewatch() {
        return isRewatch;
    }

    public void setIsRewatch(Boolean isRewatch) {
        this.isRewatch = isRewatch;
    }

    public String getVenueOrPlatform() {
        return venueOrPlatform;
    }

    public void setVenueOrPlatform(String venueOrPlatform) {
        this.venueOrPlatform = venueOrPlatform;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(String listDescription) {
        this.listDescription = listDescription;
    }

    public Integer getListItemCount() {
        return listItemCount;
    }

    public void setListItemCount(Integer listItemCount) {
        this.listItemCount = listItemCount;
    }
}
