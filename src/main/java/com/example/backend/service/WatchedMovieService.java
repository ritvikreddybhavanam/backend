package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.WatchedMovie;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WatchedMovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchedMovieService {

    private final WatchedMovieRepository watchedMovieRepository;
    private final UserRepository userRepository;

    public WatchedMovieService(
            WatchedMovieRepository watchedMovieRepository,
            UserRepository userRepository
    ) {
        this.watchedMovieRepository =
                watchedMovieRepository;

        this.userRepository =
                userRepository;
    }

    public List<WatchedMovie> getWatchedMovies(
            String email
    ) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        return userRepository
                .findByEmailaddress(email)
                .map(watchedMovieRepository::findByUserOrderByWatchedAtDesc)
                .orElse(List.of());
    }

    public WatchedMovie addWatchedMovie(
            String email,
            Long movieId,
            String title,
            String poster,
            Integer year,
            String genre,
            String service,
            Double rating,
            String language
    ) {

        User user = userRepository
                .findByEmailaddress(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        if (watchedMovieRepository
                .existsByUserAndMovieId(user, movieId)) {

            throw new RuntimeException(
                    "Movie is already marked as watched"
            );
        }

        WatchedMovie watchedMovie =
                new WatchedMovie();

        watchedMovie.setUser(user);
        watchedMovie.setMovieId(movieId);
        watchedMovie.setTitle(title);
        watchedMovie.setPoster(poster);
        watchedMovie.setYear(year);
        watchedMovie.setGenre(genre);
        watchedMovie.setService(service);
        watchedMovie.setRating(rating);
        watchedMovie.setLanguage(language);

        return watchedMovieRepository.save(
                watchedMovie
        );
    }

    @Transactional
    public void removeWatchedMovie(
            String email,
            Long movieId
    ) {

        User user = userRepository
                .findByEmailaddress(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        watchedMovieRepository.deleteByUserAndMovieId(
                user,
                movieId
        );
    }

    public boolean isWatched(
            String email,
            Long movieId
    ) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return userRepository
                .findByEmailaddress(email)
                .map(user -> watchedMovieRepository.existsByUserAndMovieId(user, movieId))
                .orElse(false);
    }
}