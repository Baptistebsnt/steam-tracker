package com.steamtracker.steam;

public sealed interface SyncResult
        permits SyncResult.Success,
        SyncResult.ProfilePrivate,
        SyncResult.RateLimited,
        SyncResult.Failed {

    record Success(int gamesSynced, int achievementsSynced) implements SyncResult {}
    record ProfilePrivate(String steamId) implements SyncResult {}
    record RateLimited() implements SyncResult {}
    record Failed(String reason) implements SyncResult {}
}
