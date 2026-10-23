package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        System.out.println(
                "JWT FILTER: " +
                        request.getMethod() +
                        " " +
                        requestUri
        );

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT FILTER: No Bearer token - continuing"
            );

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {

            System.out.println(
                    "JWT FILTER: Empty token - continuing"
            );

            filterChain.doFilter(request, response);
            return;
        }

        try {

            boolean valid =
                    jwtService.isTokenValid(token);

            System.out.println(
                    "JWT TOKEN VALID: " + valid
            );

            if (valid) {

                String email =
                        jwtService.extractEmail(token);

                System.out.println(
                        "JWT EMAIL: " + email
                );

                if (email != null &&
                        !email.isBlank()) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    Collections.emptyList()
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );

                    System.out.println(
                            "JWT FILTER: Authentication set"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR: " +
                            e.getClass().getSimpleName() +
                            " - " +
                            e.getMessage()
            );

            SecurityContextHolder
                    .clearContext();
        }

        System.out.println(
                "JWT FILTER: Continuing request"
        );

        filterChain.doFilter(request, response);
    }
}