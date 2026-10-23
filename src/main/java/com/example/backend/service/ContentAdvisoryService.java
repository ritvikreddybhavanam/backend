package com.example.backend.service;

import com.example.backend.dto.ContentAdvisoryRequest;
import com.example.backend.dto.ContentGuideResponse;
import com.example.backend.entity.ContentAdvisory;
import com.example.backend.entity.User;
import com.example.backend.repository.ContentAdvisoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentAdvisoryService {

    private final ContentAdvisoryRepository advisoryRepository;

    public ContentAdvisoryService(ContentAdvisoryRepository advisoryRepository) {
        this.advisoryRepository = advisoryRepository;
    }

    @Transactional
    public ContentAdvisory submitAdvisory(ContentAdvisoryRequest request, User user) {
        if (request.getMovieId() == null) {
            throw new RuntimeException("Movie ID is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new RuntimeException("Category is required");
        }
        if (request.getSeverity() == null || request.getSeverity().isBlank()) {
            throw new RuntimeException("Severity is required");
        }

        String category = request.getCategory().trim().toUpperCase();
        String severity = request.getSeverity().trim().toUpperCase();
        String details = request.getDetails() != null ? request.getDetails().trim() : null;
        if (details != null && details.length() > 250) {
            details = details.substring(0, 250);
        }

        // Check if user already submitted for this movie + category; update if so
        Optional<ContentAdvisory> existing = advisoryRepository.findByMovieIdAndUserAndCategory(
                request.getMovieId(), user, category
        );

        ContentAdvisory advisory;
        if (existing.isPresent()) {
            advisory = existing.get();
            advisory.setSeverity(severity);
            advisory.setDetails(details);
        } else {
            advisory = new ContentAdvisory(request.getMovieId(), user, category, severity, details);
        }

        return advisoryRepository.save(advisory);
    }

    @Transactional(readOnly = true)
    public ContentGuideResponse getMovieContentGuide(Long movieId) {
        List<ContentAdvisory> list = advisoryRepository.findByMovieId(movieId);
        long totalReports = list.size();

        Map<String, List<ContentAdvisory>> byCategory = list.stream()
                .collect(Collectors.groupingBy(ContentAdvisory::getCategory));

        List<ContentGuideResponse.CategorySummary> summaries = new ArrayList<>();

        for (Map.Entry<String, List<ContentAdvisory>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<ContentAdvisory> reports = entry.getValue();

            Map<String, Long> severityCounts = reports.stream()
                    .collect(Collectors.groupingBy(ContentAdvisory::getSeverity, Collectors.counting()));

            // Determine dominant severity by highest vote
            String dominantSeverity = severityCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("NONE");

            List<String> details = reports.stream()
                    .map(ContentAdvisory::getDetails)
                    .filter(d -> d != null && !d.isBlank())
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());

            summaries.add(new ContentGuideResponse.CategorySummary(
                    category,
                    dominantSeverity,
                    reports.size(),
                    severityCounts,
                    details
            ));
        }

        // Sort summaries by report count descending
        summaries.sort((a, b) -> Long.compare(b.getReportCount(), a.getReportCount()));

        return new ContentGuideResponse(movieId, totalReports, summaries);
    }
}
