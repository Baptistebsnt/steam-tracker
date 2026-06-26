package com.steamtracker.domain.achievement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Steam achievement with unlock status")
public record AchievementDto(

        @Schema(description = "Internal Steam achievement identifier", example = "ACH_WIN_ONE_GAME")
        String apiName,

        @Schema(description = "Display name shown in Steam", example = "First Win")
        String displayName,

        @Schema(description = "Achievement description", example = "Win your first game")
        String description,

        @Schema(description = "URL of the achievement icon")
        String iconUrl,

        @Schema(description = "Whether the achievement has been unlocked", example = "true")
        boolean unlocked,

        @Schema(description = "Unlock date and time (null if not unlocked)")
        LocalDateTime unlockedAt
) {}