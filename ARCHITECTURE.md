# 🏗️ System Architecture & Engineering Design

This document details the architectural decisions, design patterns, telemetry processing algorithms, and scaling trade-offs of the **Telecom Network Incident Monitoring Platform**.

---

## 1. Domain Context & High-Level Problem Formulation

In modern cellular networks (4G LTE / 5G NR), telecom operators (e.g., Vodafone Idea, Bharti Airtel, Jio) manage tens of thousands of cell sites (eNodeB / gNodeB). Each tower continuously transmits operational telemetry packets every few seconds:
- **Round-Trip Latency (ms)**: Measures packet transit delay across backhaul transport.
- **Packet Loss (%)**: Measures unacknowledged packets due to physical attenuation or buffer drops.
- **Throughput (Mbps)**: User data rate transmitted over RF carriers.
- **Active User Density**: Connected RRC (Radio Resource Control) user equipment (UE).
- **Physical Cell Status**: Operational health of baseband units (BBU) and remote radio heads (RRH).

When an infrastructure failure occurs (e.g., optical fiber cut between aggregation switches or power transformer failure), thousands of telemetry packets report degradation within seconds. A naive monitoring system suffers from **Alert Storms**—flooding operations queues with thousands of duplicate tickets.

### Our Solution
A scalable, decoupled architecture combining:
1. **Asynchronous Telemetry Ingestion (Kafka)**.
2. **Deterministic Composite Scoring & Temporal Deduplication Engine**.
3. **Relational Persistence Layer (PostgreSQL) with Indexed State Queries**.
4. **Operations Command Center Gateway (Node.js BFF + SPA)**.

---

## 2. End-to-End Telemetry Data Pipeline

```
[Cell Towers / IoT Sensors / Simulator]
                    │
                    ▼  (Stochastic telemetry packets: Latency, Packet Loss, Users)
         ┌─────────────────────┐
         │ Apache Kafka Topic  │  Topic: telecom.network.events
         │ (Key = towerId)     │  Partition Key: towerId guarantees in-order arrival
         └──────────┬──────────┘
                    │
                    ▼  (Consumer Group: telecom-incident-engine-group)
         ┌─────────────────────┐
         │ Kafka Consumer      │
         └──────────┬──────────┘
                    │
                    ▼  (Bean Validation: @Min, @Max, @NotNull)
         ┌─────────────────────┐
         │ NetworkEventService │
         └──────────┬──────────┘
                    │
                    ▼
         ┌────────────────────────────────────────────────────────┐
         │            INCIDENT DETECTION RULE ENGINE              │
         │                                                        │
         │ 1. Composite Abnormality Score Calculation (0 - 100)  │
         │    - Tower Status == DOWN? -> Score = 100 (CRITICAL)   │
         │    - Latency >= 300ms: +45 | >= 200ms: +25 | >= 120ms: +10
         │    - Loss >= 25%: +45      | >= 12%: +25   | >= 5%: +10│
         │    - Throughput collapse under load: +15               │
         │    - High subscriber volume multiplier (>10k): +15     │
         │                                                        │
         │ 2. Severity Classification:                            │
         │    - Score >= 70 -> CRITICAL                           │
         │    - Score >= 45 -> HIGH                               │
         │    - Score >= 25 -> MEDIUM                             │
         │    - Score < 25  -> Normal Telemetry (Ignored)         │
         │                                                        │
         │ 3. Temporal Deduplication & Correlation Window:       │
         │    - Query existing non-RESOLVED incident for tower    │
         │    - Exists? Escalate severity if worse; append RCA    │
         │    - None? Create new INC-XXXX ticket                  │
         └──────────┬─────────────────────────────────────────────┘
                    │
         ┌──────────┴─────────────────────────┐
         │                                    │
         ▼ (If New or Escalated)              ▼
┌──────────────────┐               ┌────────────────────────┐
│ AlertService     │               │ PostgreSQL Persistence │
│ - PagerDuty API  │               │ - network_events       │
│ - Slack Webhook  │               │ - incidents            │
└──────────────────┘               │ - towers (status updated)
                                   └────────────────────────┘
```

---

## 3. Deep Dive: Incident Detection & Deduplication Engine

The core business logic resides in [`IncidentDetectionEngine.java`](file:///backend/src/main/java/com/telecom/monitor/service/IncidentDetectionEngine.java).

### A. Mathematical Formulation of the Composite Score
Let $S$ be the total abnormality score:

$$S = S_{\text{status}} + S_{\text{latency}} + S_{\text{loss}} + S_{\text{throughput}} + S_{\text{congestion}}$$

- If $\text{TowerStatus} = \text{DOWN} \implies S = 100$
- Latency score component:
  $$S_{\text{latency}} = \begin{cases} 45, & \text{if } L \ge 300\text{ ms} \\ 25, & \text{if } 200 \le L < 300\text{ ms} \\ 10, & \text{if } 120 \le L < 200\text{ ms} \\ 0, & \text{otherwise} \end{cases}$$
- Packet loss score component:
  $$S_{\text{loss}} = \begin{cases} 45, & \text{if } P \ge 25\% \\ 25, & \text{if } 12 \le P < 25\% \\ 10, & \text{if } 5 \le P < 12\% \\ 0, & \text{otherwise} \end{cases}$$
- Throughput collapse component: $+15$ if Throughput $< 15\text{ Mbps}$ and Active Users $> 2,000$.
- High subscriber congestion multiplier: $+15$ if Active Users $> 10,000$ and degradation detected.

### B. Deduplication & State Escalation Algorithm
When an anomaly triggers:
1. The engine checks:
   ```java
   incidentRepository.findFirstByTowerIdAndStatusNotOrderByCreatedAtDesc(towerId, IncidentStatus.RESOLVED);
   ```
2. **If an active incident already exists**:
   - The system **does not** create a new incident.
   - It checks whether the new event's severity exceeds the existing severity (`severity.ordinal() > existing.getSeverity().ordinal()`).
   - If worse (e.g., upgraded from `HIGH` to `CRITICAL`), it **escalates** the ticket and triggers an immediate high-priority PagerDuty dispatch.
   - It updates the root cause analysis (RCA) log and latest trigger metrics.
3. **If no active incident exists**:
   - It generates an atomic identifier (`INC-XXXX`).
   - It marks the tower status as `DEGRADED` or `DOWN`.
   - It publishes an emergency alert.

---

## 4. Lifecycle State Machine & Autonomous Recovery

Incidents transition through a deterministic finite state machine (FSM):

```
       [ OPEN ]
          │
     ┌────┴──────────────┐
     │                   │
     ▼                   ▼
[ ACKNOWLEDGED ]    [ IN_PROGRESS ]
     │                   │
     └────┬──────────────┘
          │
          ▼
     [ RESOLVED ]
```

### Transition Rules:
- `OPEN` &rarr; `ACKNOWLEDGED` (Operator claims ticket).
- `ACKNOWLEDGED` &rarr; `IN_PROGRESS` (Field engineers dispatched / diagnostics running).
- `IN_PROGRESS` &rarr; `RESOLVED` (Requires operator ID and resolution notes).
- `RESOLVED` &rarr; Any: **Forbidden**. Throws `InvalidStatusTransitionException` to maintain audit immutability.

### Autonomous Infrastructure Recovery:
When an operator resolves an incident on a cell tower:
```java
long otherActiveCount = incidentRepository.findByTowerId(tower.getId()).stream()
        .filter(i -> !i.getId().equals(incident.getId()) && i.getStatus() != IncidentStatus.RESOLVED)
        .count();

if (otherActiveCount == 0) {
    tower.setStatus(TowerStatus.ACTIVE);
    towerRepository.save(tower);
}
```
If no other active incidents remain for that physical tower, the platform **automatically re-enables the tower's `ACTIVE` operational state**!

---

## 5. Event Streaming Strategy with Apache Kafka

### Why Kafka instead of direct HTTP insertion?
1. **Backpressure Buffer**: During network storms, thousands of towers emit telemetry at rates exceeding database write IOPS. Kafka buffers metrics safely without dropping packets or exhausting connection pools.
2. **Partitioning by `towerId`**:
   - Kafka guarantees total ordering **within a single partition**.
   - By using `towerId` as the Kafka message key, all metrics for a given physical tower are mapped to the same partition.
   - This ensures sequential, chronological evaluation in the consumer without race conditions.

---

## 6. Caching & Query Optimization (Redis & PostgreSQL)

### PostgreSQL Indexing Strategy:
- `idx_event_tower_timestamp` on `(tower_id, timestamp)`: Allows constant-time retrieval of time-series graphs for any individual cell site.
- `idx_incident_status_severity` on `(status, severity)`: Optimizes high-frequency operator filter queries (e.g. `GET /api/incidents?status=OPEN&severity=CRITICAL`).

### Redis Cache-Aside Pattern:
- **Cached Keys**:
  - `incidents:OPEN`: Cached active tickets.
  - `analytics:network-health`: Cached SLA health summary (TTL = 30 seconds).
- **Eviction Strategy**:
  - Whenever an incident is created, escalated, or resolved, `@CacheEvict(allEntries = true)` invalidates the cached collections, ensuring operators always view fresh state.

---

## 7. Node.js Backend-For-Frontend (BFF) Pattern

Instead of having client browsers communicate directly with internal microservices:
1. The **Node.js Express Gateway** sits at the perimeter on Port 3000.
2. It serves the frontend static SPA while proxying `/api/*` requests to Spring Boot on Port 8080.
3. This decouples frontend presentation requirements from core backend domain entities, enabling request batching, token validation, and uniform CORS handling.
