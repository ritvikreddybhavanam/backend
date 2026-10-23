package com.example.backend.dto;

import java.util.List;
import java.util.Map;

public class ContentGuideResponse {

    private Long movieId;
    private long totalReports;
    private List<CategorySummary> summaries;

    public ContentGuideResponse() {
    }

    public ContentGuideResponse(Long movieId, long totalReports, List<CategorySummary> summaries) {
        this.movieId = movieId;
        this.totalReports = totalReports;
        this.summaries = summaries;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public List<CategorySummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<CategorySummary> summaries) {
        this.summaries = summaries;
    }

    public static class CategorySummary {
        private String category;
        private String dominantSeverity;
        private long reportCount;
        private Map<String, Long> severityCounts;
        private List<String> details;

        public CategorySummary() {
        }

        public CategorySummary(String category, String dominantSeverity, long reportCount, Map<String, Long> severityCounts, List<String> details) {
            this.category = category;
            this.dominantSeverity = dominantSeverity;
            this.reportCount = reportCount;
            this.severityCounts = severityCounts;
            this.details = details;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDominantSeverity() {
            return dominantSeverity;
        }

        public void setDominantSeverity(String dominantSeverity) {
            this.dominantSeverity = dominantSeverity;
        }

        public long getReportCount() {
            return reportCount;
        }

        public void setReportCount(long reportCount) {
            this.reportCount = reportCount;
        }

        public Map<String, Long> getSeverityCounts() {
            return severityCounts;
        }

        public void setSeverityCounts(Map<String, Long> severityCounts) {
            this.severityCounts = severityCounts;
        }

        public List<String> getDetails() {
            return details;
        }

        public void setDetails(List<String> details) {
            this.details = details;
        }
    }
}
