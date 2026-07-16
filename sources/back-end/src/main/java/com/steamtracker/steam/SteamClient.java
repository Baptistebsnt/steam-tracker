package com.steamtracker.steam;

import com.steamtracker.steam.dto.SteamAchievementDto;
import com.steamtracker.steam.dto.SteamAchievementSchemaDto;
import com.steamtracker.steam.dto.SteamGameDto;
import com.steamtracker.steam.dto.SteamGameSearchDto;
import com.steamtracker.steam.dto.SteamPlayerSummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SteamClient {

    private final WebClient webClient;
    private final String apiKey;

    public SteamClient(WebClient steamWebClient,
                       @Value("${steam.api.key}") String apiKey) {
        this.webClient = steamWebClient;
        this.apiKey = apiKey;
    }

    public List<SteamGameDto> getOwnedGames(String steamId) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/IPlayerService/GetOwnedGames/v1/")
                            .queryParam("key", apiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Collections.emptyList();

            var responseBody = (Map<?, ?>) response.get("response");
            if (responseBody == null || !responseBody.containsKey("games")) {
                return Collections.emptyList();
            }

            var games = (List<Map<String, Object>>) responseBody.get("games");
            return games.stream()
                    .map(g -> new SteamGameDto(
                            ((Number) g.get("appid")).longValue(),
                            (String) g.getOrDefault("name", "Unknown"),
                            ((Number) g.getOrDefault("playtime_forever", 0)).longValue()
                    ))
                    .toList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 403 = jeu sans achievements ou stats privées → résultat attendu, on retourne liste vide
    // l=english pour récupérer name (displayName) et description directement
    public List<SteamAchievementDto> getPlayerAchievements(String steamId, Long appId) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ISteamUserStats/GetPlayerAchievements/v1/")
                            .queryParam("key", apiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("appid", appId)
                            .queryParam("l", "english")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 403, ignored -> Mono.empty())
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Collections.emptyList();

            var playerStats = (Map<?, ?>) response.get("playerstats");
            if (playerStats == null || !playerStats.containsKey("achievements")) {
                return Collections.emptyList();
            }

            var achievements = (List<Map<String, Object>>) playerStats.get("achievements");
            return achievements.stream()
                    .map(a -> new SteamAchievementDto(
                            (String) a.get("apiname"),
                            ((Number) a.getOrDefault("achieved", 0)).intValue(),
                            ((Number) a.getOrDefault("unlocktime", 0)).longValue(),
                            (String) a.getOrDefault("name", ""),
                            (String) a.getOrDefault("description", "")
                    ))
                    .toList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 403 = jeu sans schema de stats → résultat attendu, on retourne map vide
    public Map<String, SteamAchievementSchemaDto> getAchievementSchema(Long appId) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ISteamUserStats/GetSchemaForGame/v2/")
                            .queryParam("key", apiKey)
                            .queryParam("appid", appId)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 403, ignored -> Mono.empty())
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Collections.emptyMap();

            var game = (Map<?, ?>) response.get("game");
            if (game == null) return Collections.emptyMap();

            var availableStats = (Map<?, ?>) game.get("availableGameStats");
            if (availableStats == null || !availableStats.containsKey("achievements")) {
                return Collections.emptyMap();
            }

            var achievements = (List<Map<String, Object>>) availableStats.get("achievements");
            return achievements.stream()
                    .collect(Collectors.toMap(
                            a -> (String) a.get("name"),
                            a -> new SteamAchievementSchemaDto(
                                    (String) a.get("name"),
                                    (String) a.getOrDefault("displayName", ""),
                                    (String) a.getOrDefault("description", ""),
                                    (String) a.getOrDefault("icon", "")
                            )
                    ));

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    // Recherche de jeux via le store Steam (endpoint public, sans clé, sur un host différent
    // de la base-url configurée → on passe une URI absolue qui l'override).
    public List<SteamGameSearchDto> searchGames(String term) {
        try {
            var uri = URI.create("https://store.steampowered.com/api/storesearch/?term="
                    + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&l=english&cc=US");

            var response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Collections.emptyList();

            var items = (List<Map<String, Object>>) response.get("items");
            if (items == null) return Collections.emptyList();

            return items.stream()
                    .map(i -> new SteamGameSearchDto(
                            ((Number) i.get("id")).longValue(),
                            (String) i.getOrDefault("name", "Unknown"),
                            (String) i.getOrDefault("tiny_image", "")
                    ))
                    .toList();

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public SteamPlayerSummaryDto getPlayerSummary(String steamId) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ISteamUser/GetPlayerSummaries/v2/")
                            .queryParam("key", apiKey)
                            .queryParam("steamids", steamId)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return null;

            var responseBody = (Map<?, ?>) response.get("response");
            var players = (List<Map<String, Object>>) responseBody.get("players");

            if (players == null || players.isEmpty()) return null;

            var player = players.getFirst();
            return new SteamPlayerSummaryDto(
                    (String) player.get("steamid"),
                    (String) player.get("personaname"),
                    (String) player.get("avatarfull")
            );

        } catch (Exception e) {
            return null;
        }
    }
}