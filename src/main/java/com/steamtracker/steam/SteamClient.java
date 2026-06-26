package com.steamtracker.steam;

import com.steamtracker.steam.dto.SteamAchievementDto;
import com.steamtracker.steam.dto.SteamGameDto;
import com.steamtracker.steam.dto.SteamPlayerSummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public List<SteamAchievementDto> getPlayerAchievements(String steamId, Long appId) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ISteamUserStats/GetPlayerAchievements/v1/")
                            .queryParam("key", apiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("appid", appId)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
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
                            ((Number) a.getOrDefault("unlocktime", 0)).longValue()
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

            var player = players.get(0);
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
