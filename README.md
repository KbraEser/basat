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

Planned services (event-driven, built around Kafka):

```
Meter data → Ingestion Service → Kafka → Monitoring Service → TimescaleDB
                                   ↘ Alert Service → WebSocket / Email
React dashboard ← API Gateway ← Auth Service · Monitoring Service · AI Service
```

| Service | Responsibility | Status |
|---|---|---|
| `auth-service` | Tenants, users, roles, JWT authentication | 🟡 Skeleton |
| `ingestion-service` | Validates meter readings and publishes them to Kafka | ⚪ Planned |
| `monitoring-service` | Sites, meters, time-series storage, costs, reports | ⚪ Planned |
| `alert-service` | Anomaly rules and notifications | ⚪ Planned |
| `ai-service` | AI assistant with tool calling over real data | ⚪ Planned |
| `api-gateway` | Single entry point, routing, rate limiting | ⚪ Planned |
| `simulator` | Replays real building data and injects anomalies | ⚪ Planned |
| `frontend` | React dashboard | ⚪ Planned |

## Tech stack

| Area | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, Flyway, Spring Kafka, WebSocket (STOMP), Spring Cloud Gateway, Spring AI |
| **Data** | PostgreSQL 17 + TimescaleDB, Apache Kafka, Redis |
| **Frontend** | React, TypeScript, Vite, TanStack Query, Redux Toolkit, Tailwind CSS, ECharts |
| **Testing** | JUnit 5, Mockito, Testcontainers, Vitest, Cypress, k6 |
| **DevOps** | Docker, Docker Compose, GitHub Actions, Prometheus, Grafana, Loki |

## Getting started

### Prerequisites

- Java 21
- Docker (required for Testcontainers)

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
- [ ] Local infrastructure with Docker Compose (TimescaleDB, Redis)
- [ ] Authentication and multi-tenancy
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

**Kübra Eser**, Full Stack Developer with a background in Energy Systems Engineering and HVAC.

[GitHub](https://github.com/KbraEser)