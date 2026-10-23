package com.example.backend.controller;

import com.example.backend.dto.ActivityFeedItemDTO;
import com.example.backend.dto.SocialUserDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.SocialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SocialController(
            SocialService socialService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.socialService = socialService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            socialService.followUser(userId, currentUser);
            return ResponseEntity.ok(Map.of("message", "Successfully followed user", "following", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            socialService.unfollowUser(userId, currentUser);
            return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user", "following", false));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/is-following/{userId}")
    public ResponseEntity<?> isFollowing(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveOptionalUser(authentication, authHeader);
            boolean isFollowing = socialService.isFollowing(userId, currentUser);
            return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isFollowing", false));
        }
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowers(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveOptionalUser(authentication, authHeader);
            List<SocialUserDTO> followers = socialService.getFollowers(userId, currentUser);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getFollowing(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveOptionalUser(authentication, authHeader);
            List<SocialUserDTO> following = socialService.getFollowing(userId, currentUser);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/counts/{userId}")
    public ResponseEntity<?> getFollowCounts(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(socialService.getFollowCounts(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getPersonalizedFeed(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            List<ActivityFeedItemDTO> feed = socialService.getPersonalizedFeed(currentUser);
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/community-feed")
    public ResponseEntity<?> getGlobalCommunityFeed() {
        try {
            List<ActivityFeedItemDTO> feed = socialService.getGlobalCommunityFeed();
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestedUsers(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User currentUser = resolveUser(authentication, authHeader);
            List<SocialUserDTO> suggestions = socialService.getSuggestedUsers(currentUser, limit);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private User resolveUser(Authentication authentication, String authorizationHeader) {
        String email = null;
        if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            email = authentication.getName();
        } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            if (jwtService.isTokenValid(token)) {
                email = jwtService.extractEmail(token);
            }
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Authorization token is missing or invalid");
        }

        final String userEmail = email;
        return userRepository.findByEmailaddress(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    private User resolveOptionalUser(Authentication authentication, String authorizationHeader) {
        try {
            return resolveUser(authentication, authorizationHeader);
        } catch (Exception e) {
            return null;
        }
    }
}
