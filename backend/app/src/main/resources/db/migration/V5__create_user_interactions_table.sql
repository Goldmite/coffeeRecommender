CREATE TABLE IF NOT EXISTS user_interactions (
    user_id BIGINT,
    coffee_id BIGINT,
    rating SMALLINT,
    is_clicked BOOLEAN,
    is_purchased BOOLEAN,
    purchase_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);