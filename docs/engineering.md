# Engineering notes

Details behind the [README](../README.md): the full API, the design decisions and how they are tested.

## API reference

Each service publishes interactive API documentation while it is running (raw OpenAPI spec at `/v3/api-docs`):

- auth-service: **http://localhost:8081/swagger-ui.html**
- monitoring-service: **http://localhost:8082/swagger-ui.html**
- ingestion-service: **http://localhost:8083/swagger-ui.html**

### Auth service

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registers a company and its first user with the `ADMIN` role | `201` · `400` · `409` |
| `POST` | `/api/auth/login` | Checks the credentials and sets the `access_token` and `refresh_token` cookies | `200` · `400` · `401` · `429` |
| `POST` | `/api/auth/refresh` | Exchanges the refresh token cookie for new tokens; the old refresh token is revoked | `200` · `401` |
| `POST` | `/api/auth/logout` | Revokes the refresh token and clears both cookies | `204` |
| `GET` | `/api/users/me` | Returns the profile of the signed-in user (requires the access token cookie) | `200` · `401` · `404` |
| `GET` | `/.well-known/jwks.json` | Publishes the public key that other services use to verify access tokens | `200` |

Tokens are never returned in the response body; the browser stores them as HttpOnly cookies and sends them automatically.

### Monitoring service

All endpoints require the access token cookie and only return data of the signed-in user's company.

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `GET` | `/api/sites` | Lists the company's sites, ordered by name | `200` · `401` |
| `POST` | `/api/sites` | Creates a site (name, type, floor area, city); names are unique within a company | `201` · `400` · `403` · `409` |
| `GET` | `/api/sites/{siteId}` | Returns one site | `200` · `404` |
| `PUT` | `/api/sites/{siteId}` | Replaces the details of a site | `200` · `400` · `403` · `404` · `409` |
| `DELETE` | `/api/sites/{siteId}` | Deletes a site; rejected while the site still has meters | `204` · `403` · `404` · `409` |
| `GET` | `/api/sites/{siteId}/meters` | Lists the meters of a site | `200` · `404` |
| `POST` | `/api/sites/{siteId}/meters` | Adds a meter to a site; serial numbers are unique across the platform | `201` · `400` · `403` · `404` · `409` |
| `GET` | `/api/meters/{meterId}` | Returns one meter | `200` · `404` |
| `PUT` | `/api/meters/{meterId}` | Replaces the details of a meter | `200` · `400` · `403` · `404` · `409` |
| `DELETE` | `/api/meters/{meterId}` | Deletes a meter | `204` · `403` · `404` |

Meters are created under their site and then addressed directly by their own ID, which keeps URLs short without losing the parent relation.

**Dashboard.** `period` is `TODAY` (default), `LAST_7_DAYS` or `LAST_30_DAYS`; each period starts at local midnight (Istanbul) and runs until now. Without `siteId` the figures cover all sites of the company.

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `GET` | `/api/consumption?siteId=&period=` | Consumption and solar production per hour (per day for 30 days), for the chart | `200` · `400` · `404` |
| `GET` | `/api/kpis?siteId=&period=` | Total consumption, cost split by tariff band, CO₂ and solar production, for the summary cards | `200` · `400` · `404` |
| `GET` | `/api/kpis/sites?period=` | One row per site: consumption, cost, solar production and consumption per m², for comparing sites | `200` · `400` |

**Live readings (WebSocket).** The browser connects to `/ws` with STOMP and subscribes to `/topic/tenant/{tenantId}/site/{siteId}`. Every reading the monitoring service stores is pushed to that topic the moment it arrives, so the dashboard updates without polling:

```json
{ "meterId": "…", "serialNumber": "EM-2024-001", "meterType": "ELECTRICITY", "measuredAt": "2026-11-19T10:30:00Z", "energyKwh": 0.1150 }
```

### Ingestion service

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `POST` | `/api/readings` | Accepts one meter reading (serial number, end of interval, kWh) and publishes it to Kafka | `202` · `400` · `503` |

The endpoint answers `202 Accepted` only after Kafka has stored the reading, so an accepted reading is never lost; if Kafka is unreachable it answers `503` and the data logger retries. The reading is stored asynchronously by the monitoring service. The endpoint is called by meter data loggers (the simulator for now), not by users; device authentication with API keys is planned.

### Roles

| Role | Sites and meters |
|---|---|
| `ADMIN` | Read and change |
| `ENERGY_MANAGER` | Read and change |
| `VIEWER` | Read only (changes return `403`) |

The role is a claim in the access token and is checked with `@PreAuthorize` on each endpoint.

Successful responses share a common envelope:

```json
{
  "data": { "tenantId": "…", "userId": "…", "email": "ayse@deniz.com", "role": "ADMIN" },
  "message": "Company registered successfully",
  "timestamp": "2026-10-15T09:00:00Z"
}
```

Errors are returned as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details (`application/problem+json`). Validation errors list every invalid field:

```json
{
  "title": "Invalid request",
  "status": 400,
  "detail": "Request validation failed",
  "errors": { "email": "must be a well-formed email address", "password": "size must be between 8 and 72" }
}
```

## Engineering practices

**Architecture**
- Code is packaged by feature (`authentication`, `tenant`, `user`, `token` in auth-service; `site`, `meter`, `reading`, `consumption`, `tariff`, `kpi`, `live` in monitoring-service; `reading` in ingestion-service; `profile`, `reading` in the simulator), not by layer.
- Controllers depend on service interfaces (`AuthService` → `AuthServiceImpl`), which keeps layers decoupled and lets the web layer be tested with mocks.
- The ingestion controller only knows the `ReadingPublisher` interface. The first implementation only logged readings; `KafkaReadingPublisher` replaced it later without any change to the controller or its tests.
- The same idea in monitoring-service: the reading service only knows `LiveReadingPublisher` (implemented with STOMP today), and cost is calculated through a `Tariff` interface (see below).
- Entities are never exposed by the API; requests and responses use dedicated DTOs (Java records).
- Database schema changes are versioned with Flyway; Hibernate only validates the schema.

**Security**
- Passwords are hashed with BCrypt (input limited to 72 bytes, BCrypt's maximum).
- Access tokens are short-lived (15 min) JWTs signed with RS256. Only the auth service holds the private key; other services fetch the public key from its JWKS endpoint and verify every token locally, so no secret is shared and the auth service is not called on each request.
- Tokens travel in `HttpOnly`, `SameSite=Strict` cookies, so JavaScript cannot read them (XSS) and other sites cannot send them (CSRF).
- Refresh tokens (7 days) are random values stored only as SHA-256 hashes, sent only to `/api/auth`, rotated on every use and revoked on logout.
- Login returns the same error for an unknown email and a wrong password to prevent user enumeration.
- Failed logins are limited to 5 per 15 minutes per email with a Redis sliding window (sorted set); Redis keys contain a hash of the email, not the email itself.
- Emails are normalised with `Locale.ROOT` to avoid the Turkish dotless-i problem.
- Role-based access: write operations require `ADMIN` or `ENERGY_MANAGER`; a missing role returns a `403` problem detail.

**Simulator**
- Each building type is a `ConsumptionProfile` (Strategy pattern): `OfficeProfile`, `HotelProfile` and `SolarProfile` return the share of installed power in use at a given local time. The reading generator receives all profiles from Spring as a list and never names them, so a new profile is added without touching it.
- The profiles use HVAC domain knowledge: a base load that never switches off, occupancy (office hours, hotel breakfast and evening peaks, quiet weekends in offices) and a cooling load that follows the season and the daily temperature curve. Solar production follows the day length and sun height of central Türkiye.
- Meters are configured in `application.yaml` (serial number, profile, installed kW) and validated at startup; every 10 seconds one reading per meter (±5 % noise) is sent to the ingestion service with `RestClient`. A failed send is logged and the simulation keeps running, like a real data logger.
- The profiles are an approximation. Replaying measured data from the [Building Data Genome Project 2](https://github.com/buds-lab/building-data-genome-project-2) and injecting labelled anomalies is planned for the alert and ML work.

**Event streaming (Kafka)**
- Readings are published to the `meter.readings` topic (3 partitions) with the meter's serial number as the key, so all readings of one meter land in the same partition and keep their order.
- The producer waits for Kafka's acknowledgement (`acks=all`, idempotent producer) before the API answers; timeouts are short so a Kafka outage becomes a fast `503` instead of a hanging request.
- Messages are plain JSON without Java type headers: the JSON shape is the contract, so producer and consumer share no classes.
- The monitoring service consumes the topic in its own consumer group (`monitoring-service`); the planned alert service will read the same messages in a separate group.
- Failures are handled by Spring Kafka's `DefaultErrorHandler`: a message is retried 3 times, 1 second apart, then moved to `meter.readings.DLT` with the exception in its headers. Messages that can never succeed (an unknown meter, unreadable JSON via `ErrorHandlingDeserializer`) go to the dead-letter topic straight away, so one bad message cannot block a partition. Each dead-lettered message is logged with its topic, partition and offset.
- Kafka delivers at least once, so the consumer is idempotent: readings have the primary key `(meter_id, measured_at)` and are inserted with `ON CONFLICT DO NOTHING`; a redelivered message changes nothing.

**Time series (TimescaleDB)**
- Readings live in a TimescaleDB hypertable split into 7-day chunks, so time-range queries only scan the chunks they need.
- They are written with plain SQL through `JdbcClient` rather than JPA: readings are many, small and never updated, and they need database-specific SQL (`ON CONFLICT`).
- Hierarchical continuous aggregates (`readings_15m` → `readings_hourly` → `readings_daily`) keep pre-computed totals up to date in the background; charts read a few hundred rows instead of millions. Real-time aggregation adds the latest, not yet materialised minutes at query time.
- Daily totals are bucketed by Istanbul days, not UTC days, so consumption between midnight and 03:00 is counted on the right day (and later, on the right bill).

**Costs and carbon**
- Costs follow Türkiye's three-period tariff: day (06–17), peak (17–22) and night (22–06), by Istanbul local time. Each hourly total is priced at the rate of its hour, and the response shows how much of the bill each band makes up, which is where savings come from (shifting load out of peak hours).
- A tariff is a `Tariff` interface with two implementations (Strategy pattern): `TimeOfUseTariff` (three rates) and `SingleRateTariff` (one rate, which many industrial customers use). The KPI service asks the tariff for the price of an hour and does not know which type it is; the type is chosen in configuration.
- Prices come from EPDK's published tariff (low voltage, single-term, "public and private services and other", energy plus distribution, excluding taxes) and live in `application.yaml`, validated at startup, so a new tariff period is a configuration change, not a code change.
- Solar production is reported separately and is not priced as consumption.
- CO₂ is consumption × the grid emission factor for distribution-connected consumers (0.474 kg CO₂/kWh, Ministry of Energy, 2022).
- Amounts are `BigDecimal` and are rounded only once, at the end (kWh to 3 decimals, ₺ to 2), so hourly rounding errors do not add up.
- Site comparison includes consumption per m², so a large hotel and a small office can be compared fairly; a site without a floor area shows no value rather than a misleading zero.

**Live updates (WebSocket)**
- After a reading is stored, the monitoring service publishes it with STOMP to the topic of its site. Repeated Kafka messages are not stored again, so they are not pushed twice either.
- The meter is loaded together with its site in one query (`@EntityGraph`), because the Kafka listener runs outside a transaction and needs the site's company to build the topic.
- An in-memory broker is enough for a single instance; with several instances it would be replaced by an external broker (for example RabbitMQ) behind the same `LiveReadingPublisher` interface.

**Multi-tenancy**
- All companies share the same tables; every row carries a `tenant_id`.
- The tenant is always taken from the verified access token, never from the request body, so a user cannot act on behalf of another company.
- Every query is filtered by tenant; another company's resource is reported as not found (`404`) rather than forbidden, so its existence is not revealed.
- Meters have no tenant column of their own: they belong to a site, and every meter query joins through the site's `tenant_id`, so a meter can never end up in a different company than its site.
- Live topics follow the same rule. The WebSocket handshake requires the access token cookie, and a channel interceptor checks every subscription: a user may subscribe only to a single site topic of their own company (no wildcards), and clients may not send messages to topics at all.

**Data integrity**
- Business rules are checked in the service layer with clear `409` messages (duplicate site name, duplicate serial number, deleting a site that still has meters); database constraints (`UNIQUE`, foreign keys without cascade) remain as the last line of defence.
- Meter serial numbers are stored in one canonical form (trimmed, upper case), so `em-2024-001` and `EM-2024-001` count as the same device.

**Testing** (`./mvnw verify`)

| Level | Example | What it covers |
|---|---|---|
| Unit | `AuthServiceImplTest`, `RefreshTokenTest`, `OfficeProfileTest`, `ReadingGeneratorTest`, `TariffBandTest`, `TariffTest`, `SiteKpiTest`, `ReadingServiceImplTest`, `LiveTopicGuardTest` | Business rules with Mockito, entity rules (token expiry and revocation), simulator calculations (load curves, energy per interval), tariff band boundaries and prices, consumption per m², what is pushed live and which subscriptions are allowed; no Spring, no database |
| Configuration | `TariffConfigTest` | The tariff type chosen in configuration creates the right `Tariff`, and a missing or negative price stops the application at startup (`ApplicationContextRunner`) |
| Web layer | `AuthControllerTest`, `JwksControllerTest`, `SecurityConfigTest`, `ReadingControllerTest` | HTTP status codes, validation, JSON shape and token checks with `@WebMvcTest` and a mocked service |
| HTTP client | `IngestionClientTest` | The exact request the simulator sends, and that a failing server does not stop it, with `@RestClientTest` and `MockRestServiceServer` |
| Messaging | `KafkaReadingPublisherTest` | The reading is sent to the right topic with the serial number as key, and both a rejected send and an unreachable broker become an error the API turns into `503` (mocked `KafkaTemplate`) |
| Integration | `RegistrationIntegrationTest`, `LoginIntegrationTest`, `RefreshAndLogoutIntegrationTest`, `LoginRateLimitIntegrationTest`, `SiteIntegrationTest`, `MeterIntegrationTest`, `ConsumptionIntegrationTest`, `KpiIntegrationTest` | Full user journeys (register → login → protected endpoint → refresh → logout, rate limiting), site/meter management (tenant isolation, roles, uniqueness rules) and dashboard figures (hourly and daily totals, cost per tariff band, CO₂, site comparison, with a fixed clock) against real TimescaleDB and Redis started by Testcontainers |
| Data pipeline | `ReadingListenerIntegrationTest`, `ReadingRepositoryTest`, `ReadingAggregatesTest` | Kafka → consumer → TimescaleDB with real containers: readings are stored, a duplicate message is stored once, unknown meters and invalid JSON end up in the dead-letter topic, the table is a hypertable, and aggregates sum correctly across 15-minute, hourly and Istanbul day boundaries (asynchronous checks with Awaitility) |
| WebSocket | `LiveReadingIntegrationTest` | A real STOMP client on a running server: a reading sent to Kafka reaches the subscribed client, subscribing to another company's site is rejected with an `ERROR` frame, and a handshake without a token gets `401` |

Error cases are tested alongside the happy paths: wrong credentials, missing or reused tokens, malformed requests, too many attempts, another company's data, missing roles, an unavailable Kafka broker, messages that cannot be processed, unknown periods and subscriptions to another company's live data.

**Logging**
- SLF4J logs for business events (`INFO`), rejected requests (`WARN`) and unexpected errors with stack traces (`ERROR`).
- Logs contain IDs only; personal data such as email addresses is never logged (KVKK/GDPR).
- Unexpected errors return a generic `500` problem detail without exposing internals.

**Workflow and code quality**
- Every change goes through a feature branch and a pull request; CI must pass before merging.
- Commit messages and PR titles follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat(auth): …`, `test(auth): …`).
- Code is checked locally with SonarQube for IDE (SonarLint) before committing.

## Running the services in detail

Commands are in the [README](../README.md#getting-started).

| Service | Port | Database |
|---|---|---|
| `auth-service` | `8081` | `auth_db` |
| `monitoring-service` | `8082` | `monitoring_db` |
| `ingestion-service` | `8083` | none |
| `simulator` | none (no web server) | none |

Each service applies its own Flyway migrations on startup. The connection can be overridden with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables, Kafka with `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`); monitoring-service finds the auth service's public key through `AUTH_JWKS_URI` (default `http://localhost:8081/.well-known/jwks.json`).

With the simulator running, readings flow through Kafka into `monitoring_db`. To watch the topic or the dead-letter topic:

```bash
docker exec basat-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic meter.readings
```

```bash
docker exec basat-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic meter.readings.DLT --from-beginning --formatter-property print.headers=true
```
