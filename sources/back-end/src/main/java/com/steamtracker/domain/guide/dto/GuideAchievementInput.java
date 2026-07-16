package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "An achievement to link to a guide step")
public record GuideAchievementInput(

        @Schema(description = "Steam achievement identifier", example = "ACH_WIN_ONE_GAME")
        @NotBlank(message = "apiName obligatoire")
        String apiName,

        @Schema(description = "Achievement display name", example = "First Win")
        String displayName,

        @Schema(description = "URL of the achievement icon")
        String iconUrl
) {}
