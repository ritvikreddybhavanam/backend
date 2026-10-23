package com.example.backend.controller;

import com.example.backend.entity.WatchedMovie;
import com.example.backend.service.WatchedMovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watched")
public class WatchedMovieController {

    private final WatchedMovieService watchedMovieService;

    public WatchedMovieController(
            WatchedMovieService watchedMovieService
    ) {
        this.watchedMovieService = watchedMovieService;
    }

    @GetMapping
    public ResponseEntity<List<WatchedMovie>> getWatchedMovies(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                watchedMovieService.getWatchedMovies(email)
        );
    }

    @PostMapping
    public ResponseEntity<WatchedMovie> addWatchedMovie(
            Authentication authentication,
            @RequestBody Map<String, Object> request
    ) {

        String email = authentication.getName();

        Long movieId = Long.valueOf(
                request.get("movieId").toString()
        );

        String title = request.get("title").toString();

        String poster =
                request.get("poster") == null
                        ? null
                        : request.get("poster").toString();

        Integer year =
                request.get("year") == null
                        ? null
                        : Integer.valueOf(
                        request.get("year").toString()
                );

        String genre =
                request.get("genre") == null
                        ? null
                        : request.get("genre").toString();

        String service =
                request.get("service") == null
                        ? null
                        : request.get("service").toString();

        Double rating =
                request.get("rating") == null
                        ? null
                        : Double.valueOf(
                        request.get("rating").toString()
                );

        String language =
                request.get("language") == null
                        ? null
                        : request.get("language").toString();

        return ResponseEntity.ok(
                watchedMovieService.addWatchedMovie(
                        email,
                        movieId,
                        title,
                        poster,
                        year,
                        genre,
                        service,
                        rating,
                        language
                )
        );
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> removeWatchedMovie(
            Authentication authentication,
            @PathVariable Long movieId
    ) {

        String email = authentication.getName();

        watchedMovieService.removeWatchedMovie(
                email,
                movieId
        );

        return ResponseEntity.ok(
                "Movie removed from watched films"
        );
    }

    @GetMapping("/check/{movieId}")
    public ResponseEntity<Boolean> checkWatched(
            Authentication authentication,
            @PathVariable Long movieId
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                watchedMovieService.isWatched(
                        email,
                        movieId
                )
        );
    }
}