CREATE TABLE IF NOT EXISTS trained_models (
    id SERIAL PRIMARY KEY,
    version INT NOT NULL,
    model_data BYTEA NOT NULL,
    rmse NUMERIC NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    metadata JSONB
);

CREATE INDEX idx_active_model ON trained_models (is_active) WHERE is_active = TRUE;