package com.example.backend.repository;

import com.example.backend.entity.Review;
import com.example.backend.entity.ReviewLike;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    long countByReview(Review review);

    boolean existsByReviewAndUser(Review review, User user);

    Optional<ReviewLike> findByReviewAndUser(Review review, User user);

    @Modifying
    @Query("DELETE FROM ReviewLike l WHERE l.review = :review AND l.user = :user")
    void deleteByReviewAndUser(@Param("review") Review review, @Param("user") User user);

    @Modifying
    @Query("DELETE FROM ReviewLike l WHERE l.review = :review")
    void deleteByReview(@Param("review") Review review);
}
