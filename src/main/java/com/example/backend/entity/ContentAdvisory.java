package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_advisories")
public class ContentAdvisory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String category; // VIOLENCE_GORE, JUMP_SCARES, FLASHING_LIGHTS, ANIMAL_HARM, SEXUAL_CONTENT, SUBSTANCE_USE, PSYCHOLOGICAL_DISTRESS, CLAUSTROPHOBIA, OTHER

    @Column(nullable = false, length = 30)
    private String severity; // NONE, MILD, MODERATE, STRONG, EXTREME

    @Column(length = 255)
    private String details;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ContentAdvisory() {
    }

    public ContentAdvisory(Long movieId, User user, String category, String severity, String details) {
        this.movieId = movieId;
        this.user = user;
        this.category = category;
        this.severity = severity;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
