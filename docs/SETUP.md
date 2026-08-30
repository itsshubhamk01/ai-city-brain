# Setup Guide

## Prerequisites

| Tool | Version | Required for |
|---|---|---|
| JDK | 21+ | Backend |
| Maven | 3.9+ (or use the included `mvnw` wrapper if present in your clone) | Backend |
| Node.js | 20+ | Frontend |
| Docker + Docker Compose | any recent version | Only if you want the PostgreSQL profile |

Nothing else. The default backend profile (`local`) uses a file-based H2 database
that Spring Boot creates automatically — no database server to install.

## 1. Backend

```bash
cd backend
mvn spring-boot:run
```

This starts on `http://localhost:8080` using the `local` profile:
- H2 database file created at `backend/data/ai-city-brain.mv.db`
- Flyway runs all migrations automatically (schema + NovaCity seed data + demo users)
- The simulation engine autostarts and ticks every 4 seconds
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console (dev convenience only): `http://localhost:8080/h2-console` — JDBC URL
  `jdbc:h2:file:./data/ai-city-brain`, user `sa`, empty password

To start completely fresh, stop the app and delete `backend/data/`.

### Running against real PostgreSQL instead

```bash
# Start Postgres only:
docker run -d --name novacity-pg -e POSTGRES_DB=aicitybrain \
  -e POSTGRES_USER=aicitybrain -e POSTGRES_PASSWORD=aicitybrain \
  -p 5432:5432 postgres:16-alpine

cd backend
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run
```

Or just run the whole stack with `docker compose up --build` from the repo root.

## 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on `http://localhost:5173` and expects the backend at `http://localhost:8080`
(override with `frontend/.env` — copy `.env.example` — if different).

## 3. Environment variables

Backend (see `backend/.env.example`):

| Variable | Default | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | `local` (H2) or `docker` (Postgres) |
| `SERVER_PORT` | `8080` | |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | — | Only used by the `docker` profile |
| `JWT_SECRET` | an obvious placeholder | **Change this** outside of local demo use |
| `JWT_EXPIRATION_MINUTES` | `120` | |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Comma-separated |
| `SIM_TICK_INTERVAL_MS` | `4000` | How often the simulation engine ticks |
| `SIM_AUTOSTART` | `true` | Set `false` to start paused |
| `AI_PROVIDER` | `rule-based` | Set `llm` to enable real Claude-generated text |
| `ANTHROPIC_API_KEY` | empty | Required only if `AI_PROVIDER=llm` |
| `ANTHROPIC_MODEL` | `claude-haiku-4-5-20251001` | Any valid Claude model string |

Frontend (see `frontend/.env.example`):

| Variable | Default |
|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` |
| `VITE_WS_BASE_URL` | `ws://localhost:8080` |

## 4. Testing

```bash
cd backend
mvn test
```

- `RiskScoringTest`, `FloodAgentTest`, `CityBrainServiceTest` — pure unit tests
  (JUnit 5 + Mockito), no Spring context, run in milliseconds.
- `AuthControllerIntegrationTest` — full `@SpringBootTest` against the real
  Flyway-seeded in-memory H2 database (`test` profile), exercising login, JWT
  issuance/validation, and role-based access control end to end using the same demo
  accounts the app ships with.
- `AiCityBrainApplicationTests` — context-loads smoke test.

Frontend has no test suite yet (noted as a gap — see the README's future-enhancements
framing); `npm run build` acts as the closest thing to a regression check today since
it runs a full TypeScript compile.

## 5. Demo script (2 minutes)

1. Log in as `admin` (or use the one-click demo picker).
2. Command Center: click **AI Briefing** to generate a natural-language summary.
3. Go to **Simulation**, click **Heavy Rain**. Within a few ticks, watch Riverside
   District's flood-risk metric climb on the Command Center, then check **AI
   Decisions** for the Flood Agent's event and Traffic Agent's road-closure reaction.
4. Go to **City Map** to see the same event reflected geographically (roads turn red,
   zone overlay shifts color).
5. Trigger **Major Accident**, then check **Incidents** — an Emergency Responder or
   Ops Manager account can advance its status through to Resolved.
6. Try **What If…?** with a large rainfall delta and read the generated narrative.
7. Log out and back in as `citizen` to see the read-only, alerts-focused view.

## 6. Troubleshooting

**Backend won't compile.** This project was generated without a live Maven Central
connection in the build sandbox, so the Java code could not be compiled there (see
the README's closing note). Run `mvn clean install` and treat any error as something
to fix — most likely a small import or method-signature mismatch. Please don't
hesitate to report anything that comes up.

**Frontend shows a blank page / network errors.** Confirm the backend is running and
`VITE_API_BASE_URL` in `frontend/.env` points at it; check the browser console for
CORS errors, which usually mean `CORS_ALLOWED_ORIGINS` on the backend doesn't include
your frontend's origin.

**WebSocket shows "Reconnecting…" forever.** Same root cause as above (wrong URL) —
also check that nothing between you and the backend is stripping the
`Upgrade: websocket` header (some corporate proxies do).

**H2 console won't open / "database already in use".** Only one process can hold the
file-based H2 database at a time — stop the running Spring Boot app before opening
the H2 console pointed at the same file, or just use the `test` profile's in-memory
database for exploration.

**Flyway migration checksum mismatch after editing a migration file.** Don't edit
already-applied migration files — add a new `V5__...sql` instead, or delete
`backend/data/` to start over in local dev.
