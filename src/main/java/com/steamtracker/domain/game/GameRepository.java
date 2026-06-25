package com.steamtracker.domain.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByUserId(Long userId);

    Optional<Game> findByUserIdAndAppId(Long userId, Long appId);

    boolean existsByUserIdAndAppId(Long userId, Long appId);
}
