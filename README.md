# Basat

[![CI](https://github.com/KbraEser/basat/actions/workflows/ci.yml/badge.svg)](https://github.com/KbraEser/basat/actions/workflows/ci.yml)

**Real-time energy monitoring and efficiency platform for businesses.**

> 🚧 **Work in progress.** This project is under active development. See the [roadmap](#roadmap) for current status.

## What it does

Most businesses only find out how much electricity they used when the monthly bill arrives. They cannot see **where, when or why** energy is wasted. Basat is a multi-tenant SaaS platform that turns meter data into actionable insight for SMEs, factories, hotels, schools, multi-branch businesses and solar plant owners:

- 📈 **Live monitoring** of consumption across sites and meters
- 💰 **Cost analysis** with Türkiye's three-period tariff (day, peak, night)
- 🏢 **Site comparison** (e.g. consumption per m²) and 🌱 **CO₂ reporting**
- 🚨 **Anomaly alerts** for night-time usage, sudden spikes and offline meters (planned)
- 🤖 **AI assistant** that explains the data in plain language (planned)

## Architecture

Event-driven, built around Kafka. The solid path already runs end to end; the rest is planned.

```
Simulator ──REST──▶ Ingestion Service ──▶ Kafka (meter.readings) ──▶ Monitoring Service ──▶ TimescaleDB
                                              │                         │                  (raw readings +
                                              │                         ├─▶ meter.readings.DLT  15 min / hourly /
                                              │                         │   (failed messages)   daily aggregates)
                                              │                         └─▶ WebSocket (STOMP) ──▶ live dashboard
                                              └ ─ ▶ Alert Service ─ ─ ▶ WebSocket / Email      (planned)

React dashboard ──▶ (Vite proxy, later API Gateway) ──▶ Auth Service · Monitoring Service · AI Service
```

| Service | Responsibility | Status |
|---|---|---|
| `auth-service` | Tenants, users, roles, JWT authentication | 🟢 Done (registration, login, refresh, logout, rate limiting) |
| `ingestion-service` | Validates meter readings and publishes them to Kafka | 🟢 Done (validation, Kafka publishing keyed by meter, `503` when Kafka is down) |
| `monitoring-service` | Sites, meters, time-series storage, costs, reports | 🟡 In progress (site and meter management, Kafka consumer, TimescaleDB storage, dashboard API with tariff costs and CO₂, site comparison and live readings over WebSocket done; reports next) |
| `alert-service` | Anomaly rules and notifications | ⚪ Planned |
| `ai-service` | AI assistant with tool calling over real data | ⚪ Planned |
| `api-gateway` | Single entry point, routing, rate limiting | ⚪ Planned |
| `simulator` | Generates realistic meter readings; later replays real building data and injects anomalies | 🟡 In progress (synthetic office, hotel and solar profiles done) |
| `frontend` | React dashboard | 🟡 In progress (Vite, TypeScript, Tailwind and shadcn/ui set up; routing done; dashboard pages next) |

## Tech stack

Target stack for the whole platform. Items marked ✅ are already in use.

| Area | Technologies |
|---|---|
| **Backend** | ✅ Java 21 (virtual threads), ✅ Spring Boot 4, ✅ Spring Security, ✅ Spring Data JPA, ✅ JdbcClient, ✅ Flyway, ✅ springdoc-openapi, ✅ JWT (RS256, Nimbus), ✅ Spring Kafka, ✅ WebSocket (STOMP), Spring Cloud Gateway, Spring AI |
| **Data** | ✅ PostgreSQL 17 + TimescaleDB (hypertables, continuous aggregates), ✅ Redis, ✅ Apache Kafka (KRaft) |
| **Frontend** | ✅ React 19, ✅ TypeScript, ✅ Vite, ✅ Tailwind CSS, ✅ shadcn/ui, Recharts, TanStack Query, Redux Toolkit |
| **Testing** | ✅ JUnit 5, ✅ Mockito, ✅ AssertJ, ✅ Testcontainers (PostgreSQL, Kafka, Redis), ✅ Awaitility, Vitest, Cypress, k6 |
| **DevOps** | ✅ Docker, ✅ Docker Compose, ✅ GitHub Actions, Prometheus, Grafana, Loki |

## Highlights

- **Event-driven ingestion:** readings go through Kafka keyed by meter (ordering per meter), with retries, a dead-letter topic and an idempotent consumer.
- **Time series:** TimescaleDB hypertable with hierarchical continuous aggregates (15 min → hourly → daily) bucketed by Istanbul days.
- **Costs:** three-period tariff as a Strategy (`Tariff` interface), EPDK prices in configuration, `BigDecimal` rounded once at the end.
- **Live dashboard:** readings pushed over WebSocket (STOMP) the moment they are stored.
- **Multi-tenancy:** the company always comes from the verified token; every query and every live subscription is checked against it.
- **Security:** RS256 JWTs in HttpOnly, SameSite cookies, verified locally through JWKS; refresh token rotation; Redis sliding-window login limit.
- **Clean boundaries:** feature packages, service interfaces, DTOs instead of entities, Flyway migrations, common response envelope and RFC 9457 errors.
- **Tests at every level:** unit, web layer, Kafka, WebSocket and integration tests against real TimescaleDB, Kafka and Redis with Testcontainers; error cases tested alongside happy paths.

API reference, design decisions and test details: **[docs/engineering.md](docs/engineering.md)**. Interactive API docs run at `/swagger-ui.html` on each service.

## Getting started

Requires Java 21, Docker and Node.js 22+.

```bash
docker compose up -d
```

Starts TimescaleDB (port `5433`, `basat`/`basat`), Redis (`6379`) and Kafka (`9092`). Then start each service in its own terminal:

```bash
./mvnw -pl auth-service spring-boot:run
```

```bash
./mvnw -pl monitoring-service spring-boot:run
```

```bash
./mvnw -pl ingestion-service spring-boot:run
```

```bash
./mvnw -pl simulator spring-boot:run
```

Frontend (http://localhost:5173, proxies `/api` to the services):

```bash
cd frontend && npm install && npm run dev
```

Build and run all tests (Docker is the only requirement; Testcontainers starts the databases):

```bash
./mvnw verify
```

## Roadmap

- [x] Authentication: registration, JWT login, refresh, logout, rate limiting
- [x] Site and meter management with tenant isolation and roles
- [x] Simulator, ingestion and Kafka pipeline into TimescaleDB
- [x] Dashboard API: consumption, KPIs, tariff costs, CO₂, site comparison
- [x] Live readings over WebSocket
- [ ] Dashboard UI with charts and live consumption
- [ ] Reactive energy penalty detection
- [ ] Alerts and notifications, with an ML-based anomaly rule
- [ ] Reports (PDF, Excel) and AI assistant
- [ ] Monitoring, deployment and live demo

## Why "Basat"?

In the *Book of Dede Korkut*, a classic Turkish epic, the hero **Basat** defeats **Tepegöz**, a giant who endlessly consumes the people's resources. Basat does the same with energy waste: it finds it and stops it.

## Author

**Kübra Eser**, Full Stack Developer with a background in Energy Systems Engineering.
