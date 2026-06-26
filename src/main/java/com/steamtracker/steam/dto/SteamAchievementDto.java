package com.steamtracker.steam.dto;

public record SteamAchievementDto(
        String apiName,
        int achieved,
        Long unlockTime,
        String displayName,
        String description
) {}