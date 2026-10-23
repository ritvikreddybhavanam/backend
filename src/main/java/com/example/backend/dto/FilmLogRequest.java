package com.example.backend.dto;

import java.time.LocalDate;

public class FilmLogRequest {

    private Long movieId;
    private String title;
    private String poster;
    private Integer year;
    private String genre;
    private Integer runtime;
    private Double rating;
    private LocalDate watchedDate;
    private boolean isRewatch;
    private String venueOrPlatform;
    private String reviewNotes;
    private String director;

    public FilmLogRequest() {
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDate getWatchedDate() {
        return watchedDate;
    }

    public void setWatchedDate(LocalDate watchedDate) {
        this.watchedDate = watchedDate;
    }

    public boolean isRewatch() {
        return isRewatch;
    }

    public void setRewatch(boolean rewatch) {
        isRewatch = rewatch;
    }

    public String getVenueOrPlatform() {
        return venueOrPlatform;
    }

    public void setVenueOrPlatform(String venueOrPlatform) {
        this.venueOrPlatform = venueOrPlatform;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }
}
