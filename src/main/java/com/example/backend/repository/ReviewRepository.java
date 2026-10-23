package com.example.backend.repository;

import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    List<Review> findByUserInOrderByCreatedAtDesc(List<User> users);

    List<Review> findTop30ByOrderByCreatedAtDesc();

    Optional<Review> findByMovieIdAndUser(Long movieId, User user);

    long countByUser(User user);
}