package com.steamtracker.domain.guide;

import com.steamtracker.domain.guide.dto.GuideDetailDto;
import com.steamtracker.domain.guide.dto.GuideRequest;
import com.steamtracker.domain.guide.dto.GuideSummaryDto;
import com.steamtracker.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guides")
@Tag(name = "Guides", description = "Community 100% guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @GetMapping
    @Operation(summary = "Browse guides (public), optionally filtered by game")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guide list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GuideSummaryDto.class))))
    })
    public ResponseEntity<List<GuideSummaryDto>> list(
            @Parameter(description = "Filter by Steam App ID") @RequestParam(required = false) Long appId) {
        return ResponseEntity.ok(guideService.list(appId));
    }

    @GetMapping("/mine")
    @Operation(summary = "List guides authored by the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guide list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GuideSummaryDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<GuideSummaryDto>> mine(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(guideService.listMine(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a guide with the reader's progress (public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guide detail",
                    content = @Content(schema = @Schema(implementation = GuideDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GuideDetailDto> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(guideService.getDetail(id, usernameOrNull(userDetails)));
    }

    @PostMapping
    @Operation(summary = "Create a guide")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Guide created",
                    content = @Content(schema = @Schema(implementation = GuideDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GuideDetailDto> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GuideRequest request) {
        var created = guideService.create(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a guide (author only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Guide updated",
                    content = @Content(schema = @Schema(implementation = GuideDetailDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Not the guide author",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GuideDetailDto> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody GuideRequest request) {
        return ResponseEntity.ok(guideService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a guide (author only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Guide deleted"),
            @ApiResponse(responseCode = "403", description = "Not the guide author",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        guideService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    private static String usernameOrNull(UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : null;
    }
}
