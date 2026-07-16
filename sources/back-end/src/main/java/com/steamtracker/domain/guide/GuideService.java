package com.steamtracker.domain.guide;

import com.steamtracker.domain.achievement.dto.AchievementDto;
import com.steamtracker.domain.game.GameService;
import com.steamtracker.domain.guide.dto.*;
import com.steamtracker.domain.user.User;
import com.steamtracker.domain.user.UserRepository;
import com.steamtracker.error.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GuideService {

    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final GameService gameService;

    public GuideService(GuideRepository guideRepository,
                        UserRepository userRepository,
                        GameService gameService) {
        this.guideRepository = guideRepository;
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    @Transactional(readOnly = true)
    public List<GuideSummaryDto> list(Long appId) {
        var guides = appId != null
                ? guideRepository.findByAppIdOrderByCreatedAtDesc(appId)
                : guideRepository.findAllByOrderByCreatedAtDesc();
        return guides.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<GuideSummaryDto> listMine(String email) {
        var user = findUser(email);
        return guideRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public GuideDetailDto getDetail(Long id, String email) {
        var guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guide introuvable"));
        return toDetail(guide, email);
    }

    @Transactional
    public GuideDetailDto create(String email, GuideRequest request) {
        var user = findUser(email);

        var guide = new Guide();
        guide.setAuthor(user);
        applyRequest(guide, request);
        guideRepository.save(guide);

        return toDetail(guide, email);
    }

    @Transactional
    public GuideDetailDto update(String email, Long id, GuideRequest request) {
        var guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guide introuvable"));
        requireAuthor(guide, email);

        applyRequest(guide, request);
        guide.setUpdatedAt(LocalDateTime.now());
        guideRepository.save(guide);

        return toDetail(guide, email);
    }

    @Transactional
    public void delete(String email, Long id) {
        var guide = guideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guide introuvable"));
        requireAuthor(guide, email);
        guideRepository.delete(guide);
    }

    // --- helpers ---

    private void applyRequest(Guide guide, GuideRequest request) {
        guide.setAppId(request.appId());
        guide.setGameName(request.gameName());
        guide.setTitle(request.title());
        guide.setDescription(request.description());

        var steps = new java.util.ArrayList<GuideStep>();
        int position = 0;
        for (var stepInput : request.steps()) {
            var step = new GuideStep();
            step.setPosition(position++);
            step.setTitle(stepInput.title());
            step.setContent(stepInput.content());
            if (stepInput.achievements() != null) {
                for (var achievementInput : stepInput.achievements()) {
                    var achievement = new GuideStepAchievement();
                    achievement.setApiName(achievementInput.apiName());
                    achievement.setDisplayName(achievementInput.displayName());
                    achievement.setIconUrl(achievementInput.iconUrl());
                    step.addAchievement(achievement);
                }
            }
            steps.add(step);
        }
        guide.replaceSteps(steps);
    }

    private void requireAuthor(Guide guide, String email) {
        if (email == null || !guide.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("Seul l'auteur peut modifier ce guide");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private GuideSummaryDto toSummary(Guide guide) {
        int achievementCount = guide.getSteps().stream()
                .flatMap(s -> s.getAchievements().stream())
                .map(GuideStepAchievement::getApiName)
                .collect(Collectors.toSet())
                .size();
        return new GuideSummaryDto(
                guide.getId(),
                guide.getAppId(),
                guide.getGameName(),
                guide.getTitle(),
                guide.getAuthor().getEmail(),
                guide.getSteps().size(),
                achievementCount,
                guide.getCreatedAt()
        );
    }

    private GuideDetailDto toDetail(Guide guide, String email) {
        var unlocked = unlockedApiNames(email, guide.getAppId());

        var steps = guide.getSteps().stream()
                .map(step -> new GuideStepDto(
                        step.getId(),
                        step.getPosition(),
                        step.getTitle(),
                        step.getContent(),
                        step.getAchievements().stream()
                                .map(a -> new GuideStepAchievementDto(
                                        a.getApiName(),
                                        a.getDisplayName(),
                                        a.getIconUrl(),
                                        unlocked.contains(a.getApiName())
                                ))
                                .toList()
                ))
                .toList();

        var linkedApiNames = guide.getSteps().stream()
                .flatMap(s -> s.getAchievements().stream())
                .map(GuideStepAchievement::getApiName)
                .collect(Collectors.toSet());
        int unlockedCount = (int) linkedApiNames.stream().filter(unlocked::contains).count();

        boolean isAuthor = email != null && guide.getAuthor().getEmail().equals(email);

        return new GuideDetailDto(
                guide.getId(),
                guide.getAppId(),
                guide.getGameName(),
                guide.getTitle(),
                guide.getDescription(),
                guide.getAuthor().getEmail(),
                isAuthor,
                linkedApiNames.size(),
                unlockedCount,
                guide.getCreatedAt(),
                guide.getUpdatedAt(),
                steps
        );
    }

    /**
     * Set of achievement apiNames the reader has unlocked for this game. Empty when the reader
     * is anonymous or has not synced this game.
     */
    private Set<String> unlockedApiNames(String email, Long appId) {
        if (email == null) {
            return Set.of();
        }
        try {
            return gameService.getAchievementsForGame(email, appId).stream()
                    .filter(AchievementDto::unlocked)
                    .map(AchievementDto::apiName)
                    .collect(Collectors.toSet());
        } catch (ResourceNotFoundException e) {
            return Set.of();
        }
    }
}
