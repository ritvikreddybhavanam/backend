package com.example.backend.repository;

import com.example.backend.entity.Watchlist;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository
        extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUser(User user);

    Optional<Watchlist> findByUserAndMovieId(
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