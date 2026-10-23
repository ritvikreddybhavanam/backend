package com.example.backend.controller;

import com.example.backend.dto.ReviewCommentDTO;
import com.example.backend.dto.ReviewRequest;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewService reviewService,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.reviewService = reviewService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ReviewRequest request
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            Review review = reviewService.createReview(request, user);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(reviewResponse(review, user));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getMovieReviews(
            @PathVariable Long movieId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User optionalUser = resolveOptionalUser(authentication, authorizationHeader);
            List<Review> reviews = reviewService.getMovieReviews(movieId);
            List<ReviewResponse> responses = reviews.stream()
                    .map(r -> reviewResponse(r, optionalUser))
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReviews(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            List<Review> reviews = reviewService.getMyReviews(user);
            List<ReviewResponse> responses = reviews.stream()
                    .map(r -> reviewResponse(r, user))
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Unauthorized", "error", e.getMessage() != null ? e.getMessage() : "Unauthorized"));
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User optionalUser = resolveOptionalUser(authentication, authorizationHeader);
            Review review = reviewService.getReview(reviewId);
            return ResponseEntity.ok(reviewResponse(review, optionalUser));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Not found", "error", e.getMessage() != null ? e.getMessage() : "Not found"));
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ReviewRequest request
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            Review review = reviewService.updateReview(reviewId, request, user);
            return ResponseEntity.ok(reviewResponse(review, user));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            reviewService.deleteReview(reviewId, user);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully", "success", true));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    // ==========================================
    // INSTAGRAM-STYLE LIKES ENDPOINTS
    // ==========================================

    @PostMapping({"/{reviewId}/likes", "/{reviewId}/like"})
    public ResponseEntity<?> toggleLikeReview(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            Map<String, Object> result = reviewService.toggleLikeReview(reviewId, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{reviewId}/likes")
    public ResponseEntity<?> getReviewLikes(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User optionalUser = resolveOptionalUser(authentication, authorizationHeader);
            Map<String, Object> result = reviewService.getReviewLikes(reviewId, optionalUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // INSTAGRAM-STYLE COMMENTS ENDPOINTS
    // ==========================================

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<?> getReviewComments(@PathVariable Long reviewId) {
        try {
            List<ReviewCommentDTO> comments = reviewService.getReviewComments(reviewId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<?> addReviewComment(
            @PathVariable Long reviewId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> body
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            String content = (String) body.get("content");
            Long parentId = null;
            if (body.get("parentId") != null) {
                parentId = Long.valueOf(body.get("parentId").toString());
            }

            ReviewCommentDTO comment = reviewService.addReviewComment(reviewId, content, parentId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{reviewId}/comments/{commentId}")
    public ResponseEntity<?> deleteReviewComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            User user = resolveUser(authentication, authorizationHeader);
            reviewService.deleteReviewComment(reviewId, commentId, user);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully", "success", true));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
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

    private User resolveOptionalUser(Authentication authentication, String authorizationHeader) {
        try {
            return resolveUser(authentication, authorizationHeader);
        } catch (Exception e) {
            return null;
        }
    }

    private ReviewResponse reviewResponse(Review review, User optionalUser) {
        User user = review.getUser();
        long likesCount = reviewService.getLikesCount(review);
        boolean isLiked = optionalUser != null && reviewService.isLikedByUser(review, optionalUser);
        long commentsCount = reviewService.getCommentsCount(review);

        return new ReviewResponse(
                review.getId(),
                review.getMovieId(),
                review.getMovieTitle(),
                review.getPoster(),
                review.getYear(),
                user != null ? user.getId() : null,
                user != null ? user.getFirstname() : null,
                user != null ? user.getLastname() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getAvatarUrl() : null,
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getWatchedDate(),
                review.getViewingFormat(),
                review.getRewatch(),
                review.getSpoilers(),
                review.getSpoilerTypes(),
                review.getVisibility(),
                review.getReviewIntent() != null ? review.getReviewIntent() : "general",
                likesCount,
                isLiked,
                commentsCount,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public record ReviewResponse(
            Long id,
            Long movieId,
            String movieTitle,
            String poster,
            Integer year,
            Long userId,
            String firstname,
            String lastname,
            String username,
            String avatarUrl,
            Double rating,
            String title,
            String content,
            java.time.LocalDate watchedDate,
            String viewingFormat,
            Boolean rewatch,
            Boolean spoilers,
            String spoilerTypes,
            String visibility,
            String reviewIntent,
            long likesCount,
            boolean isLiked,
            long commentsCount,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}