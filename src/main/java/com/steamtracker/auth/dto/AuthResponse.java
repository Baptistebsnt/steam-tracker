package com.steamtracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing the JWT token")
public record AuthResponse(

        @Schema(description = "JWT token to use as Bearer in the Authorization header")
        String token,

        @Schema(description = "Authenticated user email", example = "user@example.com")
        String email,

        @Schema(description = "Linked Steam ID", example = "76561198000000000")
        String steamId
) {}