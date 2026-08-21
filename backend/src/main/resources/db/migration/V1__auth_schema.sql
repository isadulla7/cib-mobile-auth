CREATE TABLE auth_policy_version (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version INTEGER NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMPTZ,
    CONSTRAINT ck_auth_policy_status CHECK (status IN ('DRAFT','VALIDATED','APPROVED','ACTIVE','ROLLED_BACK'))
);

CREATE UNIQUE INDEX ux_auth_policy_single_active
    ON auth_policy_version ((status))
    WHERE status = 'ACTIVE';

CREATE TABLE auth_policy_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_version_id BIGINT NOT NULL REFERENCES auth_policy_version(id),
    policy_key VARCHAR(100) NOT NULL,
    value_json JSONB NOT NULL,
    description VARCHAR(300),
    UNIQUE (policy_version_id, policy_key)
);

CREATE TABLE auth_ui_config (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_version_id BIGINT NOT NULL REFERENCES auth_policy_version(id),
    flow_code VARCHAR(30) NOT NULL,
    step_code VARCHAR(40) NOT NULL,
    order_no INTEGER NOT NULL,
    min_app_version VARCHAR(30),
    config_json JSONB NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (policy_version_id, flow_code, step_code)
);

CREATE TABLE auth_validation_map (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_version_id BIGINT NOT NULL REFERENCES auth_policy_version(id),
    domain_code VARCHAR(30) NOT NULL,
    step_code VARCHAR(40) NOT NULL,
    field_code VARCHAR(60) NOT NULL,
    validator_code VARCHAR(80) NOT NULL,
    order_no INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_auth_validation_lookup
    ON auth_validation_map(policy_version_id, domain_code, step_code, order_no);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    pinfl VARCHAR(14) NOT NULL UNIQUE,
    passport VARCHAR(20) NOT NULL UNIQUE,
    identity_subject VARCHAR(120),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE','BLOCKED','CLOSED'))
);

CREATE TABLE user_credentials (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash TEXT NOT NULL,
    pin_hash TEXT NOT NULL,
    password_algo VARCHAR(40) NOT NULL,
    pin_algo VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE trusted_devices (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_hash VARCHAR(64) NOT NULL,
    trust_until TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, device_hash)
);

CREATE INDEX ix_trusted_device_lookup
    ON trusted_devices(user_id, device_hash, revoked_at);

CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_hash VARCHAR(64) NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL UNIQUE,
    token_family_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT ck_auth_session_status CHECK (status IN ('ACTIVE','REVOKED','EXPIRED'))
);

CREATE INDEX ix_auth_sessions_user_status ON auth_sessions(user_id, status);
CREATE INDEX ix_auth_sessions_family ON auth_sessions(token_family_id);

CREATE TABLE auth_audit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(80) NOT NULL,
    result VARCHAR(30) NOT NULL,
    correlation_id VARCHAR(80),
    device_hash VARCHAR(64),
    ip_hash VARCHAR(64),
    details_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_auth_audit_user_time ON auth_audit(user_id, created_at DESC);
CREATE INDEX ix_auth_audit_event_time ON auth_audit(event_type, created_at DESC);
