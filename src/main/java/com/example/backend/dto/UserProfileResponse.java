package com.example.backend.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long id;
    private String firstname;
    private String lastname;
    private String emailaddress;
    private String username;
    private String bio;
    private String pronouns;
    private String location;
    private String website;
    private String avatarUrl;
    private String bannerUrl;
    private String tagline;
    private String letterboxd;
    private String imdb;
    private String tmdb;
    private String twitter;
    private String instagram;
    private String favoriteMovieIds;
    private String favoriteGenres;
    private String favoriteDirectors;
    private String favoriteActors;
    private LocalDateTime createdAt;

    // Computed stats
    private long totalFilmsWatched;
    private long totalFilmsLiked;
    private long totalWatchlist;
    private long totalReviews;
    private long totalDiaryLogs;

    public UserProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getLetterboxd() {
        return letterboxd;
    }

    public void setLetterboxd(String letterboxd) {
        this.letterboxd = letterboxd;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getTmdb() {
        return tmdb;
    }

    public void setTmdb(String tmdb) {
        this.tmdb = tmdb;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFavoriteMovieIds() {
        return favoriteMovieIds;
    }

    public void setFavoriteMovieIds(String favoriteMovieIds) {
        this.favoriteMovieIds = favoriteMovieIds;
    }

    public String getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(String favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }

    public String getFavoriteDirectors() {
        return favoriteDirectors;
    }

    public void setFavoriteDirectors(String favoriteDirectors) {
        this.favoriteDirectors = favoriteDirectors;
    }

    public String getFavoriteActors() {
        return favoriteActors;
    }

    public void setFavoriteActors(String favoriteActors) {
        this.favoriteActors = favoriteActors;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalFilmsWatched() {
        return totalFilmsWatched;
    }

    public void setTotalFilmsWatched(long totalFilmsWatched) {
        this.totalFilmsWatched = totalFilmsWatched;
    }

    public long getTotalFilmsLiked() {
        return totalFilmsLiked;
    }

    public void setTotalFilmsLiked(long totalFilmsLiked) {
        this.totalFilmsLiked = totalFilmsLiked;
    }

    public long getTotalWatchlist() {
        return totalWatchlist;
    }

    public void setTotalWatchlist(long totalWatchlist) {
        this.totalWatchlist = totalWatchlist;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getTotalDiaryLogs() {
        return totalDiaryLogs;
    }

    public void setTotalDiaryLogs(long totalDiaryLogs) {
        this.totalDiaryLogs = totalDiaryLogs;
    }
}
