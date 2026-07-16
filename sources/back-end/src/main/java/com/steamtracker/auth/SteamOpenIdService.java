package com.steamtracker.auth;

import com.steamtracker.auth.dto.AuthResponse;
import com.steamtracker.domain.user.User;
import com.steamtracker.domain.user.UserRepository;
import com.steamtracker.steam.SteamClient;
import com.steamtracker.steam.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SteamOpenIdService {

    private static final String STEAM_OPENID_ENDPOINT = "https://steamcommunity.com/openid/login";
    private static final String OPENID_NS = "http://specs.openid.net/auth/2.0";
    private static final String IDENTIFIER_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    private static final Pattern CLAIMED_ID_PATTERN =
            Pattern.compile("^https?://steamcommunity\\.com/openid/id/(\\d+)$");

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SteamClient steamClient;
    private final SyncService syncService;
    private final String backendUrl;

    public SteamOpenIdService(WebClient steamWebClient,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              JwtService jwtService,
                              SteamClient steamClient,
                              SyncService syncService,
                              @Value("${app.backend-url}") String backendUrl) {
        this.webClient = steamWebClient;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.steamClient = steamClient;
        this.syncService = syncService;
        this.backendUrl = backendUrl;
    }

    /** URL to redirect the browser to so the user can authenticate on Steam. */
    public String buildAuthUrl() {
        var returnTo = backendUrl + "/auth/steam/callback";
        var realm = backendUrl + "/";
        return STEAM_OPENID_ENDPOINT
                + "?openid.ns=" + encode(OPENID_NS)
                + "&openid.mode=checkid_setup"
                + "&openid.return_to=" + encode(returnTo)
                + "&openid.realm=" + encode(realm)
                + "&openid.identity=" + encode(IDENTIFIER_SELECT)
                + "&openid.claimed_id=" + encode(IDENTIFIER_SELECT);
    }

    /**
     * Verifies the OpenID assertion returned by Steam and, if valid, resolves the
     * SteamID from the claimed_id. Returns empty when the assertion is invalid.
     */
    public Optional<String> verify(Map<String, String> params) {
        if (!"id_res".equals(params.get("openid.mode"))) {
            return Optional.empty();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        params.forEach(form::add);
        form.set("openid.mode", "check_authentication");

        String body;
        try {
            body = webClient.post()
                    .uri(URI.create(STEAM_OPENID_ENDPOINT))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.warn("Steam OpenID verification call failed: {}", e.getMessage());
            return Optional.empty();
        }

        if (body == null || !body.contains("is_valid:true")) {
            log.warn("Steam OpenID assertion rejected");
            return Optional.empty();
        }

        var claimedId = params.get("openid.claimed_id");
        if (claimedId == null) {
            return Optional.empty();
        }
        Matcher matcher = CLAIMED_ID_PATTERN.matcher(claimedId);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /** Finds the account linked to this SteamID, creating one on first login. */
    public AuthResponse authenticate(String steamId) {
        var existing = userRepository.findBySteamId(steamId);
        var user = existing.orElseGet(() -> createSteamUser(steamId));

        // First Steam login: pull the game library in the background so the
        // dashboard is populated without blocking the OpenID redirect.
        if (existing.isEmpty()) {
            var email = user.getEmail();
            Thread.startVirtualThread(() -> {
                try {
                    syncService.syncUser(email);
                } catch (Exception e) {
                    log.warn("Initial Steam sync failed for {}: {}", email, e.getMessage());
                }
            });
        }

        var token = jwtService.generateToken(user.getEmail());
        return AuthResponse.of(token, user);
    }

    private User createSteamUser(String steamId) {
        var user = new User();
        user.setEmail("steam_" + steamId + "@steamtracker.local");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setSteamId(steamId);

        var summary = steamClient.getPlayerSummary(steamId);
        if (summary != null) {
            user.setPersonaName(summary.personaName());
            user.setAvatarUrl(summary.avatarUrl());
        }
        return userRepository.save(user);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
