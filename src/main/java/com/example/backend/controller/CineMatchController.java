package com.example.backend.controller;

import com.example.backend.dto.CineMatchRoomResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.CineMatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cinematch")
public class CineMatchController {

    private final CineMatchService cineMatchService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CineMatchController(
            CineMatchService cineMatchService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.cineMatchService = cineMatchService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            String genre = body != null ? body.get("genre") : "";
            String providers = body != null ? body.get("providers") : "";
            CineMatchRoomResponse res = cineMatchService.createRoom(user, genre, providers);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @PostMapping("/join/{roomCode}")
    public ResponseEntity<?> joinRoom(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            CineMatchRoomResponse res = cineMatchService.joinRoom(user, roomCode);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> getRoomStatus(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            CineMatchRoomResponse res = cineMatchService.getRoomStatus(roomCode, user);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @PostMapping("/room/{roomCode}/start")
    public ResponseEntity<?> startSwiping(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            CineMatchRoomResponse res = cineMatchService.startSwiping(roomCode, user);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @PostMapping("/room/{roomCode}/swipe")
    public ResponseEntity<?> swipeMovie(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            Long movieId = Long.valueOf(payload.get("movieId").toString());
            boolean isLike = Boolean.parseBoolean(payload.get("isLike").toString());
            CineMatchRoomResponse res = cineMatchService.recordSwipe(roomCode, user, movieId, isLike);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
        }
    }

    @PostMapping("/room/{roomCode}/reset")
    public ResponseEntity<?> resetRoom(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            CineMatchRoomResponse res = cineMatchService.resetRoom(roomCode, user);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Bad request", "error", e.getMessage() != null ? e.getMessage() : "Bad request"));
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
