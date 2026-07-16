package com.steamtracker.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Authenticated user's profile")
public record UserProfileDto(

        @Schema(description = "User email", example = "user@example.com")
        String email,

        @Schema(description = "Linked Steam ID (null if none)", example = "76561198000000000")
        String steamId,

        @Schema(description = "Account creation date")
        LocalDateTime createdAt
) {}
