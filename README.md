# OrdersHub

Plataforma de pedidos baseada em microservices, em construção. Projeto de estudo para praticar Spring Cloud (Eureka, Config Server), banco certo por caso de uso (Postgres, Mongo, Redis) e mensageria (Kafka).

## Serviços

| Serviço | Porta | Status | Descrição |
|---|---|---|---|
| `discovery-service` | 8761 | ✅ | Eureka — registro e descoberta |
| `config-server` | 8888 | ✅ | Spring Cloud Config (lê o `config-repo/`) |
| `catalog-service` | 8082 | ✅ | Catálogo — produtos e categorias |
| `auth-service` | 8081 | ✅ | Autenticação — registro, login e refresh (JWT) |
| `api-gateway` | 8080 | ✅ | Borda única — roteamento, JWT e rate limit |
| `order-service` | 8083 | ✅ | Pedidos — criação com outbox e Kafka |
| `payment-service` | 8084 | ✅ | Pagamentos — consumidor Kafka + DLT, Pagar.me |
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

   Na primeira subida, o `infra/postgres/init.sql` cria os bancos `catalog`, `auth` e `orders` automaticamente.

3. Suba os serviços (cada um em um terminal):

   ```bash
   mvn -pl discovery-service spring-boot:run
   mvn -pl config-server spring-boot:run
   mvn -pl catalog-service spring-boot:run
   mvn -pl auth-service spring-boot:run
   mvn -pl api-gateway spring-boot:run
   mvn -pl order-service spring-boot:run
   mvn -pl payment-service spring-boot:run
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

## auth-service

Dono do domínio de autenticação. Emite e valida JWT (access + refresh) e guarda o refresh token no Redis.

| Banco | Uso |
|---|---|
| Postgres `auth` | usuários (`users`) — senha hasheada com BCrypt |
| Redis | refresh tokens (`refresh:*` com TTL) |

Endpoints:

- `POST /auth/register` — cria usuário e devolve access + refresh
- `POST /auth/login` — autentica e devolve access + refresh
- `POST /auth/refresh` — rotaciona o refresh token

Detalhes: access token com TTL de 15 min, refresh de 7 dias; senha via BCrypt.

## api-gateway

Borda única da plataforma (Spring Cloud Gateway, reativo). Roteia por `lb://` via Eureka e valida o JWT antes de liberar a rota.

| Rota | Destino |
|---|---|
| `/auth/**` | auth-service (pública) |
| `/products/**`, `/categories/**` | catalog-service |
| `/orders/**` | order-service |

Detalhes: o `JwtAuthFilter` valida o `Authorization: Bearer` e injeta `X-User-Id` para os serviços internos; o `RequestRateLimiter` limita por IP via Redis. Todo caminho fora de `/auth/**` exige token (401 caso contrário).

## order-service

Dono do domínio de pedidos. Cria o pedido consultando o catálogo (Feign + Circuit Breaker) e grava o evento no outbox para publicação assíncrona via Kafka.

| Banco | Uso |
|---|---|
| Postgres `orders` | pedidos (`orders`) e outbox (`outbox_events`) |

Endpoints:

- `POST /orders` — cria um pedido (`{ "productId": 1, "customerId": "u-1" }`)

Detalhes: o preço vem do `catalog-service` via `CatalogClient` (Feign) com Circuit Breaker `catalog` (fallback: preço ZERO); o evento `OrderCreated` é gravado em `outbox_events` na mesma transação do pedido e publicado pelo `OutboxPublisher` em `orders.events` (chave de partição = orderId).

## payment-service

Dono do domínio de pagamentos. Consome o evento de pedido criado, processa o pagamento na Pagar.me e publica o resultado em outro tópico.

| Banco | Uso |
|---|---|
| Postgres `payments` | pagamentos (`payments`) — um por pedido (idempotência) |

Fluxo (mensageria):

- Consome `orders.events` → `OrderCreatedEvent` (grupo `payment-service`)
- Processa via `PagarMeClient` (cartão de teste fixo) → status `APPROVED` ou `REFUSED`
- Publica `payments.events` → `PaymentApprovedEvent` ou `PaymentRefusedEvent` (chave = orderId)
- Retry: `@RetryableTopic` (4 tentativas, backoff 3s × 2); falha definitiva cai em `orders.events-dlt`

Detalhes: idempotência via `orderId` único em `payments`; integração Pagar.me via `RestClient` com `Authorization: Basic` (`PAGARME_API_KEY` no `.env`).

## Verificação

```bash
curl http://localhost:8082/products

curl -X POST http://localhost:8082/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Teclado Mecânico","price":349.90}'
```
