package com.steamtracker.steam.dto;

public record SteamAchievementSchemaDto(
        String apiName,
        String displayName,
        String description,
        String iconUrl
) {}