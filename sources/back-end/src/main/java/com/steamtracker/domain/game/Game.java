package com.steamtracker.domain.game;

import com.steamtracker.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "app_id")
    private Long appId;

    @Column
    private String name;

    @Column(name = "playtime_minutes")
    private Long playtimeMinutes = 0L;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

}