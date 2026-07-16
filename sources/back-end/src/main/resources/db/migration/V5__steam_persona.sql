-- Cached Steam profile shown in the UI instead of the synthetic login email.
ALTER TABLE users ADD COLUMN persona_name VARCHAR(255);
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
