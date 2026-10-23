package com.example.backend.controller;

import com.example.backend.entity.Watchlist;
import com.example.backend.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(
            WatchlistService watchlistService) {

        this.watchlistService = watchlistService;
    }

    @GetMapping
    public ResponseEntity<List<Watchlist>> getWatchlist(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                watchlistService.getWatchlist(email)
        );
    }

    @PostMapping
    public ResponseEntity<Watchlist> addToWatchlist(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        String email = authentication.getName();

        Long movieId = Long.valueOf(
                request.get("movieId").toString()
        );

        String title = request.get("title").toString();

        String poster = request.get("poster") == null
                ? null
                : request.get("poster").toString();

        return ResponseEntity.ok(
                watchlistService.addToWatchlist(
                        email,
                        movieId,
                        title,
                        poster
                )
        );
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> removeFromWatchlist(
            Authentication authentication,
            @PathVariable Long movieId) {

        String email = authentication.getName();

        watchlistService.removeFromWatchlist(
                email,
                movieId
        );

        return ResponseEntity.ok(
                "Movie removed from watchlist"
        );
    }

    @GetMapping("/check/{movieId}")
    public ResponseEntity<Boolean> checkWatchlist(
            Authentication authentication,
            @PathVariable Long movieId) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                watchlistService.isInWatchlist(
                        email,
                        movieId
                )
        );
    }
}