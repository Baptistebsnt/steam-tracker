CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    steam_id VARCHAR(64) UNIQUE,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    app_id BIGINT NOT NULL,
    name VARCHAR(255),
    playtime_minutes BIGINT DEFAULT 0,
    last_synced_at TIMESTAMP,
    UNIQUE(user_id, app_id)
);

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    api_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    description TEXT,
    icon_url VARCHAR(500),
    unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP,
    global_percent DECIMAL(5,2),
    UNIQUE(game_id, api_name)
);

CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_id     BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_value BIGINT,
    completed   BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT now()
)