package com.example.backend.repository;

import com.example.backend.entity.Review;
import com.example.backend.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    @Query("SELECT c FROM ReviewComment c JOIN FETCH c.user WHERE c.review = :review ORDER BY c.createdAt ASC")
    List<ReviewComment> findByReviewOrderByCreatedAtAsc(@Param("review") Review review);

    long countByReview(Review review);

    List<ReviewComment> findByParentId(Long parentId);

    @Modifying
    @Query("DELETE FROM ReviewComment c WHERE c.parentId = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Query("DELETE FROM ReviewComment c WHERE c.review = :review")
    void deleteByReview(@Param("review") Review review);
}
