package com.example.backend.service;

import com.example.backend.dto.ImportResultDTO;
import com.example.backend.dto.ImportRowDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImportExportService {

    private final UserRepository userRepository;
    private final FilmLogRepository filmLogRepository;
    private final WatchedMovieRepository watchedMovieRepository;
    private final WatchlistRepository watchlistRepository;
    private final ReviewRepository reviewRepository;
    private final TmdbService tmdbService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImportExportService(
            UserRepository userRepository,
            FilmLogRepository filmLogRepository,
            WatchedMovieRepository watchedMovieRepository,
            WatchlistRepository watchlistRepository,
            ReviewRepository reviewRepository,
            TmdbService tmdbService
    ) {
        this.userRepository = userRepository;
        this.filmLogRepository = filmLogRepository;
        this.watchedMovieRepository = watchedMovieRepository;
        this.watchlistRepository = watchlistRepository;
        this.reviewRepository = reviewRepository;
        this.tmdbService = tmdbService;
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmailaddress(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Matches title + year to TMDB movie data
     */
    public Map<String, Object> resolveMovieMetadata(String title, Integer year, Long providedTmdbId) {
        Map<String, Object> result = new HashMap<>();
        if (providedTmdbId != null && providedTmdbId > 0) {
            try {
                String detailsJson = tmdbService.getMovieDetails(providedTmdbId);
                JsonNode root = objectMapper.readTree(detailsJson);
                if (root.has("id") && !root.get("id").isNull()) {
                    result.put("id", root.get("id").asLong());
                    result.put("title", root.has("title") ? root.get("title").asText() : title);
                    result.put("poster", root.has("poster_path") && !root.get("poster_path").isNull() ? "https://image.tmdb.org/t/p/w500" + root.get("poster_path").asText() : null);
                    result.put("year", extractYear(root.has("release_date") ? root.get("release_date").asText() : null, year));
                    result.put("runtime", root.has("runtime") && !root.get("runtime").isNull() ? root.get("runtime").asInt() : null);
                    if (root.has("genres") && root.get("genres").isArray() && root.get("genres").size() > 0) {
                        result.put("genre", root.get("genres").get(0).get("name").asText());
                    }
                    return result;
                }
            } catch (Exception ignored) {}
        }

        if (title == null || title.isBlank()) {
            return result;
        }

        try {
            String searchJson = tmdbService.searchMovies(title.trim(), 1);
            JsonNode root = objectMapper.readTree(searchJson);
            if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                JsonNode bestMatch = null;
                for (JsonNode candidate : root.get("results")) {
                    String releaseDate = candidate.has("release_date") ? candidate.get("release_date").asText() : "";
                    Integer candYear = extractYear(releaseDate, null);
                    if (year != null && candYear != null && Math.abs(candYear - year) <= 1) {
                        bestMatch = candidate;
                        break;
                    }
                }
                if (bestMatch == null) {
                    bestMatch = root.get("results").get(0);
                }

                result.put("id", bestMatch.get("id").asLong());
                result.put("title", bestMatch.has("title") ? bestMatch.get("title").asText() : title);
                result.put("poster", bestMatch.has("poster_path") && !bestMatch.get("poster_path").isNull() ? "https://image.tmdb.org/t/p/w500" + bestMatch.get("poster_path").asText() : null);
                result.put("year", extractYear(bestMatch.has("release_date") ? bestMatch.get("release_date").asText() : null, year));
                return result;
            }
        } catch (Exception e) {
            System.err.println("TMDB resolution error for [" + title + "]: " + e.getMessage());
        }

        // Fallback placeholder
        result.put("id", (long) Math.abs((title + (year != null ? year : "")).hashCode()));
        result.put("title", title);
        result.put("poster", null);
        result.put("year", year);
        return result;
    }

    private Integer extractYear(String releaseDate, Integer defaultYear) {
        if (releaseDate != null && releaseDate.length() >= 4) {
            try {
                return Integer.parseInt(releaseDate.substring(0, 4));
            } catch (Exception ignored) {}
        }
        return defaultYear;
    }

    /**
     * Batch imports an array of structured rows
     */
    @Transactional
    public ImportResultDTO processBatchImport(String userEmail, List<ImportRowDTO> rows) {
        User user = getAuthenticatedUser(userEmail);
        ImportResultDTO result = new ImportResultDTO();
        result.setTotalRows(rows.size());

        for (ImportRowDTO row : rows) {
            try {
                if (row.getTitle() == null || row.getTitle().trim().isEmpty()) {
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    continue;
                }

                Map<String, Object> meta = resolveMovieMetadata(row.getTitle(), row.getYear(), row.getTmdbId());
                Long movieId = ((Number) meta.get("id")).longValue();
                String resolvedTitle = (String) meta.getOrDefault("title", row.getTitle());
                String poster = (String) meta.get("poster");
                Integer year = (Integer) meta.getOrDefault("year", row.getYear());
                String genre = (String) meta.get("genre");
                Integer runtime = (Integer) meta.get("runtime");

                Double rating = row.getRating();
                if (rating != null && rating > 5.0) {
                    rating = Math.round((rating / 2.0) * 10.0) / 10.0; // convert 10-scale to 5-scale
                }

                String type = (row.getImportType() != null) ? row.getImportType().toLowerCase() : "watched";

                if ("watchlist".equals(type)) {
                    // Strictly add to Watchlist only
                    if (!watchlistRepository.existsByUserAndMovieId(user, movieId)) {
                        Watchlist wl = new Watchlist();
                        wl.setUser(user);
                        wl.setMovieId(movieId);
                        wl.setTitle(resolvedTitle);
                        wl.setPoster(poster);
                        watchlistRepository.save(wl);
                        result.setWatchlistAdded(result.getWatchlistAdded() + 1);
                    }
                } else if ("diary".equals(type)) {
                    // Create film diary log
                    FilmLog log = new FilmLog();
                    log.setUser(user);
                    log.setMovieId(movieId);
                    log.setTitle(resolvedTitle);
                    log.setPoster(poster);
                    log.setYear(year);
                    log.setGenre(genre);
                    log.setRuntime(runtime);
                    log.setRating(rating);
                    log.setRewatch(Boolean.TRUE.equals(row.getIsRewatch()));
                    log.setReviewNotes(row.getReviewText());

                    LocalDate wDate = parseDateSafe(row.getWatchedDate());
                    log.setWatchedDate(wDate != null ? wDate : LocalDate.now());
                    filmLogRepository.save(log);
                    result.setDiaryEntriesAdded(result.getDiaryEntriesAdded() + 1);

                    // Also ensure recorded in Watched Films
                    if (!watchedMovieRepository.existsByUserAndMovieId(user, movieId)) {
                        WatchedMovie wm = new WatchedMovie();
                        wm.setUser(user);
                        wm.setMovieId(movieId);
                        wm.setTitle(resolvedTitle);
                        wm.setPoster(poster);
                        wm.setYear(year);
                        wm.setGenre(genre);
                        wm.setRating(rating);
                        watchedMovieRepository.save(wm);
                        result.setWatchedAdded(result.getWatchedAdded() + 1);
                    }

                    // Create Review entity if long-form review exists and user doesn't already have one
                    if (row.getReviewText() != null && !row.getReviewText().isBlank()) {
                        if (reviewRepository.findByMovieIdAndUser(movieId, user).isEmpty()) {
                            Review rev = new Review();
                            rev.setUser(user);
                            rev.setMovieId(movieId);
                            rev.setMovieTitle(resolvedTitle);
                            rev.setPoster(poster);
                            rev.setYear(year);
                            rev.setGenre(genre);
                            rev.setRating(rating != null ? rating : 4.0);
                            rev.setTitle(row.getReviewTitle() != null && !row.getReviewTitle().isBlank() ? row.getReviewTitle() : (resolvedTitle + " Review"));
                            rev.setContent(row.getReviewText());
                            rev.setWatchedDate(wDate);
                            rev.setRewatch(Boolean.TRUE.equals(row.getIsRewatch()));
                            rev.setSpoilers(Boolean.TRUE.equals(row.getHasSpoilers()));
                            rev.setVisibility("public");
                            reviewRepository.save(rev);
                            result.setReviewsAdded(result.getReviewsAdded() + 1);
                        }
                    }
                } else {
                    // "watched" or "ratings" only -> Watched Films list
                    if (!watchedMovieRepository.existsByUserAndMovieId(user, movieId)) {
                        WatchedMovie wm = new WatchedMovie();
                        wm.setUser(user);
                        wm.setMovieId(movieId);
                        wm.setTitle(resolvedTitle);
                        wm.setPoster(poster);
                        wm.setYear(year);
                        wm.setGenre(genre);
                        wm.setRating(rating);
                        watchedMovieRepository.save(wm);
                        result.setWatchedAdded(result.getWatchedAdded() + 1);
                    }
                }

                result.setSuccessfulImports(result.getSuccessfulImports() + 1);
            } catch (Exception ex) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.addWarning("Row skipped: " + row.getTitle() + " - " + ex.getMessage());
            }
        }

        return result;
    }

    /**
     * Parses uploaded CSV multipart file or ZIP archive containing multiple CSVs
     */
    public ImportResultDTO processCsvFile(String userEmail, MultipartFile file, String forcedType) throws Exception {
        List<ImportRowDTO> rows = parseSingleFileOrZip(file, forcedType);
        return processBatchImport(userEmail, rows);
    }

    /**
     * Processes multiple files (combination of CSVs and ZIP archives)
     */
    public ImportResultDTO processMultipleFiles(String userEmail, List<MultipartFile> files) throws Exception {
        List<ImportRowDTO> allRows = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                allRows.addAll(parseSingleFileOrZip(file, null));
            }
        }
        return processBatchImport(userEmail, allRows);
    }

    private List<ImportRowDTO> parseSingleFileOrZip(MultipartFile file, String forcedType) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".zip")) {
            return parseZipFile(file);
        } else {
            return parseCsvStream(file.getInputStream(), forcedType, filename);
        }
    }

    private List<ImportRowDTO> parseZipFile(MultipartFile file) throws Exception {
        List<ImportRowDTO> rows = new ArrayList<>();
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(file.getInputStream())) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".csv")) {
                    String entryName = entry.getName().toLowerCase();
                    byte[] buffer = zis.readAllBytes();
                    try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(buffer)) {
                        rows.addAll(parseCsvStream(bais, null, entryName));
                    }
                }
                zis.closeEntry();
            }
        }
        return rows;
    }

    private List<ImportRowDTO> parseCsvStream(java.io.InputStream inputStream, String forcedType, String fileName) throws Exception {
        List<ImportRowDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return list;

            // Strip UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String cleanH = headers.get(i).replace("\uFEFF", "").replace("\"", "").trim().toLowerCase();
                colIndex.put(cleanH, i);
            }

            String lowerFileName = fileName != null ? fileName.toLowerCase() : "";

            // Detect source format
            boolean isImdb = colIndex.containsKey("const") || colIndex.containsKey("your rating") || colIndex.containsKey("imdb rating");
            boolean isLetterboxdDiary = lowerFileName.contains("diary") || colIndex.containsKey("watched date") || (colIndex.containsKey("name") && colIndex.containsKey("rewatch"));
            boolean isLetterboxdRatings = lowerFileName.contains("ratings") || (colIndex.containsKey("letterboxd uri") && colIndex.containsKey("rating"));
            
            // Letterboxd watchlist.csv only has: [Date, Name, Year, Letterboxd URI] or [Name, Year, Letterboxd URI] or [Name, Letterboxd URI]
            // Letterboxd watched.csv has: [Date, Name, Year, Letterboxd URI] (Date is the date marked as watched)
            // When user uploads by filename:
            boolean isLetterboxdWatched = lowerFileName.contains("watched");
            boolean isWatchlist = "watchlist".equalsIgnoreCase(forcedType)
                    || lowerFileName.contains("watchlist")
                    || (!isImdb && !isLetterboxdDiary && !isLetterboxdRatings && !isLetterboxdWatched && (colIndex.containsKey("letterboxd uri") || colIndex.containsKey("name")) && !colIndex.containsKey("date") && !colIndex.containsKey("rating") && !colIndex.containsKey("watched date"));


            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> cols = parseCsvLine(line);
                if (cols.isEmpty()) continue;

                ImportRowDTO dto = new ImportRowDTO();

                if (isWatchlist) {
                    dto.setTitle(getCol(cols, colIndex, "name", "title", "film", "movie"));
                    dto.setYear(parseIntSafe(getCol(cols, colIndex, "year", "release year", "release_year")));
                    dto.setImportType("watchlist");
                } else if (isImdb) {
                    dto.setTitle(getCol(cols, colIndex, "title", "name", "original title"));
                    dto.setYear(parseIntSafe(getCol(cols, colIndex, "year", "release date")));
                    dto.setRating(parseDoubleSafe(getCol(cols, colIndex, "your rating", "rating", "user rating")));
                    dto.setWatchedDate(getCol(cols, colIndex, "date rated", "date", "created", "modified"));
                    dto.setImdbId(getCol(cols, colIndex, "const"));
                    dto.setImportType("diary");
                } else if (isLetterboxdDiary) {
                    dto.setTitle(getCol(cols, colIndex, "name", "title", "film"));
                    dto.setYear(parseIntSafe(getCol(cols, colIndex, "year", "release year")));
                    dto.setRating(parseDoubleSafe(getCol(cols, colIndex, "rating", "score")));
                    dto.setWatchedDate(getCol(cols, colIndex, "watched date", "date", "date rated"));
                    String rw = getCol(cols, colIndex, "rewatch");
                    dto.setIsRewatch("yes".equalsIgnoreCase(rw) || "true".equalsIgnoreCase(rw) || "1".equals(rw));
                    dto.setTags(getCol(cols, colIndex, "tags"));
                    dto.setReviewText(getCol(cols, colIndex, "review", "notes", "comment"));
                    dto.setHasSpoilers("true".equalsIgnoreCase(getCol(cols, colIndex, "spoilers")));
                    dto.setImportType("diary");
                } else if (isLetterboxdRatings) {
                    dto.setTitle(getCol(cols, colIndex, "name", "title", "film"));
                    dto.setYear(parseIntSafe(getCol(cols, colIndex, "year", "release year")));
                    dto.setRating(parseDoubleSafe(getCol(cols, colIndex, "rating", "score")));
                    dto.setWatchedDate(getCol(cols, colIndex, "date", "watched date"));
                    dto.setImportType("watched");
                } else {
                    // Generic fallback
                    dto.setTitle(getCol(cols, colIndex, "name", "title", "film", "movie", "movie title", "film title"));
                    dto.setYear(parseIntSafe(getCol(cols, colIndex, "year", "release year", "release_year", "date")));
                    dto.setRating(parseDoubleSafe(getCol(cols, colIndex, "rating", "score", "user rating", "your rating")));
                    String wDate = getCol(cols, colIndex, "watched date", "date", "date rated", "watched_date");
                    String rev = getCol(cols, colIndex, "review", "notes", "comment", "review notes");
                    dto.setWatchedDate(wDate);
                    dto.setReviewText(rev);
                    if (forcedType != null) {
                        dto.setImportType(forcedType);
                    } else if (rev != null || (wDate != null && dto.getRating() != null)) {
                        dto.setImportType("diary");
                    } else {
                        dto.setImportType("watched");
                    }
                }

                if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
                    list.add(dto);
                }
            }
        }
        return list;
    }

    private String getCol(List<String> cols, Map<String, Integer> colIndex, String... possibleNames) {
        for (String name : possibleNames) {
            String clean = name.toLowerCase().trim();
            Integer idx = colIndex.get(clean);
            if (idx != null && idx < cols.size()) {
                String val = cols.get(idx).trim();
                // strip enclosing quotes if present
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1).trim();
                }
                if (!val.isEmpty()) return val;
            }
        }
        return null;
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    private Integer parseIntSafe(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDateSafe(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return LocalDate.parse(val.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {}
        try {
            return LocalDate.parse(val.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception ignored) {}
        try {
            return LocalDate.parse(val.trim(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } catch (Exception ignored) {}
        return null;
    }

    /*
     * =========================
     * EXPORT METHODS
     * =========================
     */

    public String exportDiaryToCsv(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        List<FilmLog> logs = filmLogRepository.findByUserOrderByWatchedDateDescCreatedAtDesc(user);

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Name,Year,Letterboxd URI,Rating,Rewatch,Tags,Watched Date,Review\n");

        for (FilmLog log : logs) {
            sb.append(escapeCsv(log.getWatchedDate() != null ? log.getWatchedDate().toString() : ""))
                    .append(",")
                    .append(escapeCsv(log.getTitle()))
                    .append(",")
                    .append(log.getYear() != null ? log.getYear() : "")
                    .append(",")
                    .append(escapeCsv("https://cinephile.app/movies/" + log.getMovieId()))
                    .append(",")
                    .append(log.getRating() != null ? log.getRating() : "")
                    .append(",")
                    .append(log.isRewatch() ? "Yes" : "No")
                    .append(",")
                    .append(escapeCsv(log.getVenueOrPlatform() != null ? log.getVenueOrPlatform() : ""))
                    .append(",")
                    .append(escapeCsv(log.getWatchedDate() != null ? log.getWatchedDate().toString() : ""))
                    .append(",")
                    .append(escapeCsv(log.getReviewNotes() != null ? log.getReviewNotes() : ""))
                    .append("\n");
        }

        return sb.toString();
    }

    public String exportWatchlistToCsv(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        List<Watchlist> list = watchlistRepository.findByUser(user);

        StringBuilder sb = new StringBuilder();
        sb.append("Name,Letterboxd URI\n");

        for (Watchlist w : list) {
            sb.append(escapeCsv(w.getTitle()))
                    .append(",")
                    .append(escapeCsv("https://cinephile.app/movies/" + w.getMovieId()))
                    .append("\n");
        }

        return sb.toString();
    }

    public String exportWatchedToCsv(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        List<WatchedMovie> list = watchedMovieRepository.findByUserOrderByWatchedAtDesc(user);

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Name,Year,Letterboxd URI,Rating\n");

        for (WatchedMovie w : list) {
            sb.append(escapeCsv(w.getWatchedAt() != null ? w.getWatchedAt().toLocalDate().toString() : ""))
                    .append(",")
                    .append(escapeCsv(w.getTitle()))
                    .append(",")
                    .append(w.getYear() != null ? w.getYear() : "")
                    .append(",")
                    .append(escapeCsv("https://cinephile.app/movies/" + w.getMovieId()))
                    .append(",")
                    .append(w.getRating() != null ? w.getRating() : "")
                    .append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String field) {
        if (field == null) return "\"\"";
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
