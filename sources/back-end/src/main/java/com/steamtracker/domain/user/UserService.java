package com.steamtracker.domain.user;

import com.steamtracker.domain.user.dto.UpdateProfileRequest;
import com.steamtracker.domain.user.dto.UserProfileDto;
import com.steamtracker.error.ConflictException;
import com.steamtracker.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        userRepository.save(user);
        return toDto(user);
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
        return new UserProfileDto(user.getEmail(), user.getSteamId(), user.getCreatedAt());
    }
}
