package com.example.backend.controller;

import com.example.backend.dto.FilmDiaryStatsResponse;
import com.example.backend.dto.FilmLogRequest;
import com.example.backend.entity.FilmLog;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.FilmDiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class FilmDiaryController {

    private final FilmDiaryService filmDiaryService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public FilmDiaryController(
            FilmDiaryService filmDiaryService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.filmDiaryService = filmDiaryService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> getMyDiary(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            List<FilmLog> diary = filmDiaryService.getDiaryByUser(user);
            return ResponseEntity.ok(diary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/month")
    public ResponseEntity<?> getDiaryByMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            List<FilmLog> diary = filmDiaryService.getDiaryByUserAndMonth(user, year, month);
            return ResponseEntity.ok(diary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> logFilm(
            @RequestBody FilmLogRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            FilmLog logged = filmDiaryService.logFilm(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(logged);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLog(
            @PathVariable Long id,
            @RequestBody FilmLogRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            FilmLog updated = filmDiaryService.updateLog(id, request, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLog(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            filmDiaryService.deleteLog(id, user);
            return ResponseEntity.ok("Diary entry removed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/years")
    public ResponseEntity<?> getAvailableYears(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            List<Integer> years = filmDiaryService.getAvailableYears(user);
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(required = false) Integer year,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            User user = resolveUser(authentication, authHeader);
            int targetYear = (year != null) ? year : LocalDate.now().getYear();
            FilmDiaryStatsResponse stats = filmDiaryService.getStatsForYear(user, targetYear);
            return ResponseEntity.ok(stats);
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
}
