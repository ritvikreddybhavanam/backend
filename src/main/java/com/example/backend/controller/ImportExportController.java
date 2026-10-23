package com.example.backend.controller;

import com.example.backend.dto.ImportResultDTO;
import com.example.backend.dto.ImportRowDTO;
import com.example.backend.security.JwtService;
import com.example.backend.service.ImportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class ImportExportController {

    private final ImportExportService importExportService;
    private final JwtService jwtService;

    public ImportExportController(ImportExportService importExportService, JwtService jwtService) {
        this.importExportService = importExportService;
        this.jwtService = jwtService;
    }

    private String resolveUsername(Authentication authentication, String authorizationHeader) {
        String email = null;
        if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            email = authentication.getName();
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            if (jwtService.isTokenValid(token)) {
                email = jwtService.extractEmail(token);
            }
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("User is not authenticated. Please log in first.");
        }
        return email;
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importCsv(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "type", required = false) String type
    ) {
        try {
            String username = resolveUsername(authentication, authHeader);
            ImportResultDTO result;
            if (files != null && !files.isEmpty()) {
                result = importExportService.processMultipleFiles(username, files);
            } else if (file != null && !file.isEmpty()) {
                result = importExportService.processCsvFile(username, file, type);
            } else {
                ImportResultDTO errorResult = new ImportResultDTO();
                errorResult.addWarning("No file uploaded.");
                return ResponseEntity.badRequest().body(errorResult);
            }

            if (result.getTotalRows() == 0) {
                result.addWarning("No movie records found. Please check that the CSV has a 'Name' or 'Title' column.");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("CSV import error: " + e.getMessage());
            e.printStackTrace();
            ImportResultDTO errorResult = new ImportResultDTO();
            errorResult.addWarning("Failed to process CSV file(s): " + (e.getMessage() != null ? e.getMessage() : "Invalid file format"));
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    @PostMapping("/import/batch")
    public ResponseEntity<ImportResultDTO> importBatch(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody List<ImportRowDTO> rows
    ) {
        String username = resolveUsername(authentication, authHeader);
        ImportResultDTO result = importExportService.processBatchImport(username, rows);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/export/diary", produces = "text/csv")
    public ResponseEntity<String> exportDiary(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String username = resolveUsername(authentication, authHeader);
        String csv = importExportService.exportDiaryToCsv(username);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cinephile_diary.csv\"")
                .body(csv);
    }

    @GetMapping(value = "/export/watchlist", produces = "text/csv")
    public ResponseEntity<String> exportWatchlist(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String username = resolveUsername(authentication, authHeader);
        String csv = importExportService.exportWatchlistToCsv(username);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cinephile_watchlist.csv\"")
                .body(csv);
    }

    @GetMapping(value = "/export/watched", produces = "text/csv")
    public ResponseEntity<String> exportWatched(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String username = resolveUsername(authentication, authHeader);
        String csv = importExportService.exportWatchedToCsv(username);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cinephile_watched.csv\"")
                .body(csv);
    }
}
