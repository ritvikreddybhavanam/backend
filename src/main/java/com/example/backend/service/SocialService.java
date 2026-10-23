package com.example.backend.service;

import com.example.backend.dto.ActivityFeedItemDTO;
import com.example.backend.dto.SocialUserDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SocialService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final FilmLogRepository filmLogRepository;
    private final ReviewRepository reviewRepository;
    private final MovieListRepository movieListRepository;
    private final NotificationService notificationService;

    public SocialService(
            UserFollowRepository userFollowRepository,
            UserRepository userRepository,
            FilmLogRepository filmLogRepository,
            ReviewRepository reviewRepository,
            MovieListRepository movieListRepository,
            NotificationService notificationService
    ) {
        this.userFollowRepository = userFollowRepository;
        this.userRepository = userRepository;
        this.filmLogRepository = filmLogRepository;
        this.reviewRepository = reviewRepository;
        this.movieListRepository = movieListRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public boolean followUser(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + targetUserId));

        if (userFollowRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            return true; // Already following
        }

        UserFollow follow = new UserFollow(currentUser, targetUser);
        userFollowRepository.save(follow);

        // Send follow notification
        String currentName = ((currentUser.getFirstname() != null ? currentUser.getFirstname() : "") + " " + (currentUser.getLastname() != null ? currentUser.getLastname() : "")).trim();
        notificationService.createNotification(
                targetUser,
                currentUser,
                "FOLLOW",
                (!currentName.isBlank() ? currentName : "Someone") + " started following you.",
                currentUser.getId(),
                null,
                null
        );

        return true;
    }

    @Transactional
    public boolean unfollowUser(Long targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + targetUserId));

        userFollowRepository.deleteByFollowerAndFollowing(currentUser, targetUser);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long targetUserId, User currentUser) {
        if (currentUser == null || currentUser.getId().equals(targetUserId)) {
            return false;
        }
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser == null) return false;
        return userFollowRepository.existsByFollowerAndFollowing(currentUser, targetUser);
    }

    @Transactional(readOnly = true)
    public List<SocialUserDTO> getFollowers(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> followers = userFollowRepository.findFollowerUsersByFollowing(user);
        return followers.stream().map(u -> toSocialUserDTO(u, currentUser)).toList();
    }

    @Transactional(readOnly = true)
    public List<SocialUserDTO> getFollowing(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> following = userFollowRepository.findFollowingUsersByFollower(user);
        return following.stream().map(u -> toSocialUserDTO(u, currentUser)).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getFollowCounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long followersCount = userFollowRepository.countByFollowing(user);
        long followingCount = userFollowRepository.countByFollower(user);

        Map<String, Long> counts = new HashMap<>();
        counts.put("followersCount", followersCount);
        counts.put("followingCount", followingCount);
        return counts;
    }

    /**
     * Personalized Friend Activity Feed (Logs, Reviews, Lists by people you follow + your own recent activity)
     */
    @Transactional(readOnly = true)
    public List<ActivityFeedItemDTO> getPersonalizedFeed(User currentUser) {
        List<User> networkUsers = new ArrayList<>(userFollowRepository.findFollowingUsersByFollower(currentUser));
        networkUsers.add(currentUser); // Include current user's activities as well

        if (networkUsers.isEmpty()) {
            return Collections.emptyList();
        }

        List<ActivityFeedItemDTO> feed = new ArrayList<>();

        // 1. Film Diary Logs
        List<FilmLog> logs = filmLogRepository.findByUserInOrderByCreatedAtDesc(networkUsers);
        for (FilmLog log : logs) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("log-" + log.getId());
            item.setType("DIARY_LOG");
            item.setTimestamp(log.getCreatedAt() != null ? log.getCreatedAt() : (log.getWatchedDate() != null ? log.getWatchedDate().atStartOfDay() : java.time.LocalDateTime.now()));
            populateUser(item, log.getUser());
            item.setMovieId(log.getMovieId());
            item.setMovieTitle(log.getTitle());
            item.setMoviePoster(log.getPoster());
            item.setMovieYear(log.getYear());
            item.setRating(log.getRating());
            item.setIsRewatch(log.isRewatch());
            item.setVenueOrPlatform(log.getVenueOrPlatform());
            item.setNotes(log.getReviewNotes());
            feed.add(item);
        }

        // 2. Reviews
        List<Review> reviews = reviewRepository.findByUserInOrderByCreatedAtDesc(networkUsers);
        for (Review review : reviews) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("review-" + review.getId());
            item.setType("REVIEW");
            item.setTimestamp(review.getCreatedAt() != null ? review.getCreatedAt() : java.time.LocalDateTime.now());
            populateUser(item, review.getUser());
            item.setMovieId(review.getMovieId());
            item.setMovieTitle(review.getMovieTitle());
            item.setMoviePoster(review.getPoster());
            item.setMovieYear(review.getYear());
            item.setRating(review.getRating());
            item.setReviewText(review.getContent());
            feed.add(item);
        }

        // 3. Curated Lists
        List<MovieList> lists = movieListRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(networkUsers);
        for (MovieList list : lists) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("list-" + list.getId());
            item.setType("LIST_CREATED");
            item.setTimestamp(list.getCreatedAt() != null ? list.getCreatedAt() : java.time.LocalDateTime.now());
            populateUser(item, list.getUser());
            item.setListId(list.getId());
            item.setListName(list.getName());
            item.setListDescription(list.getDescription());
            item.setListItemCount(list.getItems() != null ? list.getItems().size() : 0);
            if (list.getItems() != null && !list.getItems().isEmpty()) {
                item.setMoviePoster(list.getItems().get(0).getPoster());
            }
            feed.add(item);
        }

        // Sort descending by timestamp
        feed.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return feed.size() > 50 ? feed.subList(0, 50) : feed;
    }

    /**
     * Global Community Activity Stream across all Cinephile members
     */
    public List<ActivityFeedItemDTO> getGlobalCommunityFeed() {
        List<ActivityFeedItemDTO> feed = new ArrayList<>();

        // Top 30 Diary Logs
        List<FilmLog> logs = filmLogRepository.findTop30ByOrderByCreatedAtDesc();
        for (FilmLog log : logs) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("log-" + log.getId());
            item.setType("DIARY_LOG");
            item.setTimestamp(log.getCreatedAt() != null ? log.getCreatedAt() : java.time.LocalDateTime.now());
            populateUser(item, log.getUser());
            item.setMovieId(log.getMovieId());
            item.setMovieTitle(log.getTitle());
            item.setMoviePoster(log.getPoster());
            item.setMovieYear(log.getYear());
            item.setRating(log.getRating());
            item.setIsRewatch(log.isRewatch());
            item.setVenueOrPlatform(log.getVenueOrPlatform());
            item.setNotes(log.getReviewNotes());
            feed.add(item);
        }

        // Top 30 Reviews
        List<Review> reviews = reviewRepository.findTop30ByOrderByCreatedAtDesc();
        for (Review review : reviews) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("review-" + review.getId());
            item.setType("REVIEW");
            item.setTimestamp(review.getCreatedAt() != null ? review.getCreatedAt() : java.time.LocalDateTime.now());
            populateUser(item, review.getUser());
            item.setMovieId(review.getMovieId());
            item.setMovieTitle(review.getMovieTitle());
            item.setMoviePoster(review.getPoster());
            item.setMovieYear(review.getYear());
            item.setRating(review.getRating());
            item.setReviewText(review.getContent());
            feed.add(item);
        }

        // Top 30 Curated Lists
        List<MovieList> lists = movieListRepository.findTop30ByIsPublicTrueOrderByCreatedAtDesc();
        for (MovieList list : lists) {
            ActivityFeedItemDTO item = new ActivityFeedItemDTO();
            item.setId("list-" + list.getId());
            item.setType("LIST_CREATED");
            item.setTimestamp(list.getCreatedAt() != null ? list.getCreatedAt() : java.time.LocalDateTime.now());
            populateUser(item, list.getUser());
            item.setListId(list.getId());
            item.setListName(list.getName());
            item.setListDescription(list.getDescription());
            item.setListItemCount(list.getItems() != null ? list.getItems().size() : 0);
            if (list.getItems() != null && !list.getItems().isEmpty()) {
                item.setMoviePoster(list.getItems().get(0).getPoster());
            }
            feed.add(item);
        }

        // Sort descending by timestamp
        feed.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return feed.size() > 50 ? feed.subList(0, 50) : feed;
    }

    /**
     * Get suggested users to follow
     */
    @Transactional(readOnly = true)
    public List<SocialUserDTO> getSuggestedUsers(User currentUser, int limit) {
        List<User> allUsers = userRepository.findAll();
        List<User> following = userFollowRepository.findFollowingUsersByFollower(currentUser);
        Set<Long> followingIds = new HashSet<>();
        following.forEach(u -> followingIds.add(u.getId()));
        followingIds.add(currentUser.getId()); // Exclude self

        return allUsers.stream()
                .filter(u -> !followingIds.contains(u.getId()))
                .limit(limit)
                .map(u -> toSocialUserDTO(u, currentUser))
                .toList();
    }

    private void populateUser(ActivityFeedItemDTO item, User user) {
        if (user != null) {
            item.setUserId(user.getId());
            String name = (user.getFirstname() != null ? user.getFirstname() : "") +
                    (user.getLastname() != null ? " " + user.getLastname() : "");
            item.setUserName(!name.isBlank() ? name.trim() : "Cinephile");
            item.setUserUsername(user.getUsername() != null ? user.getUsername() : "user" + user.getId());
            item.setUserAvatar(user.getAvatarUrl());
        }
    }

    private SocialUserDTO toSocialUserDTO(User user, User currentUser) {
        boolean isFollowing = false;
        if (currentUser != null && !currentUser.getId().equals(user.getId())) {
            isFollowing = userFollowRepository.existsByFollowerAndFollowing(currentUser, user);
        }
        return new SocialUserDTO(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getTagline(),
                isFollowing
        );
    }
}
