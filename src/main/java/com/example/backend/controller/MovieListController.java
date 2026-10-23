package com.example.backend.controller;

import com.example.backend.dto.MovieListItemRequest;
import com.example.backend.dto.MovieListRequest;
import com.example.backend.entity.MovieList;
import com.example.backend.entity.MovieListItem;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.MovieListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class MovieListController {

    private final MovieListService movieListService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public MovieListController(
            MovieListService movieListService,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.movieListService = movieListService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLists(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            List<MovieList> lists = movieListService.getMyLists(user);
            return ResponseEntity.ok(lists.stream().map(this::toResponse).toList());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicLists() {
        try {
            List<MovieList> lists = movieListService.getPublicLists();
            return ResponseEntity.ok(lists.stream().map(this::toResponse).toList());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getListById(@PathVariable Long id) {
        try {
            MovieList list = movieListService.getListById(id);
            return ResponseEntity.ok(toResponse(list));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createList(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MovieListRequest request
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            MovieList created = movieListService.createList(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateList(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MovieListRequest request
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            MovieList updated = movieListService.updateList(id, request, user);
            return ResponseEntity.ok(toResponse(updated));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteList(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            movieListService.deleteList(id, user);
            return ResponseEntity.ok("List deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addMovieToList(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MovieListItemRequest request
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            MovieListItem item = movieListService.addMovieToList(id, request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(toItemResponse(item));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/items/{movieId}")
    public ResponseEntity<?> removeMovieFromList(
            @PathVariable Long id,
            @PathVariable Long movieId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            movieListService.removeMovieFromList(id, movieId, user);
            return ResponseEntity.ok("Movie removed from list");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private User resolveUser(Authentication authentication, String authorizationHeader) {
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
            throw new RuntimeException("Authorization token is missing or invalid");
        }

        final String userEmail = email;
        return userRepository.findByEmailaddress(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    private MovieListResponse toResponse(MovieList list) {
        List<MovieListItemResponse> itemResponses = list.getItems() != null
                ? list.getItems().stream().map(this::toItemResponse).toList()
                : List.of();

        String firstName = list.getUser() != null && list.getUser().getFirstname() != null ? list.getUser().getFirstname() : "";
        String lastName = list.getUser() != null && list.getUser().getLastname() != null ? list.getUser().getLastname() : "";
        String creatorName = (firstName + " " + lastName).trim();
        if (creatorName.isEmpty()) {
            creatorName = list.getUser() != null && list.getUser().getUsername() != null ? list.getUser().getUsername() : "Cinephile User";
        }
        String creatorUsername = list.getUser() != null ? list.getUser().getUsername() : null;
        Long userId = list.getUser() != null ? list.getUser().getId() : null;

        return new MovieListResponse(
                list.getId(),
                list.getName(),
                list.getDescription(),
                list.getGenre(),
                list.getIsPublic(),
                userId,
                creatorName,
                creatorUsername,
                itemResponses,
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }

    private MovieListItemResponse toItemResponse(MovieListItem item) {
        return new MovieListItemResponse(
                item.getId(),
                item.getMovieId(),
                item.getTitle(),
                item.getPoster(),
                item.getYear(),
                item.getGenre(),
                item.getRating(),
                item.getNote(),
                item.getPosition(),
                item.getAddedAt()
        );
    }

    public record MovieListResponse(
            Long id,
            String name,
            String description,
            String genre,
            Boolean isPublic,
            Long userId,
            String creatorName,
            String creatorUsername,
            List<MovieListItemResponse> items,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}

    public record MovieListItemResponse(
            Long id,
            Long movieId,
            String title,
            String poster,
            Integer year,
            String genre,
            Double rating,
            String note,
            Integer position,
            java.time.LocalDateTime addedAt
    ) {}
}
