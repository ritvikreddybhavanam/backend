package com.example.backend.dto;

public class ImportRowDTO {
    private String title;
    private Integer year;
    private Double rating; // 0.5 to 5.0 scale (or 1-10 scale if IMDb)
    private String reviewText;
    private String reviewTitle;
    private String watchedDate; // YYYY-MM-DD
    private Boolean isRewatch;
    private Boolean hasSpoilers;
    private String tags;
    private String imdbId;
    private Long tmdbId;
    private String importType; // "diary", "rating", "watched", "watchlist", "review"

    public ImportRowDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getReviewTitle() { return reviewTitle; }
    public void setReviewTitle(String reviewTitle) { this.reviewTitle = reviewTitle; }

    public String getWatchedDate() { return watchedDate; }
    public void setWatchedDate(String watchedDate) { this.watchedDate = watchedDate; }

    public Boolean getIsRewatch() { return isRewatch; }
    public void setIsRewatch(Boolean isRewatch) { this.isRewatch = isRewatch; }

    public Boolean getHasSpoilers() { return hasSpoilers; }
    public void setHasSpoilers(Boolean hasSpoilers) { this.hasSpoilers = hasSpoilers; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
}
