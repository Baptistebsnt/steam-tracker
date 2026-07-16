package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Guide summary for browse listings")
public record GuideSummaryDto(

        @Schema(description = "Guide id", example = "1")
        Long id,

        @Schema(description = "Steam App ID of the game", example = "1145360")
        Long appId,

        @Schema(description = "Game name", example = "Hades")
        String gameName,

        @Schema(description = "Guide title", example = "100% Hades")
        String title,

        @Schema(description = "Author display name (Steam persona or email)", example = "PlayerOne")
        String authorName,

        @Schema(description = "Number of steps", example = "8")
        int stepCount,

        @Schema(description = "Number of achievements covered", example = "49")
        int achievementCount,

        @Schema(description = "Creation date")
        LocalDateTime createdAt
) {}
