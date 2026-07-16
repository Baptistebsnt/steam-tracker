package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A single ordered step of a guide")
public record GuideStepDto(

        @Schema(description = "Step id", example = "12")
        Long id,

        @Schema(description = "Ordering position (0-based)", example = "0")
        int position,

        @Schema(description = "Step title", example = "Terminer le premier run")
        String title,

        @Schema(description = "Step content / instructions")
        String content,

        @Schema(description = "Achievements covered by this step")
        List<GuideStepAchievementDto> achievements
) {}
