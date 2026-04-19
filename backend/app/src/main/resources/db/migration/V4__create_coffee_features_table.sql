CREATE TABLE IF NOT EXISTS coffee_features (
    coffee_id BIGINT PRIMARY KEY,
    origins TEXT[],
    process TEXT,
    roast_level INTEGER NOT NULL DEFAULT 2, -- 2 is MEDIUM
    description TEXT,
    altitude INT4RANGE,
    sca_score DECIMAL(4, 2),
    acidity SMALLINT,
    body SMALLINT,
    aftertaste SMALLINT,
    sweetness SMALLINT,
    bitterness SMALLINT,
    flavor_notes TEXT[],
    flavor_vector VECTOR(100),

    CONSTRAINT fk_coffee_bean_id FOREIGN KEY (coffee_id) REFERENCES coffee_beans(id) ON DELETE CASCADE
);