package com.steamtracker.domain.game.dto;

public record GameDto(
        Long appId,
        String name,
        Long playtimeMinutes,
        long totalAchievements,
        long unlockedAchievements,
        double completionPercent
) {}
