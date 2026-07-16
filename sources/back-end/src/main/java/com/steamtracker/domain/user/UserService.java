package com.steamtracker.domain.user;

import com.steamtracker.domain.user.dto.UpdateProfileRequest;
import com.steamtracker.domain.user.dto.UserProfileDto;
import com.steamtracker.error.ConflictException;
import com.steamtracker.error.ResourceNotFoundException;
import com.steamtracker.steam.SteamClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SteamClient steamClient;

    public UserService(UserRepository userRepository, SteamClient steamClient) {
        this.userRepository = userRepository;
        this.steamClient = steamClient;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String email) {
        return toDto(findByEmail(email));
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        var user = findByEmail(email);
        var steamId = normalize(request.steamId());

        if (steamId != null) {
            userRepository.findBySteamId(steamId).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new ConflictException("Ce Steam ID est déjà lié à un autre compte");
                }
            });
        }

        user.setSteamId(steamId);
        refreshPersona(user, steamId);
        userRepository.save(user);
        return toDto(user);
    }

    /** Caches the Steam persona/avatar when a SteamID is linked, clears it when unlinked. */
    private void refreshPersona(User user, String steamId) {
        if (steamId == null) {
            user.setPersonaName(null);
            user.setAvatarUrl(null);
            return;
        }
        var summary = steamClient.getPlayerSummary(steamId);
        if (summary != null) {
            user.setPersonaName(summary.personaName());
            user.setAvatarUrl(summary.avatarUrl());
        }
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private static String normalize(String steamId) {
        if (steamId == null) {
            return null;
        }
        var trimmed = steamId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static UserProfileDto toDto(User user) {
        return new UserProfileDto(
                user.getEmail(),
                user.getSteamId(),
                user.getPersonaName(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
