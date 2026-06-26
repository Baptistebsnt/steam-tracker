package com.steamtracker.steam;

import com.steamtracker.domain.achievement.Achievement;
import com.steamtracker.domain.achievement.AchievementRepository;
import com.steamtracker.domain.game.Game;
import com.steamtracker.domain.game.GameRepository;
import com.steamtracker.domain.user.User;
import com.steamtracker.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SyncService {

    private final SteamClient steamClient;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final AchievementRepository achievementRepository;

    public SyncService(SteamClient steamClient,
                       UserRepository userRepository,
                       GameRepository gameRepository,
                       AchievementRepository achievementRepository) {
        this.steamClient = steamClient;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.achievementRepository = achievementRepository;
    }

    @Transactional
    public SyncResult syncUser(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (user.getSteamId() == null) {
            return new SyncResult.Failed("Aucun SteamID lié au compte");
        }

        var profile = steamClient.getPlayerSummary(user.getSteamId());
        if (profile == null) {
            return new SyncResult.ProfilePrivate(user.getSteamId());
        }

        var steamGames = steamClient.getOwnedGames(user.getSteamId());
        if (steamGames.isEmpty()) {
            return new SyncResult.ProfilePrivate(user.getSteamId());
        }

        var gamesSynced = new AtomicInteger(0);
        var achievementsSynced = new AtomicInteger(0);

        for (var steamGame : steamGames) {
            var game = syncGame(user, steamGame.appId(), steamGame.name(), steamGame.playtimeMinutes());
            gamesSynced.incrementAndGet();

            var steamAchievements = steamClient.getPlayerAchievements(user.getSteamId(), steamGame.appId());
            for (var steamAchievement : steamAchievements) {
                syncAchievement(game, steamAchievement.apiName(), steamAchievement.achieved(), steamAchievement.unlockTime());
                achievementsSynced.incrementAndGet();
            }
        }

        return new SyncResult.Success(gamesSynced.get(), achievementsSynced.get());
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

    private void syncAchievement(Game game, String apiName, int achieved, Long unlockTime) {
        var existing = achievementRepository.findByGameIdAndApiName(game.getId(), apiName);

        if (existing.isPresent()) {
            var achievement = existing.get();
            achievement.setUnlocked(achieved == 1);
            if (achieved == 1 && unlockTime != null && unlockTime > 0) {
                achievement.setUnlockedAt(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(unlockTime), ZoneId.systemDefault())
                );
            }
            achievementRepository.save(achievement);
        } else {
            var achievement = new Achievement();
            achievement.setGame(game);
            achievement.setApiName(apiName);
            achievement.setUnlocked(achieved == 1);
            if (achieved == 1 && unlockTime != null && unlockTime > 0) {
                achievement.setUnlockedAt(
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(unlockTime), ZoneId.systemDefault())
                );
            }
            achievementRepository.save(achievement);
        }
    }
}