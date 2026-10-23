package com.example.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {
    private int totalRows;
    private int successfulImports;
    private int diaryEntriesAdded;
    private int watchedAdded;
    private int watchlistAdded;
    private int reviewsAdded;
    private int skippedCount;
    private List<String> warnings = new ArrayList<>();

    public ImportResultDTO() {}

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessfulImports() { return successfulImports; }
    public void setSuccessfulImports(int successfulImports) { this.successfulImports = successfulImports; }

    public int getDiaryEntriesAdded() { return diaryEntriesAdded; }
    public void setDiaryEntriesAdded(int diaryEntriesAdded) { this.diaryEntriesAdded = diaryEntriesAdded; }

    public int getWatchedAdded() { return watchedAdded; }
    public void setWatchedAdded(int watchedAdded) { this.watchedAdded = watchedAdded; }

    public int getWatchlistAdded() { return watchlistAdded; }
    public void setWatchlistAdded(int watchlistAdded) { this.watchlistAdded = watchlistAdded; }

    public int getReviewsAdded() { return reviewsAdded; }
    public void setReviewsAdded(int reviewsAdded) { this.reviewsAdded = reviewsAdded; }

    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public void addWarning(String warning) {
        if (this.warnings.size() < 20) {
            this.warnings.add(warning);
        }
    }
}
