package com.steamtracker.steam;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sync(
            @AuthenticationPrincipal UserDetails userDetails) {

        var result = syncService.syncUser(userDetails.getUsername());

        return switch (result) {
            case SyncResult.Success s -> ResponseEntity.ok(Map.of(
                    "status", "success",
                    "gamesSynced", s.gamesSynced(),
                    "achievementsSynced", s.achievementsSynced()
            ));
            case SyncResult.ProfilePrivate p -> ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "Profil Steam privé : " + p.steamId()
            ));
            case SyncResult.RateLimited r -> ResponseEntity.status(429).body(Map.of(
                    "status", "error",
                    "message", "Trop de requêtes Steam, réessaie dans quelques secondes"
            ));
            case SyncResult.Failed f -> ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", f.reason()
            ));
        };
    }
}