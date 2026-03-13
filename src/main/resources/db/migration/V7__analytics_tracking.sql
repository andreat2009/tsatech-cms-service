CREATE TABLE IF NOT EXISTS analytics_visitor_session (
    id BIGSERIAL PRIMARY KEY,
    visitor_id VARCHAR(128) NOT NULL UNIQUE,
    first_seen TIMESTAMPTZ NOT NULL,
    last_seen TIMESTAMPTZ NOT NULL,
    last_ip VARCHAR(128),
    last_user_agent VARCHAR(1024),
    last_path VARCHAR(1024),
    last_referrer VARCHAR(1024),
    last_locale VARCHAR(16),
    last_user_id VARCHAR(128),
    view_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS analytics_event (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES analytics_visitor_session(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    path VARCHAR(1024) NOT NULL,
    page_title VARCHAR(255),
    referrer VARCHAR(1024),
    entity_type VARCHAR(64),
    entity_id VARCHAR(128),
    ip VARCHAR(128),
    user_agent VARCHAR(1024),
    locale VARCHAR(16),
    user_id VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_analytics_event_created_at ON analytics_event(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_event_path ON analytics_event(path);
CREATE INDEX IF NOT EXISTS idx_analytics_event_entity ON analytics_event(entity_type, entity_id);
