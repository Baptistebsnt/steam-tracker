package com.steamtracker.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Global stats across the user's entire Steam library")
public record GlobalStatsDto(

        @Schema(description = "Total number of synced games", example = "150")
        int totalGames,

        @Schema(description = "Total playtime across all games in minutes", example = "54000")
        long totalPlaytimeMinutes,

        @Schema(description = "Total number of achievements across all games", example = "3200")
        long totalAchievements,

        @Schema(description = "Total number of unlocked achievements", example = "800")
        long unlockedAchievements,

        @Schema(description = "Global achievement completion percentage", example = "25.0")
        double globalCompletionPercent,

        @Schema(description = "Most played game")
        GameDto mostPlayedGame
) {}