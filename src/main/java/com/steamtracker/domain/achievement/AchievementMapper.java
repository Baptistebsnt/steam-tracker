package com.steamtracker.domain.achievement;

import com.steamtracker.domain.achievement.dto.AchievementDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AchievementMapper {

    @Mapping(target = "unlocked", expression = "java(Boolean.TRUE.equals(achievement.getUnlocked()))")
    AchievementDto toDto(Achievement achievement);
}