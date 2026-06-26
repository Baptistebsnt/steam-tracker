package com.steamtracker.steam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamPlayerSummaryDto(
        @JsonProperty("steamid") String steamId,
        @JsonProperty("personaname") String personaName,
        @JsonProperty("avatarfull") String avatarUrl
) {}
