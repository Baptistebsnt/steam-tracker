package com.steamtracker.steam.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A game returned by the Steam store search")
public record SteamGameSearchDto(

        @Schema(description = "Steam App ID", example = "1145360")
        Long appId,

        @Schema(description = "Game name", example = "Hades")
        String name,

        @Schema(description = "Thumbnail image URL")
        String imageUrl
) {}
