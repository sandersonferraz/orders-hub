
-- Bases criadas na primeira inicialização do Postgres.
-- Só roda quando o volume pgdata está vazio.

CREATE DATABASE catalog;
CREATE DATABASE auth;
CREATE DATABASE orders;
CREATE DATABASE payments;

-- Descomente quando os serviços forem criados:
-- CREATE DATABASE inventory_db;