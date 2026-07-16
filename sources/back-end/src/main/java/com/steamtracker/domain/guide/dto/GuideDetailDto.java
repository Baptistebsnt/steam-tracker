package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Full guide with steps and the reader's progress")
public record GuideDetailDto(

        @Schema(description = "Guide id", example = "1")
        Long id,

        @Schema(description = "Steam App ID of the game", example = "1145360")
        Long appId,

        @Schema(description = "Game name", example = "Hades")
        String gameName,

        @Schema(description = "Guide title", example = "100% Hades")
        String title,

        @Schema(description = "Guide description")
        String description,

        @Schema(description = "Author display name (Steam persona or email)", example = "PlayerOne")
        String authorName,

        @Schema(description = "Whether the current reader is the author", example = "false")
        boolean isAuthor,

        @Schema(description = "Total number of achievements linked across all steps", example = "49")
        int linkedAchievements,

        @Schema(description = "Number of those achievements the reader has unlocked", example = "12")
        int unlockedAchievements,

        @Schema(description = "Creation date")
        LocalDateTime createdAt,

        @Schema(description = "Last update date")
        LocalDateTime updatedAt,

        @Schema(description = "Ordered steps")
        List<GuideStepDto> steps
) {}
