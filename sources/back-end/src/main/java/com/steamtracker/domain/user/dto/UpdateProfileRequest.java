package com.steamtracker.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Profile update request. Fields left null are unchanged; send an empty string to clear one.")
public record UpdateProfileRequest(

        @Schema(description = "Steam ID (17-digit number), or empty to unlink", example = "76561198000000000")
        @Pattern(regexp = "^(\\d{17})?$", message = "Le Steam ID doit comporter 17 chiffres")
        String steamId,

        @Schema(description = "Display name (2-30 characters), or empty to clear", example = "PlayerOne")
        @Pattern(regexp = "^(.{2,30})?$", message = "Le pseudo doit comporter entre 2 et 30 caractères")
        @Size(max = 30)
        String username
) {}
