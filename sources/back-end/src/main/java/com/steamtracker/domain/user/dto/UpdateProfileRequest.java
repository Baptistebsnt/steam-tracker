package com.steamtracker.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Profile update request")
public record UpdateProfileRequest(

        @Schema(description = "Steam ID (17-digit number), or empty to unlink", example = "76561198000000000")
        @Pattern(regexp = "^(\\d{17})?$", message = "Le Steam ID doit comporter 17 chiffres")
        String steamId
) {}
