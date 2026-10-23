package com.example.backend.service;

import com.example.backend.dto.ReviewCommentDTO;
import com.example.backend.dto.ReviewRequest;
import com.example.backend.entity.Review;
import com.example.backend.entity.ReviewComment;
import com.example.backend.entity.ReviewLike;
import com.example.backend.entity.User;
import com.example.backend.entity.WatchedMovie;
import com.example.backend.repository.ReviewCommentRepository;
import com.example.backend.repository.ReviewLikeRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.WatchedMovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final WatchedMovieRepository watchedMovieRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final NotificationService notificationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            WatchedMovieRepository watchedMovieRepository,
            ReviewLikeRepository reviewLikeRepository,
            ReviewCommentRepository reviewCommentRepository,
            NotificationService notificationService
    ) {
        this.reviewRepository = reviewRepository;
        this.watchedMovieRepository = watchedMovieRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Review createReview(
            ReviewRequest request,
            User user
    ) {
        if (request.getMovieId() == null) {
            throw new RuntimeException("Movie ID is required");
        }

        if (request.getRating() == null) {
            throw new RuntimeException("Rating is required");
        }

        if (request.getRating() < 0.5 || request.getRating() > 5.0) {
            throw new RuntimeException("Rating must be between 0.5 and 5.0");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Review title is required");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Review content is required");
        }

        String movieTitle = request.getMovieTitle();
        if (movieTitle == null || movieTitle.isBlank()) {
            movieTitle = (request.getTitle() != null && !request.getTitle().isBlank())
                    ? request.getTitle()
                    : "Movie #" + request.getMovieId();
        }

        if (reviewRepository.findByMovieIdAndUser(request.getMovieId(), user).isPresent()) {
            throw new RuntimeException("You have already reviewed this movie");
        }

        Review review = new Review();
        review.setMovieId(request.getMovieId());
        review.setMovieTitle(movieTitle);
        review.setPoster(request.getPoster());
        review.setYear(request.getYear());
        review.setGenre(request.getGenre());
        review.setUser(user);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setWatchedDate(request.getWatchedDate());
        review.setViewingFormat(request.getViewingFormat());
        review.setRewatch(request.getRewatch() != null ? request.getRewatch() : false);
        review.setSpoilers(request.getSpoilers() != null ? request.getSpoilers() : false);
        review.setSpoilerTypes(request.getSpoilerTypes());
        review.setVisibility(request.getVisibility() != null ? request.getVisibility() : "public");
        review.setReviewIntent(request.getReviewIntent() != null && !request.getReviewIntent().isBlank() ? request.getReviewIntent().trim().toLowerCase() : "general");

        Review savedReview = reviewRepository.save(review);

        if (!watchedMovieRepository.existsByUserAndMovieId(user, request.getMovieId())) {
            WatchedMovie watchedMovie = new WatchedMovie();
            watchedMovie.setUser(user);
            watchedMovie.setMovieId(request.getMovieId());
            watchedMovie.setTitle(request.getMovieTitle());
            watchedMovie.setPoster(request.getPoster());
            watchedMovie.setYear(request.getYear());
            watchedMovie.setGenre(request.getGenre());
            watchedMovie.setService(request.getService());
            watchedMovie.setRating(request.getRating());

            watchedMovieRepository.save(watchedMovie);
        }

        return savedReview;
    }

    @Transactional(readOnly = true)
    public List<Review> getMovieReviews(Long movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
    }

    @Transactional(readOnly = true)
    public List<Review> getMyReviews(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    }

    @Transactional
    public Review updateReview(
            Long reviewId,
            ReviewRequest request,
            User user
    ) {
        Review review = getReview(reviewId);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to update this review");
        }

        if (request.getRating() == null || request.getRating() < 0.5 || request.getRating() > 5.0) {
            throw new RuntimeException("Rating must be between 0.5 and 5.0");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setWatchedDate(request.getWatchedDate());
        review.setViewingFormat(request.getViewingFormat());

        if (request.getRewatch() != null) {
            review.setRewatch(request.getRewatch());
        }

        if (request.getSpoilers() != null) {
            review.setSpoilers(request.getSpoilers());
        }

        if (request.getSpoilerTypes() != null) {
            review.setSpoilerTypes(request.getSpoilerTypes());
        }

        if (request.getVisibility() != null) {
            review.setVisibility(request.getVisibility());
        }

        if (request.getReviewIntent() != null && !request.getReviewIntent().isBlank()) {
            review.setReviewIntent(request.getReviewIntent().trim().toLowerCase());
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = getReview(reviewId);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to delete this review");
        }

        reviewCommentRepository.deleteByReview(review);
        reviewLikeRepository.deleteByReview(review);
        reviewRepository.delete(review);
    }

    // ==========================================
    // INSTAGRAM-STYLE LIKES
    // ==========================================

    @Transactional
    public Map<String, Object> toggleLikeReview(Long reviewId, User user) {
        Review review = getReview(reviewId);
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewAndUser(review, user);

        boolean liked;
        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            ReviewLike newLike = new ReviewLike(review, user);
            reviewLikeRepository.save(newLike);
            liked = true;

            // Trigger notification to review author (if not liking own review)
            if (review.getUser() != null && !review.getUser().getId().equals(user.getId())) {
                String userName = user.getFirstname() != null ? user.getFirstname() + " " + (user.getLastname() != null ? user.getLastname() : "") : "A Cinephile member";
                notificationService.createNotification(
                        review.getUser(),
                        user,
                        "REVIEW_LIKE",
                        userName.trim() + " liked your review for " + (review.getMovieTitle() != null ? review.getMovieTitle() : "a movie"),
                        review.getId(),
                        review.getMovieId(),
                        review.getMovieTitle()
                );
            }
        }

        long likesCount = reviewLikeRepository.countByReview(review);
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likesCount", likesCount);
        response.put("reviewId", reviewId);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewLikes(Long reviewId, User optionalUser) {
        Review review = getReview(reviewId);
        long count = reviewLikeRepository.countByReview(review);
        boolean isLiked = false;
        if (optionalUser != null) {
            isLiked = reviewLikeRepository.existsByReviewAndUser(review, optionalUser);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", count);
        response.put("isLiked", isLiked);
        response.put("reviewId", reviewId);
        return response;
    }

    @Transactional(readOnly = true)
    public long getLikesCount(Review review) {
        return reviewLikeRepository.countByReview(review);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Review review, User user) {
        if (user == null) return false;
        return reviewLikeRepository.existsByReviewAndUser(review, user);
    }

    @Transactional(readOnly = true)
    public long getCommentsCount(Review review) {
        return reviewCommentRepository.countByReview(review);
    }

    // ==========================================
    // INSTAGRAM-STYLE COMMENTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<ReviewCommentDTO> getReviewComments(Long reviewId) {
        Review review = getReview(reviewId);
        List<ReviewComment> comments = reviewCommentRepository.findByReviewOrderByCreatedAtAsc(review);

        // Build nested tree structure (top-level + replies)
        Map<Long, ReviewCommentDTO> dtoMap = new LinkedHashMap<>();
        List<ReviewCommentDTO> topLevelComments = new ArrayList<>();

        for (ReviewComment comment : comments) {
            ReviewCommentDTO dto = toCommentDTO(comment);
            dtoMap.put(comment.getId(), dto);

            if (comment.getParentId() == null) {
                topLevelComments.add(dto);
            }
        }

        for (ReviewComment comment : comments) {
            if (comment.getParentId() != null) {
                ReviewCommentDTO parent = dtoMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(dtoMap.get(comment.getId()));
                } else {
                    // Fallback if parent missing
                    topLevelComments.add(dtoMap.get(comment.getId()));
                }
            }
        }

        return topLevelComments;
    }

    @Transactional
    public ReviewCommentDTO addReviewComment(Long reviewId, String content, Long parentId, User user) {
        if (content == null || content.trim().isBlank()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        Review review = getReview(reviewId);

        ReviewComment comment = new ReviewComment(review, user, content.trim(), parentId);
        ReviewComment savedComment = reviewCommentRepository.save(comment);

        String actorName = user.getFirstname() != null ? user.getFirstname() + " " + (user.getLastname() != null ? user.getLastname() : "") : "Someone";
        actorName = actorName.trim();

        if (parentId != null) {
            // Replying to a comment
            ReviewComment parentComment = reviewCommentRepository.findById(parentId).orElse(null);
            if (parentComment != null && parentComment.getUser() != null && !parentComment.getUser().getId().equals(user.getId())) {
                notificationService.createNotification(
                        parentComment.getUser(),
                        user,
                        "COMMENT_REPLY",
                        actorName + " replied to your comment on " + (review.getMovieTitle() != null ? review.getMovieTitle() : "a review"),
                        review.getId(),
                        review.getMovieId(),
                        review.getMovieTitle()
                );
            }
        }

        // Notify review owner if not the commenter
        if (review.getUser() != null && !review.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    review.getUser(),
                    user,
                    "REVIEW_COMMENT",
                    actorName + " commented on your review for " + (review.getMovieTitle() != null ? review.getMovieTitle() : "a movie"),
                    review.getId(),
                    review.getMovieId(),
                    review.getMovieTitle()
            );
        }

        return toCommentDTO(savedComment);
    }

    @Transactional
    public void deleteReviewComment(Long reviewId, Long commentId, User user) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getReview().getId().equals(reviewId)) {
            throw new RuntimeException("Comment does not belong to this review");
        }

        // Only comment author OR review owner can delete
        boolean isCommentOwner = comment.getUser() != null && comment.getUser().getId().equals(user.getId());
        boolean isReviewOwner = comment.getReview().getUser() != null && comment.getReview().getUser().getId().equals(user.getId());

        if (!isCommentOwner && !isReviewOwner) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        // Delete any child replies
        reviewCommentRepository.deleteByParentId(commentId);
        reviewCommentRepository.delete(comment);
    }

    private ReviewCommentDTO toCommentDTO(ReviewComment comment) {
        User u = comment.getUser();
        String name = u != null ? ((u.getFirstname() != null ? u.getFirstname() : "") + " " + (u.getLastname() != null ? u.getLastname() : "")).trim() : "Cinephile";
        String username = u != null && u.getUsername() != null ? u.getUsername() : (u != null ? "user" + u.getId() : "cinephile");
        String avatar = u != null ? u.getAvatarUrl() : "";
        String createdAtStr = comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : "";

        return new ReviewCommentDTO(
                comment.getId(),
                comment.getReview() != null ? comment.getReview().getId() : null,
                u != null ? u.getId() : null,
                username,
                !name.isBlank() ? name : "Cinephile",
                avatar,
                comment.getContent(),
                comment.getParentId(),
                createdAtStr
        );
    }
}
