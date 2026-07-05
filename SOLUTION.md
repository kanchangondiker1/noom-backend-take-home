# Sleep Logger API — Solution

An implementation of the Noom backend take-home: a REST API for logging a night's sleep,
fetching the latest log, and viewing multi-day averages.

## Tech choices

- **Java 17 + Spring Boot 2.7**. The template shipped a small Kotlin bootstrap; it has been
  ported to Java and the Kotlin toolchain removed, so this is a single-language Java module.
- **PostgreSQL** with **Flyway** migrations.
- **Spring JDBC** (`NamedParameterJdbcTemplate`) — the data access already wired up by the
  template — rather than JPA, keeping SQL explicit and the mapping obvious.
- **JUnit 5 + Mockito + AssertJ** for unit tests, **Testcontainers** for the repository test.

## Architecture

Layered, with the domain rules independent of the web and persistence layers:

```
web  (SleepLogController, DTOs, GlobalExceptionHandler)
  └─ service  (SleepLogService, SleepAverageCalculator)   <- business logic
       └─ repository  (SleepLogRepository / JdbcSleepLogRepository, UserRepository)
            └─ PostgreSQL (Flyway-managed schema)
```

- `SleepLogService` enforces the domain rules: known user, strictly positive time-in-bed
  interval, one log per night, and the date being stamped server-side as "today".
- `SleepAverageCalculator` is pure (no Spring/DB) so it is trivially unit-testable. Averaging
  clock times uses a **circular mean** so that, e.g., bedtimes of 23:00 and 01:00 average to
  00:00 rather than 12:00.

## Users

Auth is out of scope, but the API is user-aware: every request carries an **`X-User-Id`**
header (a UUID). A demo user `11111111-1111-1111-1111-111111111111` is seeded by Flyway
(`V4.0__seed_demo_user.sql`) so the API can be exercised immediately.

## Data model

`users(id, username, created_at)` and
`sleep_log(id, user_id → users, sleep_date, bed_time, wake_time, total_time_in_bed_minutes,
morning_feeling, created_at)` with:

- `UNIQUE(user_id, sleep_date)` — one log per night.
- `CHECK (wake_time > bed_time)` and `CHECK (total_time_in_bed_minutes > 0)`.
- `CHECK (morning_feeling IN ('BAD','OK','GOOD'))`.
- index `(user_id, sleep_date DESC)` for the "latest" and range queries.

`total_time_in_bed_minutes` is derived from the interval and denormalized to keep reads and
average queries simple.

## API

Base path `/api/sleep-logs`. All endpoints require the `X-User-Id` header.

| Method | Path                         | Purpose                        | Success |
|--------|------------------------------|--------------------------------|---------|
| POST   | `/api/sleep-logs`            | Create last night's log (#1)   | 201     |
| GET    | `/api/sleep-logs/last-night` | Fetch last night's log (#2)    | 200     |
| GET    | `/api/sleep-logs/averages`   | Averages, `?days=` (default 30, #3) | 200 |

### Create request
```json
{ "bedTime": "2024-03-14T23:15:00", "wakeTime": "2024-03-15T07:00:00", "feeling": "GOOD" }
```
The sleep date is not accepted from the client — a log always means "last night", so the
server stamps it with today's date.

### Averages response
```json
{
  "rangeStart": "2024-02-15",
  "rangeEnd": "2024-03-15",
  "sampleSize": 12,
  "averageTotalTimeInBedMinutes": 465,
  "averageTotalTimeInBed": "7h 45m",
  "averageBedTime": "23:05:00",
  "averageWakeTime": "06:50:00",
  "feelingFrequencies": { "BAD": 1, "OK": 5, "GOOD": 6 }
}
```

### Errors
Consistent JSON error body. `400` (validation / bad enum / missing header / bad UUID),
`404` (unknown user or no logs), `409` (a log already exists for today).

## Running

```bash
docker-compose up --build     # API on :8080, Postgres on :5432 (Flyway runs on startup)
```

Smoke-test everything:
```bash
./scripts/test-api.sh
```
Or import `postman/Sleep API.postman_collection.json`.

## Tests

```bash
cd sleep
./gradlew test               # unit tests (calculator + service + context load); no Docker needed
./gradlew integrationTest    # repository test against a real Postgres via Testcontainers (needs Docker)
```

The Docker image build runs only the unit tests (`test`), so `docker-compose up --build`
never depends on a Docker-in-Docker daemon; the integration test is opt-in.

## Notes / trade-offs

- The 30-day window is the inclusive range `[today-29, today]`; `days` is configurable.
- Timestamps are stored as local `TIMESTAMP` (no timezone), matching the assignment's
  simplicity; a production version would likely store instants + the user's timezone.
- One log per night is enforced; an "update today's log" (upsert) flow could be added if the
  product wants edits.
