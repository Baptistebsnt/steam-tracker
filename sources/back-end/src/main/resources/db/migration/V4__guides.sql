CREATE TABLE guides (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    app_id BIGINT NOT NULL,
    game_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE guide_steps (
    id BIGSERIAL PRIMARY KEY,
    guide_id BIGINT NOT NULL REFERENCES guides(id) ON DELETE CASCADE,
    position INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT
);

CREATE TABLE guide_step_achievements (
    id BIGSERIAL PRIMARY KEY,
    guide_step_id BIGINT NOT NULL REFERENCES guide_steps(id) ON DELETE CASCADE,
    api_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    icon_url VARCHAR(500)
);

CREATE INDEX idx_guides_app_id ON guides(app_id);
CREATE INDEX idx_guides_author_id ON guides(author_id);
CREATE INDEX idx_guide_steps_guide_id ON guide_steps(guide_id);
CREATE INDEX idx_guide_step_achievements_step_id ON guide_step_achievements(guide_step_id);
