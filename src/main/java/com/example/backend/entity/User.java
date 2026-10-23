package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String emailaddress;

    @Column(unique = true, length = 50)
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 30)
    private String pronouns;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String website;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 500)
    private String bannerUrl;

    @Column(length = 150)
    private String tagline;

    @Column(length = 100)
    private String letterboxd;

    @Column(length = 100)
    private String imdb;

    @Column(length = 100)
    private String tmdb;

    @Column(length = 100)
    private String twitter;

    @Column(length = 100)
    private String instagram;

    @Column(columnDefinition = "TEXT")
    private String favoriteMovieIds;

    @Column(columnDefinition = "TEXT")
    private String favoriteGenres;

    @Column(columnDefinition = "TEXT")
    private String favoriteDirectors;

    @Column(columnDefinition = "TEXT")
    private String favoriteActors;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (username == null || username.isBlank()) {
            if (emailaddress != null && emailaddress.contains("@")) {
                username = emailaddress.substring(0, emailaddress.indexOf("@"));
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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
        return username != null ? username : (emailaddress != null ? emailaddress.split("@")[0] : null);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() {
        return password;
    }

    @com.fasterxml.jackson.annotation.JsonProperty
    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}