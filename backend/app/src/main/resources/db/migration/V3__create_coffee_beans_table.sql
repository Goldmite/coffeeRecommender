CREATE TABLE IF NOT EXISTS coffee_beans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(96) NOT NULL,
    price DECIMAL(5, 2),
    product_url TEXT,
    shop_id INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_shop FOREIGN KEY (shop_id) REFERENCES shops(id)
);