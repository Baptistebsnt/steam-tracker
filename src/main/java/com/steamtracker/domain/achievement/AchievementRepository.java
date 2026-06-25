package com.steamtracker.domain.achievement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByGameId(Long gameId);

    Optional<Achievement> findByGameIdAndApiName(Long gameId, String apiName);

    long countByGameIdAndUnlocked(Long gameId, boolean unlocked);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.game.user.id = :userId AND a.unlocked = true")
    long countUnlockedByUserId(Long userId);
}
