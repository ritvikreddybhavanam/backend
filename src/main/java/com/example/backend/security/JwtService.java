package com.example.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // Hardcoded developer fallback key strictly for offline local dev/testing
    private static final String DEV_FALLBACK_SECRET =
            "CinephileProjectSecretKeyForJwtAuthentication2026VerySecureKey";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        boolean isProduction = "prod".equalsIgnoreCase(activeProfile)
                || "production".equalsIgnoreCase(activeProfile)
                || System.getenv("RENDER") != null
                || System.getenv("PRODUCTION") != null;

        String secret = (configuredSecret != null && !configuredSecret.trim().isEmpty())
                ? configuredSecret.trim()
                : null;

        if (secret == null) {
            if (isProduction) {
                throw new IllegalStateException(
                        "CRITICAL SECURITY CONFIGURATION ERROR: JWT signing secret (JWT_SECRET / jwt.secret) " +
                        "is not configured in production environment. Application startup aborted to prevent insecure token generation."
                );
            }
            // Local dev mode fallback to maintain backward compatibility with local tests and clients
            System.out.println("⚠️ [WARN] JWT_SECRET environment variable is not configured. Falling back to local development signing key.");
            secret = DEV_FALLBACK_SECRET;
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT signing secret must be at least 32 bytes (256 bits) for HMAC-SHA256. Provided length: " + keyBytes.length
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            init();
        }
        return signingKey;
    }

    public String generateToken(String emailaddress) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(emailaddress)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
