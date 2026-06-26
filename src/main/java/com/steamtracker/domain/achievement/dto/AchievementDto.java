package com.steamtracker.domain.achievement.dto;

import java.time.LocalDateTime;

public record AchievementDto(
        String apiName,
        String displayName,
        String description,
        String iconUrl,
        boolean unlocked,
        LocalDateTime unlockedAt
) {}
