
-- Bases criadas na primeira inicialização do Postgres.
-- Só roda quando o volume pgdata está vazio.

CREATE DATABASE catalog;
CREATE DATABASE auth;
CREATE DATABASE orders;

-- Descomente quando os serviços forem criados:
-- CREATE DATABASE payment_db;
-- CREATE DATABASE inventory_db;