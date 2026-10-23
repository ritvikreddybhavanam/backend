package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class TmdbService {

    private final RestClient restClient;
    private final boolean isConfigured;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final List<Map<String, Object>> FALLBACK_LANGUAGES = List.of(
            Map.of("iso_639_1", "en", "english_name", "English", "name", "English")
    );

    private static final String FALLBACK_GENRES =
            "{\"genres\":[{\"id\":28,\"name\":\"Action\"},{\"id\":12,\"name\":\"Adventure\"},{\"id\":16,\"name\":\"Animation\"}," +
            "{\"id\":35,\"name\":\"Comedy\"},{\"id\":80,\"name\":\"Crime\"},{\"id\":99,\"name\":\"Documentary\"}," +
            "{\"id\":18,\"name\":\"Drama\"},{\"id\":10751,\"name\":\"Family\"},{\"id\":14,\"name\":\"Fantasy\"}," +
            "{\"id\":36,\"name\":\"History\"},{\"id\":27,\"name\":\"Horror\"},{\"id\":10402,\"name\":\"Music\"}," +
            "{\"id\":9648,\"name\":\"Mystery\"},{\"id\":10749,\"name\":\"Romance\"},{\"id\":878,\"name\":\"Science Fiction\"}," +
            "{\"id\":10770,\"name\":\"TV Movie\"},{\"id\":53,\"name\":\"Thriller\"},{\"id\":10752,\"name\":\"War\"},{\"id\":37,\"name\":\"Western\"}]}";

    private static final String FALLBACK_MOVIES = "{\"page\":1,\"results\":[],\"total_pages\":1,\"total_results\":0}";
    private static final String FALLBACK_EMPTY_OBJ = "{}";
    private static final String FALLBACK_VIDEOS = "{\"id\":0,\"results\":[]}";
    private static final String FALLBACK_CREDITS = "{\"id\":0,\"cast\":[],\"crew\":[]}";
    private static final String FALLBACK_PROVIDERS = "{\"results\":[{\"provider_id\":8,\"provider_name\":\"Netflix\",\"logo_path\":\"/rK1KljqmbvO9HQa1PBFLILWah72.png\",\"display_priority\":0},{\"provider_id\":119,\"provider_name\":\"Amazon Prime Video\",\"logo_path\":\"/gMZdpavHmxFNnLpMHwVxfqeux2g.png\",\"display_priority\":1},{\"provider_id\":2336,\"provider_name\":\"JioHotstar\",\"logo_path\":\"/ledoS6EgdjTNq8F1e6wubUQer18.png\",\"display_priority\":2},{\"provider_id\":350,\"provider_name\":\"Apple TV+\",\"logo_path\":\"/9icYBfYFcwgCbky5VdGUIKJ4C5i.png\",\"display_priority\":3},{\"provider_id\":232,\"provider_name\":\"ZEE5\",\"logo_path\":\"/5NyLm42TmCqCMOZFv249srCqK7z.jpg\",\"display_priority\":4},{\"provider_id\":237,\"provider_name\":\"Sony LIV\",\"logo_path\":\"/edg2H1PZzQe7kZ8l0f6Z0Bw51rV.jpg\",\"display_priority\":5},{\"provider_id\":122,\"provider_name\":\"Disney+ Hotstar\",\"logo_path\":\"/7rwSXluNWZAlJn243k102z6XW0d.jpg\",\"display_priority\":6},{\"provider_id\":220,\"provider_name\":\"JioCinema\",\"logo_path\":\"/1VbE6U2bZ9vK7h2M3b1q2A4yB8.jpg\",\"display_priority\":7},{\"provider_id\":337,\"provider_name\":\"Disney+\",\"logo_path\":\"/7rwSXluNWZAlJn243k102z6XW0d.jpg\",\"display_priority\":8},{\"provider_id\":1899,\"provider_name\":\"Max\",\"logo_path\":\"/6uhKBfmtzFqOcLoulGbZoxbpRaQ.jpg\",\"display_priority\":9},{\"provider_id\":531,\"provider_name\":\"Paramount+\",\"logo_path\":\"/xbhGlaFLBC24e4vgM1sX2ZtqObe.jpg\",\"display_priority\":10},{\"provider_id\":15,\"provider_name\":\"Hulu\",\"logo_path\":\"/zxrVdFjIjLqkfnwyghn2XuafNDD.jpg\",\"display_priority\":11},{\"provider_id\":386,\"provider_name\":\"Peacock\",\"logo_path\":\"/g1e16tTqS1xdP4Y7x3U8N30v74l.jpg\",\"display_priority\":12}]}";

    private static class CacheEntry {
        final String data;
        final long expiresAt;

        CacheEntry(String data, long expiresAt) {
            this.data = data;
            this.expiresAt = expiresAt;
        }
    }

    public TmdbService(
            @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl,
            @Value("${tmdb.api.token:}") String token
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(6));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                );

        String cleanToken = (token != null) ? token.trim() : "";
        if (!cleanToken.isEmpty()) {
            this.isConfigured = true;
            if (cleanToken.startsWith("ey") || cleanToken.length() > 40) {
                // TMDB v4 Read Access Token (Bearer authorization header)
                builder.defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + cleanToken
                );
                System.out.println("✅ [TMDB] Configured TMDB client with v4 Read Access Token (Bearer authentication).");
            } else {
                // TMDB v3 API Key (query parameter api_key)
                builder.requestInterceptor((request, body, execution) -> {
                    URI originalUri = request.getURI();
                    String query = originalUri.getQuery();
                    if (query != null && query.contains("api_key=")) {
                        return execution.execute(request, body);
                    }
                    URI uri = UriComponentsBuilder.fromUri(originalUri)
                            .queryParam("api_key", cleanToken)
                            .build(true)
                            .toUri();
                    HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
                        @Override
                        public URI getURI() {
                            return uri;
                        }
                    };
                    return execution.execute(modifiedRequest, body);
                });
                System.out.println("✅ [TMDB] Configured TMDB client with v3 API Key (query authentication).");
            }
        } else {
            this.isConfigured = false;
            System.out.println("⚠️ [WARN] TMDB API credentials (TMDB_API_TOKEN or TMDB_API_KEY) are not configured. Operating in offline/mock fallback mode.");
        }

        this.restClient = builder.build();
    }

    private String getFromTmdb(String cacheKey, long ttlSeconds, Supplier<String> supplier, String fallbackJson) {
        CacheEntry entry = cache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (entry != null && now < entry.expiresAt) {
            return entry.data;
        }

        if (!isConfigured) {
            return (entry != null) ? entry.data : fallbackJson;
        }

        try {
            String data = supplier.get();
            if (data != null && !data.isBlank()) {
                cache.put(cacheKey, new CacheEntry(data, now + (ttlSeconds * 1000L)));
                return data;
            }
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 401 || status == 403) {
                System.err.println("❌ [TMDB AUTH ERROR] HTTP " + status + " for operation [" + cacheKey +
                        "]: Configured TMDB credential is invalid or has expired. Please check TMDB_API_TOKEN / TMDB_API_KEY.");
            } else if (status == 429) {
                System.err.println("⚠️ [TMDB RATE LIMIT] HTTP 429 for operation [" + cacheKey + "]: Rate limit exceeded.");
            } else if (status == 404) {
                System.err.println("⚠️ [TMDB NOT FOUND] HTTP 404 for operation [" + cacheKey + "]: Resource not found.");
            } else {
                System.err.println("❌ [TMDB API ERROR] HTTP " + status + " for operation [" + cacheKey + "]: " + e.getStatusText());
            }
            if (entry != null) {
                return entry.data;
            }
        } catch (ResourceAccessException e) {
            System.err.println("⚠️ [TMDB NETWORK ERROR] Network/connectivity failure for operation [" + cacheKey + "]: " + e.getMessage());
            if (entry != null) {
                return entry.data;
            }
        } catch (Exception e) {
            System.err.println("❌ [TMDB ERROR] TMDB call failed for [" + cacheKey + "]: " + e.getMessage());
            if (entry != null) {
                return entry.data;
            }
        }

        return fallbackJson;
    }

    /*
     * =========================
     * MOVIES
     * =========================
     */

    public String getTrendingMovies(int page) {
        return getFromTmdb("trending_" + page, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/trending/movie/day")
                                .queryParam("language", "en-US")
                                .queryParam("page", page)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getPopularMovies() {
        return getFromTmdb("popular_1", 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/popular")
                                .queryParam("language", "en-US")
                                .queryParam("page", 1)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getGenres() {
        return getFromTmdb("genres_list", 86400, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/genre/movie/list")
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_GENRES
        );
    }

    public String getMovieDetails(Long movieId) {
        return getFromTmdb("movie_details_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId)
                                .queryParam("language", "en-US")
                                .queryParam("append_to_response", "credits,videos,images,recommendations,similar,keywords,watch/providers,release_dates")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getMovieVideos(Long movieId) {
        return getFromTmdb("movie_videos_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/videos")
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_VIDEOS
        );
    }

    public String getMovies(
            int page,
            String sortBy,
            String year,
            String genre,
            String rating,
            String language
    ) {
        return getMovies(page, sortBy, year, genre, rating, language, null, null, null, null, null, null);
    }

    public String getMovies(
            int page,
            String sortBy,
            String year,
            String genre,
            String rating,
            String language,
            String fromDate,
            String toDate
    ) {
        return getMovies(page, sortBy, year, genre, rating, language, fromDate, toDate, null, null, null, null);
    }

    public String getMovies(
            int page,
            String sortBy,
            String year,
            String genre,
            String rating,
            String language,
            String fromDate,
            String toDate,
            String withWatchProviders,
            String watchRegion,
            Integer runtimeLte,
            Integer runtimeGte
    ) {
        String cacheKey = "movies_" + page + "_" + sortBy + "_" + year + "_" + genre + "_" + rating + "_" + language + "_" + fromDate + "_" + toDate + "_" + withWatchProviders + "_" + watchRegion + "_" + runtimeLte + "_" + runtimeGte;

        return getFromTmdb(cacheKey, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder.path("/discover/movie")
                                    .queryParam("language", "en-US")
                                    .queryParam("include_adult", false)
                                    .queryParam("page", page);

                            if (sortBy != null && !sortBy.isBlank()) {
                                uriBuilder.queryParam("sort_by", sortBy);
                            }
                            if (year != null && !year.isBlank()) {
                                uriBuilder.queryParam("primary_release_year", year);
                            }
                            if (genre != null && !genre.isBlank()) {
                                uriBuilder.queryParam("with_genres", genre);
                            }
                            if (rating != null && !rating.isBlank()) {
                                uriBuilder.queryParam("vote_average.gte", rating);
                            }
                            if (language != null && !language.isBlank()) {
                                uriBuilder.queryParam("with_original_language", language);
                            }
                            if (fromDate != null && !fromDate.isBlank()) {
                                uriBuilder.queryParam("primary_release_date.gte", fromDate);
                            }
                            if (toDate != null && !toDate.isBlank()) {
                                uriBuilder.queryParam("primary_release_date.lte", toDate);
                            }
                            if (withWatchProviders != null && !withWatchProviders.isBlank()) {
                                uriBuilder.queryParam("with_watch_providers", withWatchProviders);
                                uriBuilder.queryParam("watch_region", (watchRegion != null && !watchRegion.isBlank()) ? watchRegion : "IN");
                                uriBuilder.queryParam("with_watch_monetization_types", "flatrate|free|ads");
                            }
                            if (runtimeLte != null && runtimeLte > 0) {
                                uriBuilder.queryParam("with_runtime.lte", runtimeLte);
                            }
                            if (runtimeGte != null && runtimeGte > 0) {
                                uriBuilder.queryParam("with_runtime.gte", runtimeGte);
                            }
                            return uriBuilder.build();
                        })
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String searchMovies(String query, int page) {
        return getFromTmdb("search_" + query + "_" + page, 600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search/movie")
                                .queryParam("query", query)
                                .queryParam("language", "en-US")
                                .queryParam("include_adult", false)
                                .queryParam("page", page)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getMovieCredits(Long movieId) {
        return getFromTmdb("credits_" + movieId, 3600, () -> {
            try {
                return restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/credits")
                                .build())
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                return restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/credits")
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .body(String.class);
            }
        }, FALLBACK_CREDITS);
    }

    public String getMovieReviews(Long movieId) {
        return getFromTmdb("reviews_" + movieId, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/reviews")
                                .queryParam("language", "en-US")
                                .queryParam("page", 1)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getMovieRecommendations(Long movieId) {
        return getFromTmdb("recs_" + movieId, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/recommendations")
                                .queryParam("language", "en-US")
                                .queryParam("page", 1)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getSimilarMovies(Long movieId) {
        return getFromTmdb("similar_" + movieId, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/similar")
                                .queryParam("language", "en-US")
                                .queryParam("page", 1)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getMovieWatchProviders(Long movieId) {
        return getFromTmdb("providers_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/watch/providers")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getWatchProviders(String watchRegion) {
        String region = (watchRegion != null && !watchRegion.isBlank()) ? watchRegion.toUpperCase() : "IN";
        return getFromTmdb("providers_list_" + region, 86400, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/watch/providers/movie")
                                .queryParam("language", "en-US")
                                .queryParam("watch_region", region)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_PROVIDERS
        );
    }

    public String getMovieImages(Long movieId) {
        return getFromTmdb("movie_images_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/images")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getMovieKeywords(Long movieId) {
        return getFromTmdb("keywords_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/keywords")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getMovieReleaseDates(Long movieId) {
        return getFromTmdb("release_dates_" + movieId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/" + movieId + "/release_dates")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    /*
     * =========================
     * PEOPLE
     * =========================
     */

    public String getPopularPeople(int page) {
        return getFromTmdb("people_popular_" + page, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/person/popular")
                                .queryParam("language", "en-US")
                                .queryParam("page", page)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String searchPeople(String query, int page) {
        return getFromTmdb("people_search_" + query + "_" + page, 600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search/person")
                                .queryParam("query", query)
                                .queryParam("language", "en-US")
                                .queryParam("include_adult", false)
                                .queryParam("page", page)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }

    public String getPersonDetails(Long personId) {
        return getFromTmdb("person_" + personId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/person/" + personId)
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getPersonCredits(Long personId) {
        return getFromTmdb("person_credits_" + personId, 3600, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/person/" + personId + "/movie_credits")
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_CREDITS
        );
    }

    public String getPersonImages(Long personId) {
        return getFromTmdb("person_images_" + personId, 3600, () ->
                restClient.get()
                        .uri("/person/" + personId + "/images")
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public String getPersonExternalIds(Long personId) {
        return getFromTmdb("person_ext_ids_" + personId, 3600, () ->
                restClient.get()
                        .uri("/person/" + personId + "/external_ids")
                        .retrieve()
                        .body(String.class),
                FALLBACK_EMPTY_OBJ
        );
    }

    public List<Map<String, Object>> getLanguages() {
        if (!isConfigured) {
            return FALLBACK_LANGUAGES;
        }
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/configuration/languages")
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<
                            List<Map<String, Object>>
                            >() {});
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 401 || status == 403) {
                System.err.println("❌ [TMDB AUTH ERROR] HTTP " + status + " for operation [languages]: Configured TMDB credential is invalid or expired.");
            } else {
                System.err.println("❌ [TMDB API ERROR] HTTP " + status + " for operation [languages]: " + e.getStatusText());
            }
            return FALLBACK_LANGUAGES;
        } catch (ResourceAccessException e) {
            System.err.println("⚠️ [TMDB NETWORK ERROR] Network failure for operation [languages]: " + e.getMessage());
            return FALLBACK_LANGUAGES;
        } catch (Exception e) {
            System.err.println("TMDB getLanguages failed: " + e.getMessage());
            return FALLBACK_LANGUAGES;
        }
    }

    public String getTopMoviesByLanguage(String language) {
        return getFromTmdb("top_lang_" + language, 1800, () ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/discover/movie")
                                .queryParam("language", "en-US")
                                .queryParam("with_original_language", language)
                                .queryParam("sort_by", "popularity.desc")
                                .queryParam("include_adult", false)
                                .queryParam("page", 1)
                                .build())
                        .retrieve()
                        .body(String.class),
                FALLBACK_MOVIES
        );
    }
}