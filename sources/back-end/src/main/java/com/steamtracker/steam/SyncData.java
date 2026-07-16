package com.steamtracker.steam;

import com.steamtracker.steam.dto.SteamAchievementDto;

import java.util.List;

/**
 * In-memory results of the (parallel) Steam fetch phase, handed off to the
 * transactional persistence phase. Keeping the network payload as plain data
 * lets us fan out the HTTP calls across threads without sharing a JPA session.
 */
record GameSyncData(long appId, String name, long playtimeMinutes, List<AchievementSyncData> achievements) {}

record AchievementSyncData(SteamAchievementDto dto, String iconUrl) {}
