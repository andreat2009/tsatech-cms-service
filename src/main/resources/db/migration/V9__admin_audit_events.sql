CREATE TABLE IF NOT EXISTS admin_audit_events (
    id BIGSERIAL PRIMARY KEY,
    actor_username VARCHAR(255) NOT NULL,
    actor_subject VARCHAR(255),
    action_type VARCHAR(48) NOT NULL,
    target_type VARCHAR(128) NOT NULL,
    target_id VARCHAR(128),
    request_path VARCHAR(1024) NOT NULL,
    http_method VARCHAR(16) NOT NULL,
    outcome VARCHAR(24) NOT NULL,
    status_code INTEGER,
    summary VARCHAR(1024),
    ip_address VARCHAR(128),
    user_agent VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_events_created_at ON admin_audit_events (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_audit_events_target_type ON admin_audit_events (target_type);
