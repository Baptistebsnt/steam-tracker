package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "A step to create within a guide")
public record GuideStepInput(

        @Schema(description = "Step title", example = "Terminer le premier run")
        @NotBlank(message = "Le titre de l'étape est obligatoire")
        String title,

        @Schema(description = "Step content / instructions")
        String content,

        @Schema(description = "Achievements covered by this step")
        @Valid
        List<GuideAchievementInput> achievements
) {}
