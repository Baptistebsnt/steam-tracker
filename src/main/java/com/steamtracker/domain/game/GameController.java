package com.steamtracker.domain.game;

import com.steamtracker.domain.achievement.dto.AchievementDto;
import com.steamtracker.domain.game.dto.GameDto;
import com.steamtracker.domain.game.dto.GlobalStatsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> getGames(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGamesForUser(userDetails.getUsername()));
    }

    @GetMapping("/{appId}/achievements")
    public ResponseEntity<List<AchievementDto>> getAchievements(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appId) {
        return ResponseEntity.ok(gameService.getAchievementsForGame(userDetails.getUsername(), appId));
    }

    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsDto> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(gameService.getGlobalStats(userDetails.getUsername()));
    }
}