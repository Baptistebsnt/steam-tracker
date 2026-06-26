package com.steamtracker.steam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamGameDto (
    @JsonProperty("appId") Long appId,
    @JsonProperty("name") String name,
    @JsonProperty("playtime_forever") Long playtimeMinutes
){}
