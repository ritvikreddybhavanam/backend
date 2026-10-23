package com.example.backend.service;

import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.dto.UserProfileResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.FilmLogRepository;
import com.example.backend.repository.LikedMovieRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WatchedMovieRepository;
import com.example.backend.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WatchedMovieRepository watchedMovieRepository;
    private final LikedMovieRepository likedMovieRepository;
    private final WatchlistRepository watchlistRepository;
    private final ReviewRepository reviewRepository;
    private final FilmLogRepository filmLogRepository;

    public UserService(
            UserRepository userRepository,
            WatchedMovieRepository watchedMovieRepository,
            LikedMovieRepository likedMovieRepository,
            WatchlistRepository watchlistRepository,
            ReviewRepository reviewRepository,
            FilmLogRepository filmLogRepository
    ) {
        this.userRepository = userRepository;
        this.watchedMovieRepository = watchedMovieRepository;
        this.likedMovieRepository = likedMovieRepository;
        this.watchlistRepository = watchlistRepository;
        this.reviewRepository = reviewRepository;
        this.filmLogRepository = filmLogRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String emailaddress) {
        User user = userRepository.findByEmailaddress(emailaddress)
                .orElseThrow(() -> new RuntimeException("User not found: " + emailaddress));

        return buildProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return buildProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String emailaddress, UpdateProfileRequest request) {
        User user = userRepository.findByEmailaddress(emailaddress)
                .orElseThrow(() -> new RuntimeException("User not found: " + emailaddress));

        if (request.getFirstname() != null && !request.getFirstname().trim().isEmpty()) {
            user.setFirstname(request.getFirstname().trim());
        }

        if (request.getLastname() != null && !request.getLastname().trim().isEmpty()) {
            user.setLastname(request.getLastname().trim());
        }

        if (request.getUsername() != null) {
            String trimmedUsername = request.getUsername().trim();
            if (!trimmedUsername.isEmpty() && !trimmedUsername.equalsIgnoreCase(user.getUsername())) {
                userRepository.findByUsernameIgnoreCase(trimmedUsername)
                        .filter(u -> !u.getId().equals(user.getId()))
                        .ifPresent(u -> {
                            throw new RuntimeException("Username '" + trimmedUsername + "' is already taken.");
                        });
                user.setUsername(trimmedUsername);
            } else if (trimmedUsername.isEmpty()) {
                user.setUsername(null);
            }
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }

        if (request.getPronouns() != null) {
            user.setPronouns(request.getPronouns().trim());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }

        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite().trim());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        if (request.getBannerUrl() != null) {
            user.setBannerUrl(request.getBannerUrl().trim());
        }

        if (request.getTagline() != null) {
            user.setTagline(request.getTagline().trim());
        }

        if (request.getLetterboxd() != null) {
            user.setLetterboxd(request.getLetterboxd().trim());
        }

        if (request.getImdb() != null) {
            user.setImdb(request.getImdb().trim());
        }

        if (request.getTmdb() != null) {
            user.setTmdb(request.getTmdb().trim());
        }

        if (request.getTwitter() != null) {
            user.setTwitter(request.getTwitter().trim());
        }

        if (request.getInstagram() != null) {
            user.setInstagram(request.getInstagram().trim());
        }

        if (request.getFavoriteMovieIds() != null) {
            user.setFavoriteMovieIds(request.getFavoriteMovieIds().trim());
        }

        if (request.getFavoriteGenres() != null) {
            user.setFavoriteGenres(request.getFavoriteGenres().trim());
        }

        if (request.getFavoriteDirectors() != null) {
            user.setFavoriteDirectors(request.getFavoriteDirectors().trim());
        }

        if (request.getFavoriteActors() != null) {
            user.setFavoriteActors(request.getFavoriteActors().trim());
        }

        User savedUser = userRepository.save(user);
        return buildProfileResponse(savedUser);
    }

    private UserProfileResponse buildProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setFirstname(user.getFirstname());
        response.setLastname(user.getLastname());
        response.setEmailaddress(user.getEmailaddress());
        response.setUsername(user.getUsername());
        response.setBio(user.getBio());
        response.setPronouns(user.getPronouns());
        response.setLocation(user.getLocation());
        response.setWebsite(user.getWebsite());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setBannerUrl(user.getBannerUrl());
        response.setTagline(user.getTagline());
        response.setLetterboxd(user.getLetterboxd());
        response.setImdb(user.getImdb());
        response.setTmdb(user.getTmdb());
        response.setTwitter(user.getTwitter());
        response.setInstagram(user.getInstagram());
        response.setFavoriteMovieIds(user.getFavoriteMovieIds());
        response.setFavoriteGenres(user.getFavoriteGenres());
        response.setFavoriteDirectors(user.getFavoriteDirectors());
        response.setFavoriteActors(user.getFavoriteActors());
        response.setCreatedAt(user.getCreatedAt());

        // Count stats
        try {
            response.setTotalFilmsWatched(watchedMovieRepository.countByUser(user));
        } catch (Exception e) {
            response.setTotalFilmsWatched(0);
        }

        try {
            response.setTotalFilmsLiked(likedMovieRepository.countByUser(user));
        } catch (Exception e) {
            response.setTotalFilmsLiked(0);
        }

        try {
            response.setTotalWatchlist(watchlistRepository.countByUser(user));
        } catch (Exception e) {
            response.setTotalWatchlist(0);
        }

        try {
            response.setTotalReviews(reviewRepository.countByUser(user));
        } catch (Exception e) {
            response.setTotalReviews(0);
        }

        try {
            response.setTotalDiaryLogs(filmLogRepository.countByUser(user));
        } catch (Exception e) {
            response.setTotalDiaryLogs(0);
        }

        return response;
    }
}
