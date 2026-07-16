package com.steamtracker.steam;

import com.steamtracker.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@Tag(name = "Sync", description = "Synchronize library and achievements from Steam")
public class SyncController {

    private final SyncService syncService;
    private final SyncStatusService syncStatusService;

    public SyncController(SyncService syncService, SyncStatusService syncStatusService) {
        this.syncService = syncService;
        this.syncStatusService = syncStatusService;
    }

    @GetMapping("/status")
    @Operation(summary = "Latest sync status for the authenticated user (for polling background syncs)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current sync status",
                    content = @Content(schema = @Schema(implementation = SyncStatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<SyncStatusResponse> status(@AuthenticationPrincipal UserDetails userDetails) {
        var state = syncStatusService.get(userDetails.getUsername());
        return ResponseEntity.ok(new SyncStatusResponse(state.name()));
    }

    @PostMapping
    @Operation(summary = "Sync games and achievements from the Steam API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync successful",
                    content = @Content(schema = @Schema(implementation = SyncResponse.class))),
            @ApiResponse(responseCode = "403", description = "Steam profile is private",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Steam API rate limit reached",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Sync failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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