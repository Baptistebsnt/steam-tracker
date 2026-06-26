package com.steamtracker.domain.game.dto;

public record GlobalStatsDto(
        int totalGames,
        long totalPlaytimeMinutes,
        long totalAchievements,
        long unlockedAchievements,
        double globalCompletionPercent,
        GameDto mostPlayedGame
) {}
