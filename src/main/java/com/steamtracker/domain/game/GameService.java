package com.steamtracker.domain.game;

import com.steamtracker.domain.achievement.AchievementMapper;
import com.steamtracker.domain.achievement.AchievementRepository;
import com.steamtracker.domain.achievement.dto.AchievementDto;
import com.steamtracker.domain.game.dto.GameDto;
import com.steamtracker.domain.game.dto.GlobalStatsDto;
import com.steamtracker.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final GameMapper gameMapper;
    private final AchievementMapper achievementMapper;

    public GameService(GameRepository gameRepository,
                       AchievementRepository achievementRepository,
                       UserRepository userRepository,
                       GameMapper gameMapper,
                       AchievementMapper achievementMapper) {
        this.gameRepository = gameRepository;
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.gameMapper = gameMapper;
        this.achievementMapper = achievementMapper;
    }

    public List<GameDto> getGamesForUser(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        return gameRepository.findByUserId(user.getId())
                .stream()
                .map(game -> {
                    long total = achievementRepository.countByGameIdAndUnlocked(game.getId(), false)
                            + achievementRepository.countByGameIdAndUnlocked(game.getId(), true);
                    long unlocked = achievementRepository.countByGameIdAndUnlocked(game.getId(), true);
                    double percent = total == 0 ? 0 : Math.round((unlocked * 100.0 / total) * 10.0) / 10.0;
                    return gameMapper.toDto(game, total, unlocked, percent);
                })
                .toList();
    }

    public List<AchievementDto> getAchievementsForGame(String email, Long appId) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        var game = gameRepository.findByUserIdAndAppId(user.getId(), appId)
                .orElseThrow(() -> new IllegalArgumentException("Jeu introuvable"));

        return achievementRepository.findByGameId(game.getId())
                .stream()
                .map(achievementMapper::toDto)
                .toList();
    }

    public GlobalStatsDto getGlobalStats(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        var games = gameRepository.findByUserId(user.getId());
        long totalPlaytime = games.stream().mapToLong(g -> g.getPlaytimeMinutes() != null ? g.getPlaytimeMinutes() : 0).sum();
        long totalAchievements = achievementRepository.countUnlockedByUserId(user.getId());

        long totalAll = games.stream()
                .mapToLong(g -> achievementRepository.countByGameIdAndUnlocked(g.getId(), false)
                        + achievementRepository.countByGameIdAndUnlocked(g.getId(), true))
                .sum();

        double globalPercent = totalAll == 0 ? 0 : Math.round((totalAchievements * 100.0 / totalAll) * 10.0) / 10.0;

        var mostPlayed = games.stream()
                .max(java.util.Comparator.comparingLong(g -> g.getPlaytimeMinutes() != null ? g.getPlaytimeMinutes() : 0))
                .map(g -> {
                    long total = achievementRepository.countByGameIdAndUnlocked(g.getId(), false)
                            + achievementRepository.countByGameIdAndUnlocked(g.getId(), true);
                    long unlocked = achievementRepository.countByGameIdAndUnlocked(g.getId(), true);
                    double percent = total == 0 ? 0 : Math.round((unlocked * 100.0 / total) * 10.0) / 10.0;
                    return gameMapper.toDto(g, total, unlocked, percent);
                })
                .orElse(null);

        return new GlobalStatsDto(
                games.size(),
                totalPlaytime,
                totalAll,
                totalAchievements,
                globalPercent,
                mostPlayed
        );
    }
}