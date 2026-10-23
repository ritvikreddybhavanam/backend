package com.example.backend.dto;

import java.util.List;
import java.util.Map;

public class CineMatchRoomResponse {

    private String roomCode;
    private Long hostId;
    private String hostName;
    private String status; // "WAITING", "SWIPING", "MATCHED", "EXPIRED"
    private String genreFilter;
    private String providerFilter; // e.g., "Netflix,Prime,Disney"
    private List<Map<String, Object>> participants; // [{ userId, name, avatar, ready, swipeCount }]
    private List<Map<String, Object>> movies; // Deck of films to swipe on
    private Map<String, Object> matchedMovie; // Populated when a match is found
    private List<Map<String, Object>> allMatches; // All films that all users agreed on
    private int totalParticipants;

    public CineMatchRoomResponse() {
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGenreFilter() {
        return genreFilter;
    }

    public void setGenreFilter(String genreFilter) {
        this.genreFilter = genreFilter;
    }

    public String getProviderFilter() {
        return providerFilter;
    }

    public void setProviderFilter(String providerFilter) {
        this.providerFilter = providerFilter;
    }

    public List<Map<String, Object>> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Map<String, Object>> participants) {
        this.participants = participants;
    }

    public List<Map<String, Object>> getMovies() {
        return movies;
    }

    public void setMovies(List<Map<String, Object>> movies) {
        this.movies = movies;
    }

    public Map<String, Object> getMatchedMovie() {
        return matchedMovie;
    }

    public void setMatchedMovie(Map<String, Object> matchedMovie) {
        this.matchedMovie = matchedMovie;
    }

    public List<Map<String, Object>> getAllMatches() {
        return allMatches;
    }

    public void setAllMatches(List<Map<String, Object>> allMatches) {
        this.allMatches = allMatches;
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }
}
