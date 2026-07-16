package com.steamtracker.auth;

import com.steamtracker.auth.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/auth/steam")
@Tag(name = "Steam Authentication", description = "Sign in through Steam (OpenID 2.0)")
@SecurityRequirements
public class SteamAuthController {

    private final SteamOpenIdService steamOpenIdService;
    private final String frontendUrl;

    public SteamAuthController(SteamOpenIdService steamOpenIdService,
                              @Value("${app.frontend-url}") String frontendUrl) {
        this.steamOpenIdService = steamOpenIdService;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/login")
    @Operation(summary = "Redirect the browser to Steam to start the OpenID login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(steamOpenIdService.buildAuthUrl()))
                .build();
    }

    @GetMapping("/callback")
    @Operation(summary = "OpenID return endpoint — verifies the assertion and redirects to the front-end")
    public ResponseEntity<Void> callback(@RequestParam Map<String, String> params) {
        var location = steamOpenIdService.verify(params)
                .map(steamOpenIdService::authenticate)
                .map(this::successRedirect)
                .orElseGet(this::failureRedirect);

        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    private URI successRedirect(AuthResponse auth) {
        var builder = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/steam/callback")
                .queryParam("token", encode(auth.token()))
                .queryParam("email", encode(auth.email()))
                .queryParam("steamId", encode(auth.steamId()))
                .queryParam("displayName", encode(auth.displayName()));
        if (auth.avatarUrl() != null) {
            builder.queryParam("avatarUrl", encode(auth.avatarUrl()));
        }
        return builder.build(true).toUri();
    }

    private URI failureRedirect() {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/steam/callback")
                .queryParam("error", "steam_auth_failed")
                .build()
                .toUri();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
