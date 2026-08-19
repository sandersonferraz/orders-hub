CREATE TABLE orders (
                        id          BIGSERIAL PRIMARY KEY,
                        customer_id VARCHAR(255) NOT NULL,
                        total       NUMERIC(10, 2) NOT NULL,
                        status      VARCHAR(30) NOT NULL,
                        version     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE outbox_events (
                               id             BIGSERIAL PRIMARY KEY,
                               aggregate_type VARCHAR(50) NOT NULL,
                               aggregate_id   BIGINT NOT NULL,
                               event_type     VARCHAR(50) NOT NULL,
                               payload        TEXT NOT NULL,
                               published      BOOLEAN NOT NULL DEFAULT false
);