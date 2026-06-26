package com.steamtracker.domain.game;

import com.steamtracker.domain.game.dto.GameDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T11:09:13+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class GameMapperImpl implements GameMapper {

    @Override
    public GameDto toDto(Game game, long totalAchievements, long unlockedAchievements, double completionPercent) {
        if ( game == null ) {
            return null;
        }

        Long appId = null;
        String name = null;
        Long playtimeMinutes = null;
        if ( game != null ) {
            appId = game.getAppId();
            name = game.getName();
            playtimeMinutes = game.getPlaytimeMinutes();
        }
        long totalAchievements1 = 0L;
        totalAchievements1 = totalAchievements;
        long unlockedAchievements1 = 0L;
        unlockedAchievements1 = unlockedAchievements;
        double completionPercent1 = 0.0d;
        completionPercent1 = completionPercent;

        GameDto gameDto = new GameDto( appId, name, playtimeMinutes, totalAchievements1, unlockedAchievements1, completionPercent1 );

        return gameDto;
    }
}
