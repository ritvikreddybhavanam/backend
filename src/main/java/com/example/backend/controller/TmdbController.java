package com.example.backend.controller;

import com.example.backend.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbController {

    private final TmdbService tmdbService;

    public TmdbController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/trending")
    public ResponseEntity<String> getTrendingMovies(
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(
                tmdbService.getTrendingMovies(page)
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<String> getPopularMovies() {
        return ResponseEntity.ok(
                tmdbService.getPopularMovies()
        );
    }

    @GetMapping("/movies")
    public ResponseEntity<String> getMovies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "popularity.desc") String sortBy,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String withWatchProviders,
            @RequestParam(required = false) String watchRegion,
            @RequestParam(required = false) Integer runtimeLte,
            @RequestParam(required = false) Integer runtimeGte
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovies(
                        page,
                        sortBy,
                        year,
                        genre,
                        rating,
                        language,
                        fromDate,
                        toDate,
                        withWatchProviders,
                        watchRegion,
                        runtimeLte,
                        runtimeGte
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(
                tmdbService.searchMovies(query, page)
        );
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<String> getMovieDetails(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieDetails(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/videos")
    public ResponseEntity<String> getMovieVideos(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieVideos(movieId)
        );
    }

    @GetMapping("/genres")
    public ResponseEntity<String> getGenres() {
        return ResponseEntity.ok(
                tmdbService.getGenres()
        );
    }

    @GetMapping("/movies/{movieId}/credits")
    public ResponseEntity<String> getMovieCredits(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieCredits(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<String> getMovieReviews(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieReviews(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/recommendations")
    public ResponseEntity<String> getMovieRecommendations(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieRecommendations(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/similar")
    public ResponseEntity<String> getSimilarMovies(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getSimilarMovies(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/watch/providers")
    public ResponseEntity<String> getMovieWatchProviders(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieWatchProviders(movieId)
        );
    }

    @GetMapping("/watch/providers")
    public ResponseEntity<String> getWatchProviders(
            @RequestParam(required = false, defaultValue = "IN") String watchRegion
    ) {
        return ResponseEntity.ok(
                tmdbService.getWatchProviders(watchRegion)
        );
    }

    @GetMapping("/movies/{movieId}/images")
    public ResponseEntity<String> getMovieImages(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieImages(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/keywords")
    public ResponseEntity<String> getMovieKeywords(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieKeywords(movieId)
        );
    }

    @GetMapping("/movies/{movieId}/release_dates")
    public ResponseEntity<String> getMovieReleaseDates(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(
                tmdbService.getMovieReleaseDates(movieId)
        );
    }

    /*
     * =========================
     * PEOPLE
     * =========================
     */

    @GetMapping("/people/popular")
    public ResponseEntity<String> getPopularPeople(
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(
                tmdbService.getPopularPeople(page)
        );
    }

    @GetMapping("/people/search")
    public ResponseEntity<String> searchPeople(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(
                tmdbService.searchPeople(query, page)
        );
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<String> getPersonDetails(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                tmdbService.getPersonDetails(personId)
        );
    }

    @GetMapping("/person/{personId}/credits")
    public ResponseEntity<String> getPersonCredits(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                tmdbService.getPersonCredits(personId)
        );
    }

    @GetMapping("/person/{personId}/images")
    public ResponseEntity<String> getPersonImages(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                tmdbService.getPersonImages(personId)
        );
    }

    @GetMapping("/person/{personId}/external-ids")
    public ResponseEntity<String> getPersonExternalIds(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                tmdbService.getPersonExternalIds(personId)
        );
    }

    @GetMapping("/languages")
    public List<Map<String, Object>> getLanguages() {
        return tmdbService.getLanguages();
    }

    @GetMapping("/movies/top")
    public ResponseEntity<String> getTopMoviesByLanguage(
            @RequestParam String language
    ) {
        return ResponseEntity.ok(
                tmdbService.getTopMoviesByLanguage(language)
        );
    }

    /*
     * =========================
     * TEST
     * =========================
     */

    @GetMapping({"/test", "/health", "/status"})
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Cinephile Backend",
                "timestamp", System.currentTimeMillis()
        ));
    }

}