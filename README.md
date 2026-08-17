# OrdersHub

Plataforma de pedidos baseada em microservices, em construção. Projeto de estudo para praticar Spring Cloud (Eureka, Config Server), banco certo por caso de uso (Postgres, Mongo, Redis) e mensageria (Kafka).

## Serviços

| Serviço | Porta | Status | Descrição |
|---|---|---|---|
| `discovery-service` | 8761 | ✅ | Eureka — registro e descoberta |
| `config-server` | 8888 | ✅ | Spring Cloud Config (lê o `config-repo/`) |
| `catalog-service` | 8082 | ✅ | Catálogo — produtos e categorias |
| `api-gateway` | — | 🚧 | não implementado |
| `auth-service` | — | 🚧 | não implementado |
| `order-service` | — | 🚧 | não implementado |
| `payment-service` | — | 🚧 | não implementado |
| `inventory-service` | — | 🚧 | não implementado |
| `notification-service` | — | 🚧 | não implementado |

## Infra (docker-compose)

| Serviço | Porta |
|---|---|
| Postgres 16 | 5432 |
| Mongo 7 | 27017 |
| Redis 7 | 6379 |
| Kafka (bitnamilegacy 3.7) | 9092 |
| Kafka UI | 8088 |
| Zipkin 3 | 9411 |

## Stack

- Java 21
- Spring Boot 3.5.16
- Spring Cloud 2025.0.3
- Maven (multi-módulo)

## Como rodar

Pré-requisitos: JDK 21, Docker + Docker Compose, Maven 3.6+.

1. Copie o `.env.example` para `.env` (define `POSTGRES_USER`/`POSTGRES_PASSWORD`).
2. Suba a infra:

   ```bash
   docker compose up -d
   ```

   Na primeira subida, o `infra/postgres/init.sql` cria o banco `catalog` automaticamente.

3. Suba os serviços (cada um em um terminal):

   ```bash
   mvn -pl discovery-service spring-boot:run
   mvn -pl config-server spring-boot:run
   mvn -pl catalog-service spring-boot:run
   ```

## catalog-service

Dono do domínio de catálogo — única fonte de verdade de produto. Um banco por caso de uso:

| Banco | Uso |
|---|---|
| Postgres `catalog` | produtos e categorias (relacional, transações, FK) |
| Mongo `catalog_docs` | descrição longa e tags (documento flexível) |
| Redis | cache de leitura (`@Cacheable`/`@CacheEvict`) |

Endpoints:

- `GET` / `POST` `/products`, `GET`/`PUT`/`DELETE` `/products/{id}`
- `GET` / `PUT` `/products/{id}/details` (Mongo)
- `GET` / `POST` `/categories`, `GET`/`PUT`/`DELETE` `/categories/{id}`

Detalhes: schema versionado com Flyway; locking otimista com `@Version`; validação via Bean Validation.

> **Nota (Fedora/SELinux):** o mount do `init.sql` usa a flag `:z`. Todo bind-mount de arquivo/pasta neste host precisa de `:z` (ou `:Z`), senão o container acusa `Permission denied`.

## Verificação

```bash
curl http://localhost:8082/products

curl -X POST http://localhost:8082/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Teclado Mecânico","price":349.90}'
```
