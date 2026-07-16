package com.steamtracker.steam;

import com.steamtracker.error.ApiError;
import com.steamtracker.steam.dto.SteamAchievementSchemaDto;
import com.steamtracker.steam.dto.SteamGameSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/steam")
@Tag(name = "Steam Catalog", description = "Search Steam games and achievement schemas (for authoring guides)")
public class SteamCatalogController {

    private final SteamClient steamClient;

    public SteamCatalogController(SteamClient steamClient) {
        this.steamClient = steamClient;
    }

    @GetMapping("/games/search")
    @Operation(summary = "Search Steam games by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching games",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SteamGameSearchDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<SteamGameSearchDto>> searchGames(
            @Parameter(description = "Search term") @RequestParam("q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(steamClient.searchGames(query.trim()));
    }

    @GetMapping("/games/{appId}/achievements")
    @Operation(summary = "Full achievement schema for a game")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Achievement schema",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SteamAchievementSchemaDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<SteamAchievementSchemaDto>> achievements(
            @Parameter(description = "Steam App ID") @PathVariable Long appId) {
        return ResponseEntity.ok(steamClient.getAchievementSchema(appId).values().stream().toList());
    }
}
