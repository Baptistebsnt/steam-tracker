# Steam Tracker

A personal Steam gaming tracker that syncs your library and achievements via the Steam API. Built with Java 25 and Spring Boot.

![CI](https://github.com/Baptistebsnt/steam-tracker/actions/workflows/ci.yml/badge.svg)

## Features

- **Authentication** — Register / login with JWT-based session management
- **Steam sync** — Pull your games and achievements from the Steam API in one request
- **Achievement tracking** — Browse per-game achievements with unlock status
- **Global stats** — Overview of total playtime, games owned, and completion rate
- **Goal system** — (in progress) Set personal completion goals per game

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA |
| Auth | Spring Security + JWT |
| Mapping | MapStruct + Lombok |
| Build | Maven |

## Getting started

### Prerequisites

- Java 25
- Docker & Docker Compose

### Run locally

```bash
# 1. Start the database
docker compose up -d

# 2. Set required environment variables
export STEAM_API_KEY=your_steam_api_key
export JWT_SECRET=your_secret_at_least_32_chars

# 3. Start the application
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

> Get your Steam API key at https://steamcommunity.com/dev/apikey

## API

All routes except `/auth/**` require a `Bearer` token in the `Authorization` header.

### Auth

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Create an account |
| `POST` | `/auth/login` | Get a JWT token |

### Games

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/games` | List all synced games |
| `GET` | `/games/{appId}/achievements` | Achievements for a game |
| `GET` | `/games/stats` | Global stats (playtime, completion rate…) |

### Sync

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/sync` | Sync library and achievements from Steam |

## Project structure

```
src/main/java/com/steamtracker/
├── auth/          # JWT authentication (filter, service, controller)
├── domain/
│   ├── game/      # Game entity, repository, service, controller
│   ├── achievement/
│   ├── goal/
│   └── user/
├── steam/         # Steam API client and sync logic
└── error/         # Global exception handling
```

## Environment variables

| Variable | Description |
|---|---|
| `STEAM_API_KEY` | Steam Web API key |
| `JWT_SECRET` | Secret used to sign JWT tokens (min. 32 chars) |
| `POSTGRES_DB` | Database name (default: `steamtracker`) |
| `POSTGRES_USER` | Database user (default: `steam`) |
| `POSTGRES_PASSWORD` | Database password |