package com.steamtracker.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Steam game with playtime and achievement progress")
public record GameDto(

        @Schema(description = "Steam App ID", example = "570")
        Long appId,

        @Schema(description = "Game name", example = "Dota 2")
        String name,

        @Schema(description = "Total playtime in minutes", example = "3600")
        Long playtimeMinutes,

        @Schema(description = "Total number of achievements for this game", example = "100")
        long totalAchievements,

        @Schema(description = "Number of unlocked achievements", example = "42")
        long unlockedAchievements,

        @Schema(description = "Achievement completion percentage", example = "42.0")
        double completionPercent
) {}