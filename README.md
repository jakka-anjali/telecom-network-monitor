# 📡 Telecom Network Incident Monitoring Platform

[![CI/CD Pipeline](https://github.com/your-username/telecom-network-monitor/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/your-username/telecom-network-monitor/actions)
[![Java 21/25](https://img.shields.io/badge/Java-21%20%2F%2025%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Node.js](https://img.shields.io/badge/Node.js-20%20%2F%2022-green.svg)](https://nodejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg)](https://www.docker.com/)

An enterprise-grade, cloud-ready backend platform and operations command center for monitoring telecom cellular infrastructure (4G/5G), detecting abnormal operational telemetry conditions using a configurable rule engine, managing automated incident correlation and deduplication, and enabling Network Operations Center (NOC) teams to triage and resolve incidents.

Inspired by real-world Tier-1 telecom carrier NOC software architectures (such as Vodafone Idea, Airtel, or Jio), this platform is engineered with modern SDE best practices: modular layered design, Bean Validation, state machine lifecycles, and automated CI/CD.

---

## 🏗️ Architecture Overview

```
                         ┌─────────────────────────────────────────┐
                         │   NOC Operations Team / Engineers       │
                         └───────────────────┬─────────────────────┘
                                             │ HTTPS
                                             ▼
                         ┌─────────────────────────────────────────┐
                         │      Node.js Operations Gateway         │
                         │      (BFF & Single-Page Dashboard)      │
                         │             Port: 3000                  │
                         └───────────────────┬─────────────────────┘
                                             │ HTTP / REST Proxy
                                             ▼
                         ┌─────────────────────────────────────────┐
                         │     Spring Boot REST API Controllers    │
                         │             Port: 8080                  │
                         │  /api/towers  /api/network-events       │
                         │  /api/incidents  /api/analytics         │
                         └───────────────────┬─────────────────────┘
                                             │
                       ┌─────────────────────┴─────────────────────┐
                       ▼                                           ▼
         ┌───────────────────────────┐               ┌───────────────────────────┐
         │     Service Layer         │               │  Incident Detection Engine│
         │ Tower / Network / Incident│               │  - Composite Score Engine │
         │ Analytics / Alert Service │               │  - Deduplication Window   │
         └─────────────┬─────────────┘               │  - Severity Escalation    │
                       │                             └─────────────┬─────────────┘
                       ▼                                           │
         ┌───────────────────────────┐                             │
         │ Spring Data JPA/Hibernate │◄────────────────────────────┘
         └─────────────┬─────────────┘
                       │
        ┌──────────────┴──────────────┐
        ▼                             ▼
┌───────────────────┐       ┌───────────────────┐
│ PostgreSQL / H2   │       │   Redis Cache     │
│ Persistent Store  │       │   Fast Lookups    │
└───────────────────┘       └───────────────────┘

TELEMETRY EVENT STREAMING PIPELINE:
┌─────────────────────────┐       ┌──────────────────┐       ┌────────────────────────┐
│ Real-Time Telemetry     │──────▶│  Apache Kafka    │──────▶│ Spring Boot Kafka      │
│ Simulator (Node.js/IoT) │       │  telecom.network │       │ Consumer Group         │
└─────────────────────────┘       │  .events         │       └───────────┬────────────┘
                                  └──────────────────┘                   │
                                                                         ▼
                                                             [Incident Detection Engine]
```

---

## 🌟 Key Highlights & Engineering Features

1. **Incident Detection Rule Engine with Composite Scoring**:
   - Evaluates incoming cell telemetry (`latencyMs`, `packetLossPct`, `throughputMbps`, `activeUsers`, `signalDbm`).
   - Calculates a multi-factor abnormality score (0–100) weighting latency spikes, packet loss cliffs, throughput collapse under load, and subscriber congestion.
   - Categorizes incidents into deterministic severity tiers: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.

2. **Temporal Deduplication & Correlation Window (Anti-Alert Storm)**:
   - Prevents the classic telecom "Alert Storm" where 10,000 incoming packets per minute from a severed optical cable create 10,000 duplicate Jira/ServiceNow tickets.
   - Correlates incoming events to existing non-resolved incidents on the same tower, escalating severity if conditions deteriorate (e.g. `HIGH` &rarr; `CRITICAL`), while updating the root-cause audit trail.

3. **Strict Incident Lifecycle State Machine**:
   - Enforces transition integrity: `OPEN` &rarr; `ACKNOWLEDGED` &rarr; `IN_PROGRESS` &rarr; `RESOLVED`.
   - Prevents unauthorized state re-opening (`InvalidStatusTransitionException`).
   - **Autonomous Infrastructure Recovery**: When the last active incident for a cell tower is marked `RESOLVED`, the system automatically restores the tower's status back to `ACTIVE`.

4. **Alert Notification Dispatcher**:
   - Automatically dispatches simulated emergency alerts via PagerDuty (for `CRITICAL`) and Slack (for `HIGH`) with recipient routing and delivery logging.

5. **Full-Featured Node.js Operations Dashboard**:
   - Real-time dark-mode NOC dashboard with KPI cards (Network Health Index SLA %, MTTR, active incidents, cell availability).
   - Interactive Anomaly Injector to simulate fiber cuts, congestion, and outages on-demand.
   - Modal-driven incident triage, status transitions, and audit trail inspection.

6. **Environment Agnostic Execution**:
   - Zero-dependency local developer mode: Runs immediately using Maven and an in-memory PostgreSQL-compatible H2 database.
   - Containerized production mode: Multi-stage Docker and Docker Compose orchestrating Spring Boot, PostgreSQL, Kafka (KRaft), Redis, and Node.js.

---

## 📂 Project Directory Structure

```
telecom-network-monitor/
├── .github/
│   └── workflows/
│       └── ci-cd.yml             # Automated Maven test, Node check & Docker build pipeline
├── backend/
│   ├── pom.xml                   # Maven dependencies (Spring Boot 3.3.4, JPA, Kafka, Redis, OpenAPI)
│   └── src/
│       ├── main/
│       │   ├── java/com/telecom/monitor/
│       │   │   ├── config/       # Swagger OpenAPI 3, CORS, Seed Data Initializer
│       │   │   ├── controller/   # REST Controllers (Towers, Events, Incidents, Analytics)
│       │   │   ├── dto/          # Data Transfer Objects with Bean Validation (@NotNull, @Min, @Max)
│       │   │   ├── exception/    # Global Exception Handler with standardized RFC Problem Details
│       │   │   ├── kafka/        # Kafka Telemetry Producer and Consumer components
│       │   │   ├── model/        # JPA Entities (Tower, NetworkEvent, Incident, Alert) & Enums
│       │   │   ├── repository/   # Spring Data JPA Repositories
│       │   │   └── service/      # Business Services & IncidentDetectionEngine
│       │   └── resources/
│       │       ├── application.yml         # Local development profile (zero-setup H2 mode)
│       │       └── application-docker.yml  # Docker / Prod profile (PostgreSQL, Kafka, Redis)
│       └── test/java/com/telecom/monitor/  # JUnit 5 & Mockito Unit + MockMvc Integration Tests
├── dataset/
│   ├── telecom_towers_seed.csv   # Metro cell towers dataset (Hyderabad, Bangalore, Mumbai, Delhi)
│   ├── historical_network_events.csv # Sample historical telemetry dataset
│   └── sample_incidents_audit.json   # Sample incident logs with alert dispatches
├── frontend/
│   ├── package.json              # Node.js BFF dependencies
│   ├── server.js                 # Express API Proxy & SPA server
│   └── public/
│       ├── index.html            # NOC Operations Center web application
│       ├── styles.css            # Dark mode NOC stylesheet
│       └── app.js                # Frontend state management & live API integration
├── telemetry-simulator/
│   └── simulator.js              # Continuous telemetry streaming engine
├── Dockerfile                    # Multi-stage container build (Eclipse Temurin JRE)
├── docker-compose.yml            # Multi-container orchestration (App, DB, Kafka, Redis, UI)
├── ARCHITECTURE.md               # Deep-dive system design & interview trade-offs
├── GIT_DEPLOYMENT_GUIDE.md       # Step-by-step Git Bash commands for GitHub
└── README.md                     # Project documentation
```

---

## 🚀 Quickstart Guide

You can run this project locally on your machine in **under 2 minutes** without installing Docker or external databases.

### Prerequisites (Already on your laptop):
- **Java 21 / 25 LTS** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Node.js 20+** (`node -v`)

---

### Step 1: Run the Spring Boot Backend

Open your terminal or Git Bash and navigate to the backend directory:

```bash
cd "telecom-network-monitor/backend"
mvn spring-boot:run
```

The backend starts on port **8080**. It automatically:
- Creates the in-memory schema.
- Seeds 10 metro cell towers (Hyderabad, Bengaluru, Mumbai, Delhi, Chennai, Pune).
- Seeds initial incidents and telemetry.
- Exposes Swagger UI at: **`http://localhost:8080/swagger-ui.html`**

---

### Step 2: Run the Operations Dashboard (Node.js)

Open a second terminal window:

```bash
cd "telecom-network-monitor/frontend"
npm install
npm start
```

The Operations Dashboard will launch at: **`http://localhost:3000`**

Open your browser to **`http://localhost:3000`**. You will see:
- Real-time Network Health Index SLA gauge.
- Active cell towers grid.
- Incident Command Center with triage filters.
- **One-Click Chaos Simulator** to trigger live fiber cuts, outages, and congestions!

---

### Step 3: Run the Continuous Telemetry Stream (Optional)

In a third terminal, start the background telemetry generator:

```bash
cd "telecom-network-monitor/telemetry-simulator"
node simulator.js
```

This continuously generates stochastic telemetry across all cell towers, automatically triggering alerts when anomalies occur.

---

### Step 4: Run Tests

Run the comprehensive unit and integration test suite:

```bash
cd "telecom-network-monitor/backend"
mvn test
```

All 10 unit and integration tests run in under 2 seconds, verifying:
- Rule engine threshold evaluations and scoring.
- Temporal incident deduplication.
- State machine lifecycle validations.
- REST endpoint responses and Bean Validation constraints.

---

## 🐳 Running with Docker Compose (Full Stack)

To run the complete production stack with PostgreSQL, Kafka, and Redis:

```bash
cd "telecom-network-monitor"
docker compose up --build
```

Services exposed:
- **Operations Dashboard**: `http://localhost:3000`
- **Spring Boot Backend**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **PostgreSQL Database**: `localhost:5432` (`telecom_db` / `telecom_admin`)
- **Apache Kafka Broker**: `localhost:9092`
- **Redis Cache**: `localhost:6379`

---

## 📡 Key REST APIs

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/towers` | List all monitored cell towers (filter by `region`) |
| `GET` | `/api/towers/{id}` | Get tower details and status |
| `POST` | `/api/towers` | Register new cell tower (`@Valid` payload) |
| `POST` | `/api/network-events` | Ingest real-time telemetry packet |
| `GET` | `/api/network-events` | Retrieve recent telemetry packets |
| `POST` | `/api/network-events/simulate` | Trigger simulated telemetry anomaly |
| `GET` | `/api/incidents` | Query incidents (filter by `status`, `severity`, `region`) |
| `GET` | `/api/incidents/{id}` | Retrieve incident details |
| `PUT` | `/api/incidents/{id}/status` | Update incident state (`ACKNOWLEDGED`, `IN_PROGRESS`, `RESOLVED`) |
| `GET` | `/api/incidents/{id}/alerts` | Audit trail of alerts dispatched for incident |
| `GET` | `/api/analytics/network-health` | Aggregate SLA score, tower availability, MTTR |

---
