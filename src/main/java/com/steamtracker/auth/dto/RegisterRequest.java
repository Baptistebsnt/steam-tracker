package com.steamtracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    String email,

    @NotBlank(message = "Mot de passe obligatoire")
    @Size(min = 8, message = "Minimum 8 caractères")
    String password,

    String steamId
){}
