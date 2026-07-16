package com.steamtracker.steam;

import com.steamtracker.domain.user.UserRepository;
import com.steamtracker.error.ResourceNotFoundException;
import com.steamtracker.steam.dto.SteamGameDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
public class SyncService {

    private final SteamClient steamClient;
    private final UserRepository userRepository;
    private final SyncPersistenceService persistenceService;
    private final SyncStatusService statusService;
    private final int concurrency;

    public SyncService(SteamClient steamClient,
                       UserRepository userRepository,
                       SyncPersistenceService persistenceService,
                       SyncStatusService statusService,
                       @Value("${steam.sync.concurrency:8}") int concurrency) {
        this.steamClient = steamClient;
        this.userRepository = userRepository;
        this.persistenceService = persistenceService;
        this.statusService = statusService;
        this.concurrency = concurrency;
    }

    public SyncResult syncUser(String email) {
        statusService.markRunning(email);
        try {
            var result = doSync(email);
            statusService.markFinished(email, result);
            return result;
        } catch (RuntimeException e) {
            statusService.markFailed(email);
            throw e;
        }
    }

    private SyncResult doSync(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        var steamId = user.getSteamId();
        if (steamId == null) {
            return new SyncResult.Failed("Aucun SteamID lié au compte");
        }

        if (steamClient.getPlayerSummary(steamId) == null) {
            return new SyncResult.ProfilePrivate(steamId);
        }

        var steamGames = steamClient.getOwnedGames(steamId);
        if (steamGames.isEmpty()) {
            return new SyncResult.ProfilePrivate(steamId);
        }

        // Fetch phase: the achievement calls dominate the wall-clock time, so fan
        // them out across virtual threads, capped so we don't hammer the Steam API.
        var fetched = fetchGames(steamId, steamGames);

        // Persistence phase: single transaction, single thread.
        return persistenceService.persist(user.getId(), fetched);
    }

    private List<GameSyncData> fetchGames(String steamId, List<SteamGameDto> steamGames) {
        var limiter = new Semaphore(concurrency);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = steamGames.stream()
                    .map(game -> executor.submit(fetchGame(steamId, game, limiter)))
                    .toList();

            return futures.stream().map(this::await).toList();
        }
    }

    private Callable<GameSyncData> fetchGame(String steamId, SteamGameDto game, Semaphore limiter) {
        return () -> {
            limiter.acquire();
            try {
                var schema = steamClient.getAchievementSchema(game.appId());
                var achievements = steamClient.getPlayerAchievements(steamId, game.appId()).stream()
                        .map(a -> {
                            var meta = schema.get(a.apiName());
                            return new AchievementSyncData(a, meta != null ? meta.iconUrl() : null);
                        })
                        .toList();
                return new GameSyncData(game.appId(), game.name(), game.playtimeMinutes(), achievements);
            } finally {
                limiter.release();
            }
        };
    }

    private GameSyncData await(Future<GameSyncData> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sync interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Steam fetch failed", e.getCause());
        }
    }
}
