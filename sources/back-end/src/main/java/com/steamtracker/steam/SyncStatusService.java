package com.steamtracker.steam;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tracks the outcome of the most recent sync per user so the front-end can poll
 * for progress — chiefly the background sync triggered on first Steam login.
 * In-memory and best-effort: state is ephemeral and not shared across instances.
 */
@Service
public class SyncStatusService {

    public enum State { IDLE, RUNNING, DONE, PRIVATE, RATE_LIMITED, FAILED }

    private final Map<String, State> byUser = new ConcurrentHashMap<>();

    public void markRunning(String email) {
        byUser.put(email, State.RUNNING);
    }

    public void markFinished(String email, SyncResult result) {
        byUser.put(email, switch (result) {
            case SyncResult.Success ignored -> State.DONE;
            case SyncResult.ProfilePrivate ignored -> State.PRIVATE;
            case SyncResult.RateLimited ignored -> State.RATE_LIMITED;
            case SyncResult.Failed ignored -> State.FAILED;
        });
    }

    public void markFailed(String email) {
        byUser.put(email, State.FAILED);
    }

    public State get(String email) {
        return byUser.getOrDefault(email, State.IDLE);
    }
}
