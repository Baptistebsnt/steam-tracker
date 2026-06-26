package com.steamtracker.steam;

import com.steamtracker.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public ResponseEntity<?> sync(@AuthenticationPrincipal UserDetails userDetails) {
        var result = syncService.syncUser(userDetails.getUsername());

        return switch (result) {
            case SyncResult.Success s ->
                    ResponseEntity.ok(new SyncResponse(s.gamesSynced(), s.achievementsSynced()));
            case SyncResult.ProfilePrivate p ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiError.of(HttpStatus.FORBIDDEN.value(), "Profil Steam privé : " + p.steamId()));
            case SyncResult.RateLimited r ->
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiError.of(HttpStatus.TOO_MANY_REQUESTS.value(), "Trop de requêtes Steam, réessaie dans quelques secondes"));
            case SyncResult.Failed f ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), f.reason()));
        };
    }
}