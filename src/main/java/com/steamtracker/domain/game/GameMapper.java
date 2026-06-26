package com.steamtracker.domain.game;

import com.steamtracker.domain.game.dto.GameDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(source = "game.appId", target = "appId")
    @Mapping(source = "game.name", target = "name")
    @Mapping(source = "game.playtimeMinutes", target = "playtimeMinutes")
    GameDto toDto(Game game, long totalAchievements, long unlockedAchievements, double completionPercent);
}