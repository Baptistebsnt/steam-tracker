-- User-chosen display name (pseudo) shown across the UI so the email is never exposed.
ALTER TABLE users ADD COLUMN username VARCHAR(30);
