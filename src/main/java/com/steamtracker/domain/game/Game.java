package com.steamtracker.domain.game;

import com.steamtracker.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPlaytimeMinutes() { return playtimeMinutes; }
    public void setPlaytimeMinutes(Long playtimeMinutes) { this.playtimeMinutes = playtimeMinutes; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}