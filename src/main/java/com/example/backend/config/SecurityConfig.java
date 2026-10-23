package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"message\":\"Unauthorized: Please log in to continue\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/tmdb/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/prediction/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/reviews/my"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reviews/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/content-guide/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/content-guide/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/liked-movies/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/reviews/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/watchlist/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/watched/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/diary/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/lists/my",
                                "/api/lists/my/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/lists/public",
                                "/api/lists/*"
                        ).permitAll()

                        .requestMatchers(
                                "/api/lists/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/social/followers/**",
                                "/api/social/following/**",
                                "/api/social/is-following/**",
                                "/api/social/counts/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/social/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/notifications/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/users/profile"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*"
                        ).permitAll()

                        .requestMatchers(
                                "/api/users/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/data/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/cinematch/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:[*]",
                        "http://127.0.0.1:[*]",
                        "http://10.250.9.30:[*]",
                        "http://10.*:[*]",
                        "http://192.168.*:[*]",
                        "http://172.*:[*]",
                        "https://*.app.github.dev",
                        "https://*.devtunnels.ms",
                        "https://*.githubpreview.dev",
                        "https://*.visualstudio.com",
                        "https://*.tunnels.ms",
                        "*"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Content-Disposition"
                )
        );

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}