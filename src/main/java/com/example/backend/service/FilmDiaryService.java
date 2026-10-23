package com.example.backend.service;

import com.example.backend.dto.FilmDiaryStatsResponse;
import com.example.backend.dto.FilmLogRequest;
import com.example.backend.entity.FilmLog;
import com.example.backend.entity.User;
import com.example.backend.repository.FilmLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmDiaryService {

    private final FilmLogRepository filmLogRepository;

    public FilmDiaryService(FilmLogRepository filmLogRepository) {
        this.filmLogRepository = filmLogRepository;
    }

    @Transactional(readOnly = true)
    public List<FilmLog> getDiaryByUser(User user) {
        return filmLogRepository.findByUserOrderByWatchedDateDescCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<FilmLog> getDiaryByUserAndMonth(User user, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return filmLogRepository.findByUserAndWatchedDateBetweenOrderByWatchedDateDescCreatedAtDesc(user, start, end);
    }

    @Transactional
    public FilmLog logFilm(FilmLogRequest request, User user) {
        FilmLog log = new FilmLog();
        log.setUser(user);
        log.setMovieId(request.getMovieId());
        log.setTitle(request.getTitle());
        log.setPoster(request.getPoster());
        log.setYear(request.getYear());
        log.setGenre(request.getGenre());
        log.setRuntime(request.getRuntime() != null && request.getRuntime() > 0 ? request.getRuntime() : 110);
        log.setRating(request.getRating());
        log.setWatchedDate(request.getWatchedDate() != null ? request.getWatchedDate() : LocalDate.now());
        log.setRewatch(request.isRewatch());
        log.setVenueOrPlatform(request.getVenueOrPlatform() != null && !request.getVenueOrPlatform().isBlank()
                ? request.getVenueOrPlatform() : "Streaming");
        log.setReviewNotes(request.getReviewNotes());
        log.setDirector(request.getDirector());

        return filmLogRepository.save(log);
    }

    @Transactional
    public FilmLog updateLog(Long logId, FilmLogRequest request, User user) {
        FilmLog log = filmLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Diary entry not found"));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to edit this diary entry");
        }

        if (request.getTitle() != null) log.setTitle(request.getTitle());
        if (request.getPoster() != null) log.setPoster(request.getPoster());
        if (request.getYear() != null) log.setYear(request.getYear());
        if (request.getGenre() != null) log.setGenre(request.getGenre());
        if (request.getRuntime() != null) log.setRuntime(request.getRuntime());
        if (request.getRating() != null) log.setRating(request.getRating());
        if (request.getWatchedDate() != null) log.setWatchedDate(request.getWatchedDate());
        log.setRewatch(request.isRewatch());
        if (request.getVenueOrPlatform() != null) log.setVenueOrPlatform(request.getVenueOrPlatform());
        if (request.getReviewNotes() != null) log.setReviewNotes(request.getReviewNotes());
        if (request.getDirector() != null) log.setDirector(request.getDirector());

        return filmLogRepository.save(log);
    }

    @Transactional
    public void deleteLog(Long logId, User user) {
        FilmLog log = filmLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Diary entry not found"));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this diary entry");
        }

        filmLogRepository.delete(log);
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears(User user) {
        List<Integer> years = filmLogRepository.findDistinctYearsByUser(user);
        if (years.isEmpty()) {
            return List.of(LocalDate.now().getYear());
        }
        return years;
    }

    @Transactional(readOnly = true)
    public FilmDiaryStatsResponse getStatsForYear(User user, int year) {
        List<FilmLog> logs = year > 0
                ? filmLogRepository.findByUserAndYear(user, year)
                : filmLogRepository.findByUserOrderByWatchedDateDescCreatedAtDesc(user);

        FilmDiaryStatsResponse stats = new FilmDiaryStatsResponse();
        stats.setYear(year);
        stats.setTotalFilmsLogged(logs.size());

        long rewatches = logs.stream().filter(FilmLog::isRewatch).count();
        stats.setTotalRewatches(rewatches);
        stats.setFirstTimeWatches(Math.max(0, logs.size() - rewatches));

        long totalMinutes = logs.stream()
                .mapToLong(l -> l.getRuntime() != null && l.getRuntime() > 0 ? l.getRuntime() : 110L)
                .sum();
        stats.setTotalMinutesWatched(totalMinutes);
        double totalHours = Math.round((totalMinutes / 60.0) * 10.0) / 10.0;
        stats.setTotalHoursWatched(totalHours);
        stats.setTotalDaysWatched(Math.round((totalHours / 24.0) * 10.0) / 10.0);

        List<Double> validRatings = logs.stream()
                .map(FilmLog::getRating)
                .filter(Objects::nonNull)
                .toList();

        if (!validRatings.isEmpty()) {
            double avg = validRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            stats.setAverageRating(Math.round(avg * 10.0) / 10.0);
        } else {
            stats.setAverageRating(null);
        }

        // Rating distribution (0.5 to 5.0)
        Map<String, Long> ratingDist = new LinkedHashMap<>();
        for (double r = 0.5; r <= 5.0; r += 0.5) {
            String label = String.format(Locale.US, "%.1f", r);
            double target = r;
            long count = validRatings.stream()
                    .filter(val -> Math.abs(val - target) < 0.01)
                    .count();
            ratingDist.put(label, count);
        }
        stats.setRatingDistribution(ratingDist);

        // Top genres breakdown with percentages
        Map<String, Long> genreCounts = new HashMap<>();
        for (FilmLog l : logs) {
            if (l.getGenre() != null && !l.getGenre().isBlank()) {
                String[] parts = l.getGenre().split(",");
                for (String p : parts) {
                    String clean = p.trim();
                    if (!clean.isEmpty()) {
                        genreCounts.put(clean, genreCounts.getOrDefault(clean, 0L) + 1);
                    }
                }
            }
        }
        long totalGenreHits = genreCounts.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> topGenres = genreCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("genre", e.getKey());
                    m.put("count", e.getValue());
                    int pct = totalGenreHits > 0 ? (int) Math.round((e.getValue() * 100.0) / totalGenreHits) : 0;
                    m.put("percent", pct);
                    return m;
                })
                .collect(Collectors.toList());
        stats.setTopGenres(topGenres);

        // Top directors
        Map<String, Long> directorCounts = new HashMap<>();
        for (FilmLog l : logs) {
            if (l.getDirector() != null && !l.getDirector().isBlank()) {
                String dir = l.getDirector().trim();
                directorCounts.put(dir, directorCounts.getOrDefault(dir, 0L) + 1);
            }
        }
        List<Map<String, Object>> topDirectors = directorCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(6)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("director", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        stats.setTopDirectors(topDirectors);

        // Top viewing venues/platforms with percentages
        Map<String, Long> venueCounts = new HashMap<>();
        for (FilmLog l : logs) {
            if (l.getVenueOrPlatform() != null && !l.getVenueOrPlatform().isBlank()) {
                String ven = l.getVenueOrPlatform().trim();
                venueCounts.put(ven, venueCounts.getOrDefault(ven, 0L) + 1);
            }
        }
        long totalVenues = logs.size();
        List<Map<String, Object>> topVenues = venueCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(6)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("venue", e.getKey());
                    m.put("count", e.getValue());
                    int pct = totalVenues > 0 ? (int) Math.round((e.getValue() * 100.0) / totalVenues) : 0;
                    m.put("percent", pct);
                    return m;
                })
                .collect(Collectors.toList());
        stats.setTopVenues(topVenues);

        // Monthly activity distribution
        Map<Integer, Long> monthCounts = logs.stream()
                .filter(l -> l.getWatchedDate() != null)
                .collect(Collectors.groupingBy(l -> l.getWatchedDate().getMonthValue(), Collectors.counting()));

        List<Map<String, Object>> monthly = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> monthMap = new HashMap<>();
            String name = java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthMap.put("month", name);
            monthMap.put("count", monthCounts.getOrDefault(m, 0L));
            monthly.add(monthMap);
        }
        stats.setMonthlyActivity(monthly);

        // Decade Distribution Chart
        Map<String, Long> decadeDist = new LinkedHashMap<>();
        decadeDist.put("Pre-1970s", 0L);
        decadeDist.put("1970s", 0L);
        decadeDist.put("1980s", 0L);
        decadeDist.put("1990s", 0L);
        decadeDist.put("2000s", 0L);
        decadeDist.put("2010s", 0L);
        decadeDist.put("2020s", 0L);

        for (FilmLog l : logs) {
            if (l.getYear() != null && l.getYear() > 0) {
                int y = l.getYear();
                if (y < 1970) decadeDist.put("Pre-1970s", decadeDist.get("Pre-1970s") + 1);
                else if (y < 1980) decadeDist.put("1970s", decadeDist.get("1970s") + 1);
                else if (y < 1990) decadeDist.put("1980s", decadeDist.get("1980s") + 1);
                else if (y < 2000) decadeDist.put("1990s", decadeDist.get("1990s") + 1);
                else if (y < 2010) decadeDist.put("2000s", decadeDist.get("2000s") + 1);
                else if (y < 2020) decadeDist.put("2010s", decadeDist.get("2010s") + 1);
                else decadeDist.put("2020s", decadeDist.get("2020s") + 1);
            }
        }
        stats.setDecadeDistribution(decadeDist);

        // Day of Week Distribution
        Map<String, Long> dayDist = new LinkedHashMap<>();
        dayDist.put("Mon", 0L);
        dayDist.put("Tue", 0L);
        dayDist.put("Wed", 0L);
        dayDist.put("Thu", 0L);
        dayDist.put("Fri", 0L);
        dayDist.put("Sat", 0L);
        dayDist.put("Sun", 0L);

        for (FilmLog l : logs) {
            if (l.getWatchedDate() != null) {
                String dayKey = l.getWatchedDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                if (dayDist.containsKey(dayKey)) {
                    dayDist.put(dayKey, dayDist.get(dayKey) + 1);
                }
            }
        }
        stats.setDayOfWeekDistribution(dayDist);

        // Top rated films showcase (Top 5 / 6)
        List<Map<String, Object>> topFilms = logs.stream()
                .filter(l -> l.getRating() != null)
                .sorted((a, b) -> {
                    int c = Double.compare(b.getRating(), a.getRating());
                    if (c != 0) return c;
                    return b.getWatchedDate().compareTo(a.getWatchedDate());
                })
                .limit(6)
                .map(l -> {
                    Map<String, Object> fm = new HashMap<>();
                    fm.put("movieId", l.getMovieId());
                    fm.put("title", l.getTitle());
                    fm.put("poster", l.getPoster());
                    fm.put("rating", l.getRating());
                    fm.put("year", l.getYear());
                    fm.put("genre", l.getGenre());
                    return fm;
                })
                .collect(Collectors.toList());
        stats.setTopRatedFilms(topFilms);

        // Story 3: #1 Top Movie (Highest rated film)
        if (!topFilms.isEmpty()) {
            stats.setTopMovie(topFilms.get(0));
        } else if (!logs.isEmpty()) {
            FilmLog first = logs.get(0);
            Map<String, Object> fm = new HashMap<>();
            fm.put("movieId", first.getMovieId());
            fm.put("title", first.getTitle());
            fm.put("poster", first.getPoster());
            fm.put("rating", first.getRating() != null ? first.getRating() : 5.0);
            fm.put("year", first.getYear());
            stats.setTopMovie(fm);
        }

        // Story 6: #1 Top Actor / Filmmaker (fallback or extracted)
        // If topDirector exists, map topDirector
        if (!topDirectors.isEmpty()) {
            Map<String, Object> td = topDirectors.get(0);
            Map<String, Object> topDirMap = new HashMap<>();
            topDirMap.put("name", td.get("director") != null ? td.get("director") : "Unknown");
            topDirMap.put("count", td.get("count") != null ? td.get("count") : 0L);
            stats.setTopDirector(topDirMap);
        }

        // Story 10: Most Rewatched Movie
        Map<String, List<FilmLog>> movieLogs = logs.stream()
                .collect(Collectors.groupingBy(FilmLog::getTitle));
        
        Map<String, Object> mostRewatched = null;
        long maxWatches = 1;
        for (Map.Entry<String, List<FilmLog>> entry : movieLogs.entrySet()) {
            long count = entry.getValue().size();
            long rewatchMarks = entry.getValue().stream().filter(FilmLog::isRewatch).count();
            if (count > maxWatches || rewatchMarks > 0) {
                FilmLog sample = entry.getValue().get(0);
                long effectiveCount = Math.max(count, rewatchMarks + 1);
                if (mostRewatched == null || effectiveCount > maxWatches) {
                    maxWatches = effectiveCount;
                    mostRewatched = new HashMap<>();
                    mostRewatched.put("movieId", sample.getMovieId());
                    mostRewatched.put("title", sample.getTitle());
                    mostRewatched.put("poster", sample.getPoster());
                    mostRewatched.put("count", effectiveCount);
                    mostRewatched.put("year", sample.getYear());
                }
            }
        }
        if (mostRewatched == null && !logs.isEmpty()) {
            FilmLog sample = logs.get(0);
            mostRewatched = new HashMap<>();
            mostRewatched.put("movieId", sample.getMovieId());
            mostRewatched.put("title", sample.getTitle());
            mostRewatched.put("poster", sample.getPoster());
            mostRewatched.put("count", 1);
            mostRewatched.put("year", sample.getYear());
        }
        stats.setMostRewatchedMovie(mostRewatched);

        // Story 11: Most Active Movie Month
        Map<String, Object> peakMonth = monthly.stream()
                .max((a, b) -> Long.compare((Long) a.get("count"), (Long) b.get("count")))
                .orElse(null);
        if (peakMonth != null && (Long) peakMonth.get("count") > 0) {
            stats.setMostActiveMonth(peakMonth);
        } else {
            stats.setMostActiveMonth(Map.of("month", "Recent", "count", logs.size()));
        }

        // Story 8: Movie Taste Summary
        List<String> tasteParts = new ArrayList<>();
        if (!topGenres.isEmpty()) {
            tasteParts.add((String) topGenres.get(0).get("genre"));
        }
        if (topGenres.size() > 1) {
            tasteParts.add((String) topGenres.get(1).get("genre"));
        }
        long cinemaCount = venueCounts.getOrDefault("Cinema", 0L) + venueCounts.getOrDefault("Theaters", 0L) + venueCounts.getOrDefault("Theatre", 0L);
        if (cinemaCount > 0) {
            tasteParts.add("Big Screen / Cinema");
        } else if (!topVenues.isEmpty()) {
            tasteParts.add((String) topVenues.get(0).get("venue"));
        }
        stats.setTasteSummary(tasteParts.isEmpty() ? "Eclectic Cinema Lover" : String.join(" • ", tasteParts));

        // Cinephile Persona Archetype
        stats.setCinephilePersona(determinePersona(genreCounts, decadeDist, logs.size(), rewatches));

        return stats;
    }

    private Map<String, String> determinePersona(Map<String, Long> genreCounts, Map<String, Long> decadeDist, int totalLogs, long rewatches) {
        Map<String, String> persona = new HashMap<>();

        if (totalLogs == 0) {
            persona.put("title", "The Emerging Cineaste");
            persona.put("desc", "Your cinematic journey is just beginning. Start logging your watches to discover your cinema archetype!");
            persona.put("badge", "Newbie");
            persona.put("icon", "movie_filter");
            return persona;
        }

        String topGenre = genreCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        long sciFiCount = genreCounts.getOrDefault("Science Fiction", 0L) + genreCounts.getOrDefault("Sci-Fi", 0L);
        long dramaCount = genreCounts.getOrDefault("Drama", 0L);
        long horrorCount = genreCounts.getOrDefault("Horror", 0L) + genreCounts.getOrDefault("Thriller", 0L);
        long classicCount = decadeDist.getOrDefault("Pre-1970s", 0L) + decadeDist.getOrDefault("1970s", 0L);

        if (sciFiCount >= 3 || (totalLogs > 0 && (sciFiCount * 1.0 / totalLogs) > 0.35)) {
            persona.put("title", "The Sci-Fi Visionary");
            persona.put("desc", "You live on the outer rims of reality, fascinated by cyberpunk futures, spacetime paradoxes, and alien worlds.");
            persona.put("badge", "Futurist");
            persona.put("icon", "rocket_launch");
        } else if (dramaCount >= 4 || (totalLogs > 0 && (dramaCount * 1.0 / totalLogs) > 0.40)) {
            persona.put("title", "The Arthouse Auteur");
            persona.put("desc", "You crave rich character studies, poetic cinematography, and human truth that lingers long after the credits roll.");
            persona.put("badge", "Auteur");
            persona.put("icon", "palette");
        } else if (horrorCount >= 3 || (totalLogs > 0 && (horrorCount * 1.0 / totalLogs) > 0.35)) {
            persona.put("title", "The Midnight Fearless");
            persona.put("desc", "You thrive on atmospheric dread, psychological tension, and adrenaline-pumping spine chillers.");
            persona.put("badge", "Nocturnal");
            persona.put("icon", "dark_mode");
        } else if (classicCount >= 3 || (totalLogs > 0 && (classicCount * 1.0 / totalLogs) > 0.30)) {
            persona.put("title", "The Golden Age Historian");
            persona.put("desc", "You revere the masters of film history, timeless grain, and the foundational pillars of cinema.");
            persona.put("badge", "Historian");
            persona.put("icon", "history_edu");
        } else if (totalLogs >= 5 && (rewatches * 1.0 / totalLogs) > 0.35) {
            persona.put("title", "The Devoted Loyalist");
            persona.put("desc", "You find solace in familiar masterpieces, re-experiencing favorite worlds like reuniting with old friends.");
            persona.put("badge", "Loyalist");
            persona.put("icon", "repeat");
        } else {
            persona.put("title", "The Eclectic Connoisseur");
            persona.put("desc", "A versatile movie lover with broad horizons, exploring everything from global blockbusters to indie gems.");
            persona.put("badge", "Omnivore");
            persona.put("icon", "auto_awesome");
        }

        return persona;
    }
}
