package com.steamtracker.steam;

import com.steamtracker.domain.achievement.Achievement;
import com.steamtracker.domain.achievement.AchievementRepository;
import com.steamtracker.domain.game.Game;
import com.steamtracker.domain.game.GameRepository;
import com.steamtracker.domain.user.User;
import com.steamtracker.domain.user.UserRepository;
import com.steamtracker.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Writes the fetched Steam data to the database in a single transaction on one
 * thread — the JPA persistence context is not thread-safe, so the concurrency
 * lives in the fetch phase ({@link SyncService}), never here.
 */
@Service
public class SyncPersistenceService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final AchievementRepository achievementRepository;

    public SyncPersistenceService(UserRepository userRepository,
                                  GameRepository gameRepository,
                                  AchievementRepository achievementRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.achievementRepository = achievementRepository;
    }

    @Transactional
    public SyncResult.Success persist(Long userId, List<GameSyncData> games) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        int gamesSynced = 0;
        int achievementsSynced = 0;

        for (var gameData : games) {
            var game = syncGame(user, gameData.appId(), gameData.name(), gameData.playtimeMinutes());
            gamesSynced++;

            for (var achievement : gameData.achievements()) {
                syncAchievement(game, achievement);
                achievementsSynced++;
            }
        }

        return new SyncResult.Success(gamesSynced, achievementsSynced);
    }

    private Game syncGame(User user, Long appId, String name, Long playtimeMinutes) {
        return gameRepository.findByUserIdAndAppId(user.getId(), appId)
                .map(existing -> {
                    existing.setPlaytimeMinutes(playtimeMinutes);
                    existing.setLastSyncedAt(LocalDateTime.now());
                    return gameRepository.save(existing);
                })
                .orElseGet(() -> {
                    var game = new Game();
                    game.setUser(user);
                    game.setAppId(appId);
                    game.setName(name);
                    game.setPlaytimeMinutes(playtimeMinutes);
                    game.setLastSyncedAt(LocalDateTime.now());
                    return gameRepository.save(game);
                });
    }

    private void syncAchievement(Game game, AchievementSyncData data) {
        var dto = data.dto();
        var achievement = achievementRepository.findByGameIdAndApiName(game.getId(), dto.apiName())
                .orElseGet(() -> {
                    var a = new Achievement();
                    a.setGame(game);
                    a.setApiName(dto.apiName());
                    return a;
                });

        achievement.setDisplayName(dto.displayName());
        achievement.setDescription(dto.description());
        if (data.iconUrl() != null) achievement.setIconUrl(data.iconUrl());
        achievement.setUnlocked(dto.achieved() == 1);
        if (dto.achieved() == 1 && dto.unlockTime() != null && dto.unlockTime() > 0) {
            achievement.setUnlockedAt(
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(dto.unlockTime()), ZoneId.systemDefault())
            );
        }
        achievementRepository.save(achievement);
    }
}
