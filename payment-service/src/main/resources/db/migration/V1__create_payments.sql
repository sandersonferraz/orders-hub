CREATE TABLE payments (
                          id         BIGSERIAL PRIMARY KEY,
                          order_id   BIGINT NOT NULL UNIQUE,   -- idempotência: um pagamento por pedido
                          status     VARCHAR(30) NOT NULL,     -- APPROVED / REFUSED
                          amount     NUMERIC(10, 2) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);