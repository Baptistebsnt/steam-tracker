package com.steamtracker.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String steamId
) {}
