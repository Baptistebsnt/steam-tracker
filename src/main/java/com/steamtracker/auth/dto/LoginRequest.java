package com.steamtracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginRequest(

        @Schema(description = "User email address", example = "user@example.com")
        @Email(message = "Email invalide")
        @NotBlank(message = "Email obligatoire")
        String email,

        @Schema(description = "Password", example = "MyP@ssw0rd")
        @NotBlank(message = "Mot de passe obligatoire")
        String password
) {}