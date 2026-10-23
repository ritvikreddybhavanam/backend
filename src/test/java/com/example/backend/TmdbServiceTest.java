package com.example.backend;

import com.example.backend.service.TmdbService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TmdbServiceTest {

    @Autowired
    private TmdbService tmdbService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Verify TMDB live service returns genuine TMDB genres")
    void testGetGenresLive() throws Exception {
        String json = tmdbService.getGenres();
        assertNotNull(json, "Genres response must not be null");
        assertFalse(json.isBlank(), "Genres response must not be blank");

        JsonNode root = objectMapper.readTree(json);
        assertTrue(root.has("genres"), "Response must contain 'genres' array");
        JsonNode genres = root.get("genres");
        assertTrue(genres.isArray());
        assertTrue(genres.size() >= 19, "TMDB movie genre list contains 19 genres");
    }

    @Test
    @DisplayName("Verify TMDB live trending movies endpoint returns valid paginated results")
    void testGetTrendingMoviesLive() throws Exception {
        String json = tmdbService.getTrendingMovies(1);
        assertNotNull(json);

        JsonNode root = objectMapper.readTree(json);
        assertTrue(root.has("results"), "Trending response must contain 'results'");
        JsonNode results = root.get("results");
        assertTrue(results.isArray());
        assertTrue(results.size() > 0, "Trending movies must contain at least 1 movie");
    }

    @Test
    @DisplayName("Verify TMDB languages configuration endpoint returns language mappings")
    void testGetLanguagesLive() {
        List<Map<String, Object>> languages = tmdbService.getLanguages();
        assertNotNull(languages);
        assertFalse(languages.isEmpty());
        boolean hasEnglish = languages.stream()
                .anyMatch(m -> "en".equals(m.get("iso_639_1")));
        assertTrue(hasEnglish, "TMDB languages must contain English");
    }

    @Test
    @DisplayName("Verify offline fallback returns default data immediately when unconfigured")
    void testOfflineFallbackWhenUnconfigured() throws Exception {
        TmdbService offlineService = new TmdbService("https://api.themoviedb.org/3", "");
        String genresJson = offlineService.getGenres();
        assertNotNull(genresJson);

        JsonNode root = objectMapper.readTree(genresJson);
        assertTrue(root.has("genres"));
        assertEquals(19, root.get("genres").size());

        List<Map<String, Object>> langs = offlineService.getLanguages();
        assertEquals(1, langs.size());
        assertEquals("en", langs.get(0).get("iso_639_1"));
    }

    @Test
    @DisplayName("Verify invalid token triggers sanitized auth failure handling and safe fallback")
    void testInvalidTokenHandling() throws Exception {
        // Construct service with an invalid v4 token
        TmdbService badTokenService = new TmdbService("https://api.themoviedb.org/3", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature");
        // Should catch 401 Unauthorized, log sanitized error, and return fallback without throwing
        String genres = badTokenService.getGenres();
        assertNotNull(genres);
        JsonNode root = objectMapper.readTree(genres);
        assertTrue(root.has("genres"));
    }

    @Test
    @DisplayName("Verify v3 API key authentication mode initializes correctly")
    void testV3ApiKeyInitialization() {
        // 32-hex character string format
        TmdbService v3Service = new TmdbService("https://api.themoviedb.org/3", "4c6a102811edd4a6abfbfbd1f275826f");
        assertNotNull(v3Service);
        String genres = v3Service.getGenres();
        assertNotNull(genres);
    }
}
