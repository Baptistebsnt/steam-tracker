package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Achievement linked to a guide step, with the reader's unlock status")
public record GuideStepAchievementDto(

        @Schema(description = "Steam achievement identifier", example = "ACH_WIN_ONE_GAME")
        String apiName,

        @Schema(description = "Achievement display name", example = "First Win")
        String displayName,

        @Schema(description = "URL of the achievement icon")
        String iconUrl,

        @Schema(description = "Whether the reader has unlocked this achievement", example = "true")
        boolean unlocked
) {}
