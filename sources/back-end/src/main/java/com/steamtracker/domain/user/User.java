package com.steamtracker.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "steam_id", unique = true)
    private String steamId;

    @Column(name = "username")
    private String username;

    @Column(name = "persona_name")
    private String personaName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Name to show in the UI. Prefers the user-chosen username, then the cached Steam persona.
     * Never falls back to the email — the front-end shows a neutral label when this is {@code null}.
     */
    public String resolveDisplayName() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        if (personaName != null && !personaName.isBlank()) {
            return personaName;
        }
        return null;
    }

}