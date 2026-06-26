package com.steamtracker.domain.game;

import com.steamtracker.domain.achievement.dto.AchievementDto;
import com.steamtracker.domain.game.dto.GameDto;
import com.steamtracker.domain.game.dto.GlobalStatsDto;
import com.steamtracker.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
@Tag(name = "Games", description = "Browse your synced Steam library")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    @Operation(summary = "List all synced games")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Game list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<GameDto>> getGames(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGamesForUser(userDetails.getUsername()));
    }

    @GetMapping("/{appId}/achievements")
    @Operation(summary = "Achievements for a specific game")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Achievement list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AchievementDto.class)))),
            @ApiResponse(responseCode = "404", description = "Game not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<AchievementDto>> getAchievements(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Steam App ID") @PathVariable Long appId) {
        return ResponseEntity.ok(gameService.getAchievementsForGame(userDetails.getUsername(), appId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Global stats across the entire library")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Global stats",
                    content = @Content(schema = @Schema(implementation = GlobalStatsDto.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GlobalStatsDto> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGlobalStats(userDetails.getUsername()));
    }
}