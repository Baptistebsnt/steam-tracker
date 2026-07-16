package com.steamtracker.domain.guide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Payload to create or update a guide")
public record GuideRequest(

        @Schema(description = "Steam App ID of the game", example = "1145360")
        @NotNull(message = "appId obligatoire")
        Long appId,

        @Schema(description = "Game name", example = "Hades")
        @NotBlank(message = "Le nom du jeu est obligatoire")
        String gameName,

        @Schema(description = "Guide title", example = "100% Hades")
        @NotBlank(message = "Le titre est obligatoire")
        String title,

        @Schema(description = "Guide description")
        String description,

        @Schema(description = "Ordered steps (at least one)")
        @NotEmpty(message = "Le guide doit contenir au moins une étape")
        @Valid
        List<GuideStepInput> steps
) {}
