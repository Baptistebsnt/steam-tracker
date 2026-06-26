package com.steamtracker.steam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamAchievementDto(
        @JsonProperty("apiname") String apiName,
        @JsonProperty("achieved") int achieved,
        @JsonProperty("unlocktime") Long unlockTime
) {}