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
| `auth-service` | Tenants, users, roles, JWT authentication | 🟢 Registration done · 🟡 Login in progress |
| `ingestion-service` | Validates meter readings and publishes them to Kafka | ⚪ Planned |
| `monitoring-service` | Sites, meters, time-series storage, costs, reports | ⚪ Planned |
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
| **Data** | ✅ PostgreSQL 17 + TimescaleDB, Apache Kafka, Redis |
| **Frontend** | React, TypeScript, Vite, TanStack Query, Redux Toolkit, Tailwind CSS, ECharts |
| **Testing** | ✅ JUnit 5, ✅ Mockito, ✅ AssertJ, ✅ Testcontainers, Vitest, Cypress, k6 |
| **DevOps** | ✅ Docker, ✅ Docker Compose, ✅ GitHub Actions, Prometheus, Grafana, Loki |

## API

Interactive API documentation is available at **http://localhost:8081/swagger-ui.html** when the auth service is running (raw OpenAPI spec: `/v3/api-docs`).

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `POST` | `/api/auth/register` | Registers a company and its first user with the `ADMIN` role | `201` · `400` · `409` |

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
- Code is packaged by feature (`authentication`, `tenant`, `user`, `token`), not by layer.
- Controllers depend on service interfaces (`AuthService` → `AuthServiceImpl`), which keeps layers decoupled and lets the web layer be tested with mocks.
- Entities are never exposed by the API; requests and responses use dedicated DTOs (Java records).
- Database schema changes are versioned with Flyway; Hibernate only validates the schema.

**Security**
- Passwords are hashed with BCrypt (input limited to 72 bytes, BCrypt's maximum).
- Access tokens are JWTs signed with RS256: only the auth service holds the private key, other services will verify tokens with the public key.
- Login returns the same error for an unknown email and a wrong password to prevent user enumeration.
- Emails are normalised with `Locale.ROOT` to avoid the Turkish dotless-i problem.

**Testing** (`./mvnw verify`)

| Level | Example | What it covers |
|---|---|---|
| Unit | `AuthServiceImplTest` | Business rules with Mockito; no Spring, no database |
| Web layer | `AuthControllerTest` | HTTP status codes, validation and JSON shape with `@WebMvcTest` and a mocked service |
| Integration | `RegistrationIntegrationTest` | Full request through to a real TimescaleDB started by Testcontainers |

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

This starts TimescaleDB (PostgreSQL 17) and Redis. The init script in `infra/postgres/init` creates one database per service (`auth_db`, `monitoring_db`, `alert_db`).

| Service | Host port | Credentials |
|---|---|---|
| TimescaleDB | `5433` | `basat` / `basat` |
| Redis | `6379` | none |

TimescaleDB is mapped to `5433` so it does not clash with a PostgreSQL instance already running on `5432`.

### Run the auth service

```bash
./mvnw -pl auth-service spring-boot:run
```

The service listens on port `8081` and applies the Flyway migrations to `auth_db` on startup. The connection can be overridden with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables.

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
- [ ] Authentication: login with JWT in HttpOnly cookies, refresh and logout (in progress)
- [ ] Login rate limiting with Redis
- [ ] Site and meter management, data simulator
- [ ] Kafka-based ingestion and time-series storage
- [ ] Dashboard with charts and KPIs
- [ ] Real-time updates over WebSocket
- [ ] Alerts and notifications
- [ ] Tariffs, costs and reports
- [ ] AI assistant
- [ ] Monitoring, deployment and live demo

## Why "Basat"?

In the *Book of Dede Korkut*, a classic Turkish epic, the hero **Basat** defeats **Tepegöz**, a giant who endlessly consumes the people's resources. Basat does the same with energy waste: it finds it and stops it.

## Author

**Kübra Eser**, Full Stack Developer with a background in Energy Systems Engineering.

[GitHub](https://github.com/KbraEser)