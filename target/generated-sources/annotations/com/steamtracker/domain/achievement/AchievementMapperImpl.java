package com.steamtracker.domain.achievement;

import com.steamtracker.domain.achievement.dto.AchievementDto;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T11:09:13+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class AchievementMapperImpl implements AchievementMapper {

    @Override
    public AchievementDto toDto(Achievement achievement) {
        if ( achievement == null ) {
            return null;
        }

        String apiName = null;
        String displayName = null;
        String description = null;
        String iconUrl = null;
        LocalDateTime unlockedAt = null;

        apiName = achievement.getApiName();
        displayName = achievement.getDisplayName();
        description = achievement.getDescription();
        iconUrl = achievement.getIconUrl();
        unlockedAt = achievement.getUnlockedAt();

        boolean unlocked = Boolean.TRUE.equals(achievement.getUnlocked());

        AchievementDto achievementDto = new AchievementDto( apiName, displayName, description, iconUrl, unlocked, unlockedAt );

        return achievementDto;
    }
}
