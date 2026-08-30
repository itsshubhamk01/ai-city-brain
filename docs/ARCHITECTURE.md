# Architecture

## 1. System overview

```
                         ┌─────────────────────────────────────────────┐
                         │              React Dashboard                 │
                         │   (REST for reads/writes, WebSocket for      │
                         │    live push: city status, alerts, decisions)│
                         └───────────────┬───────────────┬─────────────┘
                                         REST           WebSocket
                                          │                │
┌─────────────────────────────────────────▼────────────────▼───────────────────┐
│                            Spring Boot API (backend)                          │
│                                                                                │
│  Controllers → Services → Repositories → Flyway-managed schema (H2/Postgres)  │
│                                                                                │
│   ┌─────────────────────────── Simulation Engine (scheduled tick) ─────────┐  │
│   │ writes raw metrics (traffic, rainfall, AQI, power, hospital, waste)    │  │
│   │ occasionally spawns organic incidents                                 │  │
│   └───────────────────────────────────┬─────────────────────────────────┘  │
│                                       calls, in fixed order                  │
│   ┌────────┐ ┌────────┐ ┌─────────┐ ┌───────┐ ┌────────────┐ ┌───────────┐  │
│   │ Flood  │▶│Traffic │ │ Energy  │ │ Waste │ │ Healthcare │ │ Emergency │  │
│   │ Agent  │ │ Agent  │ │ Agent   │ │ Agent │ │ Agent      │ │ Agent     │  │
│   └───┬────┘ └───┬────┘ └────┬────┘ └───┬───┘ └─────┬──────┘ └─────┬─────┘  │
│       └──────────┴────────publish/subscribe───────────┴──────────────┘      │
│                              (in-process EventBus)                           │
│                                     │                                        │
│                            ┌────────▼────────┐                              │
│                            │   CityBrain     │  aggregates, prioritizes,     │
│                            │  orchestrator   │  raises Alerts, detects       │
│                            └────────┬────────┘  compound multi-hazard risk   │
│                                     │                                        │
│                        pushes CityStatus + decision feed over WebSocket      │
└────────────────────────────────────────────────────────────────────────────┘
```

## 2. The agent / event model

Every specialized agent (`FloodAgent`, `TrafficAgent`, `EnergyAgent`, `WasteAgent`,
`HealthcareAgent`, `EmergencyAgent`) implements a single-method `CityAgent` interface
(`evaluate()`), called once per simulation tick in a fixed order chosen so that
downstream agents see the freshest possible upstream data within the same tick
(Flood → Traffic → Energy → Waste → Healthcare → Emergency).

Agents never call each other directly. They communicate exclusively through
`EventBus.publish(CityEvent)` / `EventBus.subscribe(eventType, listener)`. For
example: `FloodAgent` detects a zone crossing the critical flood-risk threshold and
publishes `FLOOD_RISK_CRITICAL`. `TrafficAgent` has subscribed to that event type and
reacts by closing the roads in that zone. `EmergencyAgent` has also subscribed and
reroutes any ambulance currently stationed there. `CityBrainService` subscribes to
**every** event (`subscribeAll`) and is the only component that (a) turns
high/critical severity events into citizen-facing `Alert` rows, and (b) watches for
multiple distinct critical event types converging on the same zone within a short
window, at which point it raises a single "coordinated response" `AgentAction`
attributed to `CITY_BRAIN` — a decision no individual agent could justify alone. This
is a deliberate implementation of the brief's worked example ("Zone 4: heavy rain +
traffic congestion + power strain → City Brain recommends evacuation").

Every agent observation is written to `agent_events` (the audit trail of "what an
agent noticed") and every action to `agent_actions` ("what an agent did about it").
The frontend's "AI Decisions" feed is simply these two tables merged and sorted by
time — nothing is synthesized purely for display.

### Why an in-process bus instead of Kafka?

The brief calls for Kafka/RabbitMQ as one option for the event layer. Running Kafka
(or even a KRaft-mode broker) reliably on an arbitrary personal laptop, for free,
adds real memory/CPU overhead and operational surface area for a single-process demo
that emits a handful of events every few seconds. `EventBus` is defined as an
interface specifically so this is a reversible decision: `InMemoryEventBus` is a
~40-line synchronous, in-memory pub/sub implementation; a `KafkaEventBus implements
EventBus` backed by Spring Kafka could be introduced as an alternate `@Bean` without
changing a single agent. Synchronous dispatch was also chosen deliberately over
`@Async` fan-out because it keeps a tick's cause-and-effect chain deterministic and
easy to unit test — a real trade-off (lower throughput ceiling) made explicitly for
demo reliability over horizontal scalability.

## 3. Simulation engine

`SimulationEngineService.tick()` runs on a `@Scheduled(fixedDelay = ...)` timer
(default 4s, configurable via `SIM_TICK_INTERVAL_MS`). Each tick:

1. Writes new raw metrics per zone via a bounded random walk (`smooth()` — an
   exponential-moving-average step toward a slider-driven target, not pure noise),
   so values drift realistically instead of jittering randomly every tick.
2. Has a small (~14%) chance of spawning an organic incident so the demo feels alive
   with zero operator input.
3. Runs every agent's `evaluate()`.
4. Recomputes each zone's aggregate `riskScore` (see `RiskScoring`).
5. Auto-resolves incidents that have been `IN_PROGRESS` for more than 24 seconds,
   freeing the ambulance/fire unit that was responding — a deliberate simplification
   (the exact resource that responded isn't tracked 1:1 against the incident; the
   nearest same-zone dispatched unit is freed instead) so the incident list doesn't
   grow forever during a demo.
6. Every 5th tick, persists a `ZoneStatusSnapshot` per zone for the trend charts.
7. Hands off to `CityBrainService.onTickCompleted()`, which pushes the refreshed
   `CityStatusResponse` and decision feed over the WebSocket.

Operators drive the simulation two ways:
- **Sliders** (`PATCH /api/v1/simulation/control`) set the five underlying targets
  (rainfall, traffic intensity, population pressure, power demand, emergency level)
  that `applyRawMetrics()` smooths toward every tick.
- **Scenario presets** (`POST /api/v1/simulation/scenario`) are named shortcuts that
  set several sliders at once and/or directly perturb specific zones (e.g.
  `POWER_OUTAGE` forces one zone's supply down and lets `EnergyAgent` discover and
  react to the resulting strain organically on the next tick, rather than faking the
  outcome).

## 4. The "what-if" simulator

`WhatIfSimulatorService` is intentionally read-only: it loads the *current* live zone
state, projects a hypothetical delta through the same formulas the real agents use
(`RiskScoring.computeFloodRisk`, etc.), and returns a result — without writing
anything back to the database. This is what lets an Analyst-role user safely explore
"what if it rains 50% more?" without any risk of corrupting the live simulation.

## 5. AI / insight-generation layer

`InsightGenerator` is implemented by `RuleBasedInsightGenerator` (deterministic
template-based text, zero cost, zero dependency — this is what runs by default) and
`LlmInsightGenerator` (calls the real Anthropic Messages API). `ResilientInsightGenerator`
is the only bean actually injected anywhere (`@Primary`): it checks whether the LLM
path is enabled (`app.ai.provider=llm` **and** a non-blank `ANTHROPIC_API_KEY`), tries
it with an 8-second timeout, and falls back to the rule-based generator on *any*
exception — a missing key, a network failure, a malformed response, anything. Neither
the "AI Briefing" button nor the What-If narrative can ever hard-fail because of this
layer. No API key is ever hard-coded; the default configuration runs the platform
with zero external calls.

## 6. Security architecture

- Stateless JWT (HS256, `jjwt` 0.13.x) — no server-side session store, so the API can
  scale horizontally without sticky sessions.
- Passwords are BCrypt-hashed (cost factor 10); the seeded demo accounts are real
  BCrypt hashes generated for this project, not placeholders.
- `SecurityConfig` explicitly wires a custom `AuthenticationEntryPoint` /
  `AccessDeniedHandler` pair. This matters: Spring Security's *default* fallback for
  an unauthenticated request (when no `formLogin()`/`httpBasic()` is configured) is
  `Http403ForbiddenEntryPoint` — i.e. an anonymous request to a protected endpoint
  would silently return 403 instead of the semantically correct 401. The custom entry
  point fixes this and keeps every error response (security-layer or
  controller-layer) in the exact same JSON shape.
- Method-level `@PreAuthorize` on every mutating endpoint, layered on top of the URL
  pattern rules in `SecurityConfig` (defense in depth).

### Security notes for anyone deploying this beyond a laptop demo

- Change `JWT_SECRET` to a real random value (`openssl rand -base64 48`) — the
  default in `application.yml` is intentionally an obvious placeholder.
- The H2 web console (`/h2-console`) is enabled in the `local` profile purely for
  local development convenience; disable it (or don't use the `local` profile) in any
  shared environment.
- Replace or disable the seeded demo accounts (`V3__seed_users.sql`).

## 7. Database design

Schema is fully owned by Flyway (`backend/src/main/resources/db/migration`) —
`spring.jpa.hibernate.ddl-auto` is set to `none` everywhere. This was a deliberate
choice over `validate`: without a live Maven/compile loop to confirm Hibernate 6's
exact expected DDL types (timestamp precision, UUID column type) match hand-written
SQL, `validate` risked a false-positive startup failure on first run. `none` makes
Flyway's SQL the single, unambiguous source of truth; ordinary JDBC type coercion
(not Hibernate's schema-comparison logic) handles reads/writes, which is far more
tolerant of minor DDL differences. See `V1__schema.sql` for the full normalized
schema (14 tables, foreign keys, indexes on every hot query path) and
`V2`–`V4` for the NovaCity seed data.

## 8. Versioning notes

This project targets **Spring Boot 3.5.16** (the final patch of the 3.x line) rather
than the now-current Spring Boot 4.x. That's a direct consequence of how this
codebase was produced: it was generated without a live connection to Maven Central to
compile against, so the safer choice was the long-established 3.x API surface, which
could be written with high confidence, over the newer 4.x/Spring Framework 7 surface,
whose exact breaking changes couldn't be verified in that environment. Upgrading to
Boot 4.x is a reasonable next step and should mostly follow Spring's official
migration guide; expect changes around `spring-boot-starter-parent` version,
possibly `jakarta.*` namespace details, and Spring Security's configuration DSL.

## 9. Frontend architecture

- Vite + React 18 + TypeScript, Tailwind for styling with a small custom design-token
  system (`tailwind.config.js`) rather than default Tailwind grays — see the color/
  type rationale in the login/dashboard components.
- A single typed `api.ts` client wraps `fetch`, attaches the JWT, and normalizes
  errors; a `WebSocketProvider` context manages one reconnecting WebSocket connection
  and a channel-based pub/sub so any page can subscribe to `city-status`, `alert`,
  `decisions`, or `incident` pushes without opening its own socket.
- Leaflet (via `react-leaflet`) renders the map using free CARTO dark-mode tiles —
  every marker is a custom `L.divIcon`, deliberately avoiding Leaflet's default
  marker image asset (a common source of broken-icon bugs under bundlers like Vite).
- `npm run build` (`tsc -b && vite build`) was actually run and passes with zero
  errors as part of producing this project.
