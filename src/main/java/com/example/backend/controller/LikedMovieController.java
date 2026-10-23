package com.example.backend.controller;

import com.example.backend.entity.LikedMovie;
import com.example.backend.security.JwtService;
import com.example.backend.service.LikedMovieService;
import com.example.backend.service.TmdbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/liked-movies")
public class LikedMovieController {

    private final LikedMovieService likedMovieService;
    private final JwtService jwtService;
    private final TmdbService tmdbService;

    public LikedMovieController(
            LikedMovieService likedMovieService,
            JwtService jwtService,
            TmdbService tmdbService
    ) {
        this.likedMovieService = likedMovieService;
        this.jwtService = jwtService;
        this.tmdbService = tmdbService;
    }

    @PostMapping("/{tmdbMovieId}")
    public ResponseEntity<?> likeMovie(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long tmdbMovieId
    ) {

        String emailaddress = getEmailFromToken(authorization);

        if (emailaddress == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        likedMovieService.likeMovie(
                emailaddress,
                tmdbMovieId
        );

        return ResponseEntity.ok(
                "Movie liked successfully"
        );
    }

    @DeleteMapping("/{tmdbMovieId}")
    public ResponseEntity<?> unlikeMovie(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long tmdbMovieId
    ) {

        String emailaddress = getEmailFromToken(authorization);

        if (emailaddress == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        likedMovieService.unlikeMovie(
                emailaddress,
                tmdbMovieId
        );

        return ResponseEntity.ok(
                "Movie unliked successfully"
        );
    }

    @GetMapping
    public ResponseEntity<?> getLikedMovies(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {

        String emailaddress = getEmailFromToken(authorization);

        if (emailaddress == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        List<LikedMovie> likedMovies =
                likedMovieService.getLikedMovies(emailaddress);

        List<String> movies = new ArrayList<>();

        ObjectMapper objectMapper =
                new ObjectMapper();

        for (LikedMovie likedMovie : likedMovies) {

            try {
                String movieJson =
                        tmdbService.getMovieDetails(
                                likedMovie.getTmdbMovieId()
                        );

                String creditsJson =
                        tmdbService.getMovieCredits(
                                likedMovie.getTmdbMovieId()
                        );

                ObjectNode movieNode =
                        (ObjectNode)
                                objectMapper.readTree(movieJson);

                JsonNode creditsNode =
                        objectMapper.readTree(creditsJson);

                String director = "Unknown";

                JsonNode crew =
                        creditsNode.get("crew");

                if (crew != null && crew.isArray()) {

                    for (JsonNode person : crew) {

                        String job =
                                person.path("job")
                                        .asString();

                        if ("Director".equalsIgnoreCase(job)) {

                            director =
                                    person.path("name")
                                            .asString("Unknown");

                            break;
                        }
                    }
                }

                movieNode.put(
                        "director",
                        director
                );

                movies.add(
                        movieNode.toString()
                );

            } catch (Exception e) {

                System.out.println(
                        "Failed to load movie ID: "
                                + likedMovie.getTmdbMovieId()
                );

                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{tmdbMovieId}/status")
    public ResponseEntity<?> getLikeStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long tmdbMovieId
    ) {

        String emailaddress =
                getEmailFromToken(authorization);

        if (emailaddress == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }

        boolean liked =
                likedMovieService.isMovieLiked(
                        emailaddress,
                        tmdbMovieId
                );

        return ResponseEntity.ok(
                new LikeStatusResponse(liked)
        );
    }

    private String getEmailFromToken(
            String authorization
    ) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            return null;
        }

        String token =
                authorization.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return null;
        }

        return jwtService.extractEmail(token);
    }

    public static class LikeStatusResponse {

        private boolean liked;

        public LikeStatusResponse(boolean liked) {
            this.liked = liked;
        }

        public boolean isLiked() {
            return liked;
        }
    }
}