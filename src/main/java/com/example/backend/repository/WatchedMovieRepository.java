package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchedMovieRepository
        extends JpaRepository<WatchedMovie, Long> {

    List<WatchedMovie> findByUserOrderByWatchedAtDesc(
            User user
    );

    Optional<WatchedMovie> findByUserAndMovieId(
            User user,
            Long movieId
    );

    boolean existsByUserAndMovieId(
            User user,
            Long movieId
    );

    void deleteByUserAndMovieId(
            User user,
            Long movieId
    );

    long countByUser(User user);
}