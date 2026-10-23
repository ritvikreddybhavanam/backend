package com.example.backend.controller;

import com.example.backend.dto.ContentAdvisoryRequest;
import com.example.backend.dto.ContentGuideResponse;
import com.example.backend.entity.ContentAdvisory;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.ContentAdvisoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content-guide")
public class ContentAdvisoryController {

    private final ContentAdvisoryService advisoryService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ContentAdvisoryController(
            ContentAdvisoryService advisoryService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.advisoryService = advisoryService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getContentGuide(@PathVariable Long movieId) {
        try {
            ContentGuideResponse res = advisoryService.getMovieContentGuide(movieId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Error", "error", e.getMessage() != null ? e.getMessage() : "Error"));
        }
    }

    @PostMapping("/advisory")
    public ResponseEntity<?> submitAdvisory(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ContentAdvisoryRequest request
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            ContentAdvisory advisory = advisoryService.submitAdvisory(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "id", advisory.getId(),
                    "category", advisory.getCategory(),
                    "severity", advisory.getSeverity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Error", "error", e.getMessage() != null ? e.getMessage() : "Error"));
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
}
