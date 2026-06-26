package com.steamtracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request")
public record RegisterRequest(

        @Schema(description = "User email address", example = "user@example.com")
        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @Schema(description = "Password (minimum 8 characters)", example = "MyP@ssw0rd")
        @NotBlank(message = "Mot de passe obligatoire")
        @Size(min = 8, message = "Minimum 8 caractères")
        String password,

        @Schema(description = "Steam ID (17-digit number visible on your profile)", example = "76561198000000000")
        String steamId
) {}