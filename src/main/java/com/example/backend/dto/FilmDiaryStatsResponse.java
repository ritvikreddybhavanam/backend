package com.example.backend.dto;

import java.util.List;
import java.util.Map;

public class FilmDiaryStatsResponse {

    private int year;
    private long totalFilmsLogged;
    private long totalRewatches;
    private long firstTimeWatches;
    private long totalMinutesWatched;
    private double totalHoursWatched;
    private double totalDaysWatched;
    private Double averageRating;
    private double communityAverageRating = 3.65;

    // Breakdown maps
    private Map<String, Long> ratingDistribution; // "0.5" -> count, "1.0" -> count, etc.
    private List<Map<String, Object>> topGenres;   // [{ "genre": "Sci-Fi", "count": 12, "percent": 24 }]
    private List<Map<String, Object>> topDirectors;// [{ "director": "Christopher Nolan", "count": 4 }]
    private List<Map<String, Object>> topVenues;   // [{ "venue": "Cinema", "count": 18, "percent": 36 }]
    private List<Map<String, Object>> monthlyActivity; // [{ "month": "Jan", "count": 6 }]
    private Map<String, Long> decadeDistribution; // "2020s" -> 18, "2010s" -> 12, "1990s" -> 8, etc.
    private Map<String, Long> dayOfWeekDistribution; // "Fri" -> 14, "Sat" -> 20, etc.
    private List<Map<String, Object>> topRatedFilms; // [{ "movieId": 123, "title": "Inception", "poster": "...", "rating": 5.0, "year": 2010 }]
    private Map<String, String> cinephilePersona; // { "title": "The Arthouse Connoisseur", "desc": "...", "badge": "Auteur" }

    // Enhanced Story Metrics
    private Map<String, Object> topMovie; // { "movieId": 123, "title": "Interstellar", "poster": "...", "rating": 5.0, "year": 2014, "tagline": "..." }
    private Map<String, Object> topActor; // { "name": "Prabhas", "count": 6, "avatar": "..." }
    private Map<String, Object> topDirector; // { "name": "Christopher Nolan", "count": 4 }
    private Map<String, Object> mostRewatchedMovie; // { "movieId": 456, "title": "RRR", "poster": "...", "count": 5 }
    private Map<String, Object> mostActiveMonth; // { "month": "December", "count": 18 }
    private String tasteSummary; // "International + Telugu + Sci-Fi"

    public FilmDiaryStatsResponse() {
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTotalFilmsLogged() {
        return totalFilmsLogged;
    }

    public void setTotalFilmsLogged(long totalFilmsLogged) {
        this.totalFilmsLogged = totalFilmsLogged;
    }

    public long getTotalRewatches() {
        return totalRewatches;
    }

    public void setTotalRewatches(long totalRewatches) {
        this.totalRewatches = totalRewatches;
    }

    public long getFirstTimeWatches() {
        return firstTimeWatches;
    }

    public void setFirstTimeWatches(long firstTimeWatches) {
        this.firstTimeWatches = firstTimeWatches;
    }

    public long getTotalMinutesWatched() {
        return totalMinutesWatched;
    }

    public void setTotalMinutesWatched(long totalMinutesWatched) {
        this.totalMinutesWatched = totalMinutesWatched;
    }

    public double getTotalHoursWatched() {
        return totalHoursWatched;
    }

    public void setTotalHoursWatched(double totalHoursWatched) {
        this.totalHoursWatched = totalHoursWatched;
    }

    public double getTotalDaysWatched() {
        return totalDaysWatched;
    }

    public void setTotalDaysWatched(double totalDaysWatched) {
        this.totalDaysWatched = totalDaysWatched;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public double getCommunityAverageRating() {
        return communityAverageRating;
    }

    public void setCommunityAverageRating(double communityAverageRating) {
        this.communityAverageRating = communityAverageRating;
    }

    public Map<String, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<String, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public List<Map<String, Object>> getTopGenres() {
        return topGenres;
    }

    public void setTopGenres(List<Map<String, Object>> topGenres) {
        this.topGenres = topGenres;
    }

    public List<Map<String, Object>> getTopDirectors() {
        return topDirectors;
    }

    public void setTopDirectors(List<Map<String, Object>> topDirectors) {
        this.topDirectors = topDirectors;
    }

    public List<Map<String, Object>> getTopVenues() {
        return topVenues;
    }

    public void setTopVenues(List<Map<String, Object>> topVenues) {
        this.topVenues = topVenues;
    }

    public List<Map<String, Object>> getMonthlyActivity() {
        return monthlyActivity;
    }

    public void setMonthlyActivity(List<Map<String, Object>> monthlyActivity) {
        this.monthlyActivity = monthlyActivity;
    }

    public Map<String, Long> getDecadeDistribution() {
        return decadeDistribution;
    }

    public void setDecadeDistribution(Map<String, Long> decadeDistribution) {
        this.decadeDistribution = decadeDistribution;
    }

    public Map<String, Long> getDayOfWeekDistribution() {
        return dayOfWeekDistribution;
    }

    public void setDayOfWeekDistribution(Map<String, Long> dayOfWeekDistribution) {
        this.dayOfWeekDistribution = dayOfWeekDistribution;
    }

    public List<Map<String, Object>> getTopRatedFilms() {
        return topRatedFilms;
    }

    public void setTopRatedFilms(List<Map<String, Object>> topRatedFilms) {
        this.topRatedFilms = topRatedFilms;
    }

    public Map<String, String> getCinephilePersona() {
        return cinephilePersona;
    }

    public void setCinephilePersona(Map<String, String> cinephilePersona) {
        this.cinephilePersona = cinephilePersona;
    }

    public Map<String, Object> getTopMovie() {
        return topMovie;
    }

    public void setTopMovie(Map<String, Object> topMovie) {
        this.topMovie = topMovie;
    }

    public Map<String, Object> getTopActor() {
        return topActor;
    }

    public void setTopActor(Map<String, Object> topActor) {
        this.topActor = topActor;
    }

    public Map<String, Object> getTopDirector() {
        return topDirector;
    }

    public void setTopDirector(Map<String, Object> topDirector) {
        this.topDirector = topDirector;
    }

    public Map<String, Object> getMostRewatchedMovie() {
        return mostRewatchedMovie;
    }

    public void setMostRewatchedMovie(Map<String, Object> mostRewatchedMovie) {
        this.mostRewatchedMovie = mostRewatchedMovie;
    }

    public Map<String, Object> getMostActiveMonth() {
        return mostActiveMonth;
    }

    public void setMostActiveMonth(Map<String, Object> mostActiveMonth) {
        this.mostActiveMonth = mostActiveMonth;
    }

    public String getTasteSummary() {
        return tasteSummary;
    }

    public void setTasteSummary(String tasteSummary) {
        this.tasteSummary = tasteSummary;
    }
}
