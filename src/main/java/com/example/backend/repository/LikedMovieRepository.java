package com.example.backend.repository;

import com.example.backend.entity.LikedMovie;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedMovieRepository extends JpaRepository<LikedMovie, Long> {

    List<LikedMovie> findByUserOrderByLikedAtDesc(User user);

    Optional<LikedMovie> findByUserAndTmdbMovieId(
            User user,
            Long tmdbMovieId
    );

    boolean existsByUserAndTmdbMovieId(
            User user,
            Long tmdbMovieId
    );

    void deleteByUserAndTmdbMovieId(
            User user,
            Long tmdbMovieId
    );

    long countByUser(User user);
}