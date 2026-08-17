CREATE TABLE categories (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(10, 2) NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    version     BIGINT NOT NULL DEFAULT 0
);