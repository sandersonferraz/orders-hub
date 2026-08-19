CREATE TABLE stock_items (
                             id         BIGSERIAL PRIMARY KEY,
                             product_id BIGINT NOT NULL UNIQUE,
                             quantity   INTEGER NOT NULL CHECK (quantity >= 0),
                             version    BIGINT NOT NULL DEFAULT 0
);