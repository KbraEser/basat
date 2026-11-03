# Basat

[![CI](https://github.com/KbraEser/basat/actions/workflows/ci.yml/badge.svg)](https://github.com/KbraEser/basat/actions/workflows/ci.yml)

**Real-time energy monitoring and efficiency platform for businesses.**

> 🚧 **Work in progress.** This project is under active development. See the [roadmap](#roadmap) for current status.

## The problem

Most businesses only find out how much electricity they used when the monthly bill arrives. They cannot see **where, when or why** energy is being wasted: air conditioning left running overnight, faulty equipment, or heavy consumption during peak-tariff hours all go unnoticed until they show up on the invoice.

## The solution

Basat is a multi-tenant SaaS platform that turns meter data into actionable insight:

- 📈 **Live monitoring** of energy consumption across sites and meters
- 🚨 **Anomaly detection and alerts** for unusual consumption (night-time usage, sudden spikes, offline meters)
- 💰 **Cost analysis** based on Turkey's three-period electricity tariff (day, peak, night)
- 🏢 **Site comparison** and efficiency scores (e.g. consumption per m²)
- 🌱 **CO₂ emissions reporting**
- 🤖 **AI assistant** that answers questions like *"Why did consumption increase last week?"* using real data

**Target users:** SMEs, factories, hotels, schools, multi-branch businesses and solar plant owners who need energy visibility without the cost and complexity of enterprise systems.

## Architecture

Target architecture (event-driven, built around Kafka):

```
Meter data → Ingestion Service → Kafka → Monitoring Service → TimescaleDB
                                   ↘ Alert Service → WebSocket / Email
React dashboard ← API Gateway ← Auth Service · Monitoring Service · AI Service
```

| Service | Responsibility | Status |
|---|---|---|
| `auth-service` | Tenants, users, roles, JWT authentication | 🟢 Done (registration, login, refresh, logout, rate limiting) |
| `ingestion-service` | Validates meter readings and publishes them to Kafka | ⚪ Planned |
| `monitoring-service` | Sites, meters, time-series storage, costs, reports | 🟡 In progress (token validation, site and meter management done; time-series storage next) |
| `alert-service` | Anomaly rules and notifications | ⚪ Planned |
| `ai-service` | AI assistant with tool calling over real data | ⚪ Planned |
| `api-gateway` | Single entry point, routing, rate limiting | ⚪ Planned |
| `simulator` | Replays real building data and injects anomalies | ⚪ Planned |
| `frontend` | React dashboard | ⚪ Planned |

## Tech stack

Target stack for the whole platform. Items marked ✅ are already in use.

| Area | Technologies |
|---|---|
| **Backend** | ✅ Java 21 (virtual threads), ✅ Spring Boot 4, ✅ Spring Security, ✅ Spring Data JPA, ✅ Flyway, ✅ springdoc-openapi, ✅ JWT (RS256, Nimbus), Spring Kafka, WebSocket (STOMP), Spring Cloud Gateway, Spring AI |
| **Data** | ✅ PostgreSQL 17 + TimescaleDB, ✅ Redis, Apache Kafka |
| **Frontend** | React, TypeScript, Vite, TanStack Query, Redux Toolkit, Tailwind CSS, ECharts |
| **Testing** | ✅ JUnit 5, ✅ Mockito, ✅ AssertJ, ✅ Testcontainers, Vitest, Cypress, k6 |
| **DevOps** | ✅ Docker, ✅ Docker Compose, ✅ GitHub Actions, Prometheus, Grafana, Loki |

## API

Each service publishes interactive API documentation while it is running (raw OpenAPI spec at `/v3/api-docs`):

- auth-service: **http://localhost:8081/swagger-ui.html**
- monitoring-service: **http://localhost:8082/swagger-ui.html**

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
- Code is packaged by feature (`authentication`, `tenant`, `user`, `token` in auth-service; `site`, `meter` in monitoring-service), not by layer.
- Controllers depend on service interfaces (`AuthService` → `AuthServiceImpl`), which keeps layers decoupled and lets the web layer be tested with mocks.
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

**Multi-tenancy**
- All companies share the same tables; every row carries a `tenant_id`.
- The tenant is always taken from the verified access token, never from the request body, so a user cannot act on behalf of another company.
- Every query is filtered by tenant; another company's resource is reported as not found (`404`) rather than forbidden, so its existence is not revealed.
- Meters have no tenant column of their own: they belong to a site, and every meter query joins through the site's `tenant_id`, so a meter can never end up in a different company than its site.

**Data integrity**
- Business rules are checked in the service layer with clear `409` messages (duplicate site name, duplicate serial number, deleting a site that still has meters); database constraints (`UNIQUE`, foreign keys without cascade) remain as the last line of defence.
- Meter serial numbers are stored in one canonical form (trimmed, upper case), so `em-2024-001` and `EM-2024-001` count as the same device.

**Testing** (`./mvnw verify`)

| Level | Example | What it covers |
|---|---|---|
| Unit | `AuthServiceImplTest`, `RefreshTokenTest` | Business rules with Mockito and entity rules (token expiry and revocation); no Spring, no database |
| Web layer | `AuthControllerTest`, `JwksControllerTest`, `SecurityConfigTest` | HTTP status codes, validation, JSON shape and token checks with `@WebMvcTest` and a mocked service |
| Integration | `RegistrationIntegrationTest`, `LoginIntegrationTest`, `RefreshAndLogoutIntegrationTest`, `LoginRateLimitIntegrationTest`, `SiteIntegrationTest`, `MeterIntegrationTest` | Full user journeys (register → login → protected endpoint → refresh → logout, rate limiting) and site/meter management (tenant isolation, roles, uniqueness rules) against real TimescaleDB and Redis started by Testcontainers |

Error cases are tested alongside the happy paths: wrong credentials, missing or reused tokens, malformed requests, too many attempts, another company's data and missing roles.

**Logging**
- SLF4J logs for business events (`INFO`), rejected requests (`WARN`) and unexpected errors with stack traces (`ERROR`).
- Logs contain IDs only; personal data such as email addresses is never logged (KVKK/GDPR).
- Unexpected errors return a generic `500` problem detail without exposing internals.

**Workflow and code quality**
- Every change goes through a feature branch and a pull request; CI must pass before merging.
- Commit messages and PR titles follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat(auth): …`, `test(auth): …`).
- Code is checked locally with SonarQube for IDE (SonarLint) before committing.

## Getting started

### Prerequisites

- Java 21
- Docker with Docker Compose (local infrastructure and Testcontainers)

### Start local infrastructure

```bash
docker compose up -d
```

This starts TimescaleDB (PostgreSQL 17) and Redis (used for login rate limiting). The init script in `infra/postgres/init` creates one database per service (`auth_db`, `monitoring_db`, `alert_db`).

| Service | Host port | Credentials |
|---|---|---|
| TimescaleDB | `5433` | `basat` / `basat` |
| Redis | `6379` | none |

TimescaleDB is mapped to `5433` so it does not clash with a PostgreSQL instance already running on `5432`.

### Run the services

Start each service in its own terminal:

```bash
./mvnw -pl auth-service spring-boot:run
```

```bash
./mvnw -pl monitoring-service spring-boot:run
```

| Service | Port | Database |
|---|---|---|
| `auth-service` | `8081` | `auth_db` |
| `monitoring-service` | `8082` | `monitoring_db` |

Each service applies its own Flyway migrations on startup. The connection can be overridden with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables; monitoring-service finds the auth service's public key through `AUTH_JWKS_URI` (default `http://localhost:8081/.well-known/jwks.json`).

Try it with the Swagger UI at http://localhost:8081/swagger-ui.html, or with curl:

```bash
curl -i -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"ayse@deniz.com","password":"GucluSifre123"}'
```

### Build and test

```bash
git clone https://github.com/KbraEser/basat.git
cd basat
./mvnw verify
```

Tests run against a real TimescaleDB instance started automatically with Testcontainers, so no local database setup is needed.

## Roadmap

- [x] Maven multi-module project setup
- [x] CI with GitHub Actions
- [x] Local infrastructure with Docker Compose (TimescaleDB, Redis)
- [x] Company registration with validation, problem-detail errors and tests
- [x] API documentation with OpenAPI / Swagger UI
- [x] Authentication: login with JWT in HttpOnly cookies, refresh token rotation and logout
- [x] Login rate limiting with Redis (sliding window)
- [x] Service-to-service token validation with JWKS
- [x] Site and meter management with tenant isolation and role-based access
- [ ] Data simulator with realistic consumption profiles
- [ ] Kafka-based ingestion and time-series storage
- [ ] Dashboard with charts and KPIs
- [ ] Real-time updates over WebSocket
- [ ] Alerts and notifications
- [ ] Tariffs, costs and reports
- [ ] AI assistant
- [ ] Monitoring, deployment and live demo
- [ ] Site-level access (e.g. a branch manager who only sees their own site)

## Why "Basat"?

In the *Book of Dede Korkut*, a classic Turkish epic, the hero **Basat** defeats **Tepegöz**, a giant who endlessly consumes the people's resources. Basat does the same with energy waste: it finds it and stops it.

## Author

**Kübra Eser**, Full Stack Developer with a background in Energy Systems Engineering.

[GitHub](https://github.com/KbraEser)