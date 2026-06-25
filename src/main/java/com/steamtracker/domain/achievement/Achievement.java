package com.steamtracker.domain.achievement;

import com.steamtracker.domain.game.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "diplay_name")
    private String diplayName;

    @Column
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column
    private Boolean unlocked;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "global_percent")
    private BigDecimal globalPercent;
}
