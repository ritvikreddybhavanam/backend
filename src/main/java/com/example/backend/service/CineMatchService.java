package com.example.backend.service;

import com.example.backend.dto.CineMatchRoomResponse;
import com.example.backend.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CineMatchService {

    private final TmdbService tmdbService;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    // In-memory room manager for real-time multiplayer movie matching
    // roomCode -> RoomSession
    private final Map<String, RoomSession> rooms = new ConcurrentHashMap<>();

    public CineMatchService(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
        this.objectMapper = new ObjectMapper();
    }

    public static class RoomParticipant {
        public Long userId;
        public String name;
        public String avatar;
        public boolean isHost;
        public boolean ready;
        public Set<Long> likedMovieIds = ConcurrentHashMap.newKeySet();
        public Set<Long> passedMovieIds = ConcurrentHashMap.newKeySet();

        public RoomParticipant(Long userId, String name, String avatar, boolean isHost) {
            this.userId = userId;
            this.name = name;
            this.avatar = avatar;
            this.isHost = isHost;
            this.ready = true;
        }
    }

    public static class RoomSession {
        public String roomCode;
        public Long hostId;
        public String hostName;
        public String status = "WAITING"; // WAITING, SWIPING, MATCHED
        public String genreFilter = "";
        public String providerFilter = "";
        public Map<Long, RoomParticipant> participants = new ConcurrentHashMap<>();
        public List<Map<String, Object>> movieDeck = new ArrayList<>();
        public List<Map<String, Object>> matchedMovies = new ArrayList<>();
        public long createdAt = System.currentTimeMillis();
    }

    public CineMatchRoomResponse createRoom(User host, String genreFilter, String providerFilter) {
        String code = generateUniqueRoomCode();
        RoomSession room = new RoomSession();
        room.roomCode = code;
        room.hostId = host.getId();
        room.hostName = host.getFirstname() != null ? host.getFirstname() : "Host";
        room.genreFilter = genreFilter != null ? genreFilter : "";
        room.providerFilter = providerFilter != null ? providerFilter : "";

        RoomParticipant hostParticipant = new RoomParticipant(
                host.getId(),
                room.hostName,
                host.getAvatarUrl(),
                true
        );
        room.participants.put(host.getId(), hostParticipant);

        // Populate initial movie deck from TMDB
        room.movieDeck = fetchMovieDeck(genreFilter, providerFilter);
        rooms.put(code, room);

        return toDto(room, host.getId());
    }

    public CineMatchRoomResponse joinRoom(User user, String roomCode) {
        RoomSession room = findRoom(roomCode);
        if (room == null) {
            throw new RuntimeException("Room " + roomCode.toUpperCase().trim() + " not found or expired. Make sure the host has created the room.");
        }

        String name = user.getFirstname() != null ? user.getFirstname() : "Member";
        RoomParticipant participant = room.participants.computeIfAbsent(
                user.getId(),
                id -> new RoomParticipant(user.getId(), name, user.getAvatarUrl(), false)
        );
        participant.ready = true;

        return toDto(room, user.getId());
    }

    public CineMatchRoomResponse getRoomStatus(String roomCode, User user) {
        RoomSession room = findRoom(roomCode);
        if (room == null) {
            throw new RuntimeException("Room " + roomCode + " not found.");
        }
        return toDto(room, user.getId());
    }

    private RoomSession findRoom(String codeInput) {
        if (codeInput == null) return null;
        String raw = codeInput.trim().toUpperCase();
        if (rooms.containsKey(raw)) return rooms.get(raw);
        if (!raw.startsWith("CINE-") && rooms.containsKey("CINE-" + raw)) {
            return rooms.get("CINE-" + raw);
        }
        // Case-insensitive / whitespace fallback search
        for (Map.Entry<String, RoomSession> entry : rooms.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(raw) || entry.getKey().equalsIgnoreCase("CINE-" + raw)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public CineMatchRoomResponse startSwiping(String roomCode, User user) {
        RoomSession room = findRoom(roomCode);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        if (!room.hostId.equals(user.getId())) {
            throw new RuntimeException("Only the room host can start matching.");
        }

        room.status = "SWIPING";
        return toDto(room, user.getId());
    }

    public CineMatchRoomResponse recordSwipe(String roomCode, User user, Long movieId, boolean isLike) {
        RoomSession room = findRoom(roomCode);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        RoomParticipant p = room.participants.get(user.getId());
        if (p == null) {
            p = new RoomParticipant(user.getId(), user.getFirstname(), user.getAvatarUrl(), false);
            room.participants.put(user.getId(), p);
        }

        if (isLike) {
            p.likedMovieIds.add(movieId);
            p.passedMovieIds.remove(movieId);

            // Check if ALL participants liked this movie!
            if (room.participants.size() >= 2) {
                boolean allLiked = room.participants.values().stream()
                        .allMatch(participant -> participant.likedMovieIds.contains(movieId));

                if (allLiked) {
                    // Match found!
                    Optional<Map<String, Object>> movieOpt = room.movieDeck.stream()
                            .filter(m -> Objects.equals(m.get("id"), movieId) || Objects.equals(m.get("id"), String.valueOf(movieId)))
                            .findFirst();

                    boolean alreadyMatched = room.matchedMovies.stream()
                            .anyMatch(m -> Objects.equals(m.get("id"), movieId) || Objects.equals(m.get("id"), String.valueOf(movieId)));

                    if (movieOpt.isPresent() && !alreadyMatched) {
                        room.matchedMovies.add(movieOpt.get());
                        room.status = "MATCHED";
                    }
                }
            }
        } else {
            p.passedMovieIds.add(movieId);
            p.likedMovieIds.remove(movieId);
        }

        return toDto(room, user.getId());
    }

    public CineMatchRoomResponse resetRoom(String roomCode, User user) {
        RoomSession room = findRoom(roomCode);
        if (room == null) throw new RuntimeException("Room not found");

        for (RoomParticipant p : room.participants.values()) {
            p.likedMovieIds.clear();
            p.passedMovieIds.clear();
        }
        room.matchedMovies.clear();
        room.status = "SWIPING";
        return toDto(room, user.getId());
    }

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < 20; i++) {
            StringBuilder sb = new StringBuilder("CINE-");
            for (int j = 0; j < 3; j++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!rooms.containsKey(code)) {
                return code;
            }
        }
        return "CINE-" + (100 + random.nextInt(900));
    }

    private List<Map<String, Object>> fetchMovieDeck(String genreFilter, String providerFilter) {
        List<Map<String, Object>> deck = new ArrayList<>();
        try {
            String json;
            if (genreFilter != null && !genreFilter.isBlank()) {
                json = tmdbService.getMovies(1, "popularity.desc", null, genreFilter, null, null, null, null, providerFilter, "US", null, null);
            } else {
                json = tmdbService.getTrendingMovies(1);
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode n : results) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", n.path("id").asLong());
                    m.put("title", n.path("title").asText("Untitled"));
                    m.put("overview", n.path("overview").asText(""));
                    m.put("poster_path", n.path("poster_path").asText(null));
                    m.put("backdrop_path", n.path("backdrop_path").asText(null));
                    m.put("vote_average", n.path("vote_average").asDouble(0.0));
                    m.put("release_date", n.path("release_date").asText(""));
                    m.put("genre_ids", objectMapper.convertValue(n.path("genre_ids"), new TypeReference<List<Integer>>() {}));
                    
                    // Streaming mock/defaults
                    List<String> streamOn = new ArrayList<>();
                    if (m.get("id").hashCode() % 3 == 0) streamOn.add("Netflix");
                    if (m.get("id").hashCode() % 2 == 0) streamOn.add("Prime Video");
                    if (streamOn.isEmpty()) streamOn.add("Apple TV / Cinema");
                    m.put("streamingProviders", streamOn);

                    deck.add(m);
                }
            }
        } catch (Exception e) {
            // Fallback mock deck if TMDB call fails
            deck.addAll(createFallbackDeck());
        }

        if (deck.isEmpty()) {
            deck.addAll(createFallbackDeck());
        }

        return deck;
    }

    private List<Map<String, Object>> createFallbackDeck() {
        return List.of(
                Map.of("id", 157336L, "title", "Interstellar", "overview", "The adventures of a group of explorers who make use of a newly discovered wormhole.", "poster_path", "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "vote_average", 8.4, "release_date", "2014-11-05", "streamingProviders", List.of("Prime Video", "Apple TV")),
                Map.of("id", 872585L, "title", "Oppenheimer", "overview", "The story of J. Robert Oppenheimer’s role in the development of the atomic bomb.", "poster_path", "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", "vote_average", 8.1, "release_date", "2023-07-19", "streamingProviders", List.of("JioCinema", "Prime Video")),
                Map.of("id", 579974L, "title", "RRR", "overview", "A fictional story about two legendary revolutionaries and their journey away from home.", "poster_path", "/wE0noFUqsvP2ElDV6fZOp7MuOpm.jpg", "vote_average", 7.8, "release_date", "2022-03-24", "streamingProviders", List.of("Netflix", "ZEE5")),
                Map.of("id", 693134L, "title", "Dune: Part Two", "overview", "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen.", "poster_path", "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", "vote_average", 8.2, "release_date", "2024-02-27", "streamingProviders", List.of("JioCinema", "Apple TV")),
                Map.of("id", 496243L, "title", "Parasite", "overview", "All unemployed, Ki-taek's family takes peculiar interest in the wealthy Parks.", "poster_path", "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg", "vote_average", 8.5, "release_date", "2019-05-30", "streamingProviders", List.of("SonyLIV", "Prime Video"))
        );
    }

    private CineMatchRoomResponse toDto(RoomSession room, Long currentUserId) {
        CineMatchRoomResponse dto = new CineMatchRoomResponse();
        dto.setRoomCode(room.roomCode);
        dto.setHostId(room.hostId);
        dto.setHostName(room.hostName);
        dto.setStatus(room.status);
        dto.setGenreFilter(room.genreFilter);
        dto.setProviderFilter(room.providerFilter);
        dto.setTotalParticipants(room.participants.size());

        List<Map<String, Object>> participantList = room.participants.values().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", p.userId);
            map.put("name", p.name);
            map.put("avatar", p.avatar);
            map.put("isHost", p.isHost);
            map.put("ready", p.ready);
            map.put("swipedCount", p.likedMovieIds.size() + p.passedMovieIds.size());
            return map;
        }).collect(Collectors.toList());
        dto.setParticipants(participantList);

        // Filter unswiped movies for the requesting user
        RoomParticipant currentParticipant = room.participants.get(currentUserId);
        Set<Long> alreadySwiped = currentParticipant != null
                ? new HashSet<>() {{
                    addAll(currentParticipant.likedMovieIds);
                    addAll(currentParticipant.passedMovieIds);
                }}
                : Collections.emptySet();

        List<Map<String, Object>> unswipedDeck = room.movieDeck.stream()
                .filter(m -> {
                    Object idObj = m.get("id");
                    if (idObj == null) return false;
                    long idVal = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
                    return !alreadySwiped.contains(idVal);
                })
                .collect(Collectors.toList());

        dto.setMovies(unswipedDeck);
        dto.setAllMatches(room.matchedMovies);
        dto.setMatchedMovie(room.matchedMovies.isEmpty() ? null : room.matchedMovies.get(room.matchedMovies.size() - 1));

        return dto;
    }
}
