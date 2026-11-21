# Parcella API

Backend for **Parcella**, a delivery management platform: customers request a delivery between two points, choose a driver, pay through escrow, and confirm with pickup and delivery codes. This is a local-only practice build (no live GPS, no routing engine, no sockets).

The Vue frontend lives in the `parcella-web` repository.

## Stack

- Java 21, Spring Boot 4.1.x (Spring Framework 7), Maven
- PostgreSQL + PostGIS, Flyway migrations
- Spring Security with JWT (OAuth2 resource server, HS256)
- Hibernate Spatial (JTS), Lombok

## Prerequisites

- JDK 21 and Maven
- PostgreSQL (tested with: `<version>`) with PostGIS available (`<version>`)
- Redis is **not** needed yet (planned for a later slice)

## Setup

1. Create the two databases:

```sql
CREATE DATABASE parcella;
CREATE DATABASE parcella_test;
```

   The database user must be allowed to run `CREATE EXTENSION`, because migration V1 enables PostGIS.

2. Copy the environment template and fill it in:

```bash
cp .env.example .env        # PowerShell: Copy-Item .env.example .env
```

| Variable | Purpose |
| --- | --- |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Database location (defaults: localhost, 5432, parcella) |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials (no default password) |
| `JWT_SECRET` | Token signing secret, at least 32 bytes (`openssl rand -base64 48`) |
| `FRONTEND_URL` | Exact origin allowed by CORS, e.g. `http://localhost:5173` (no path, no trailing slash) |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD` | Dev-only admin account created at startup |

Missing secrets fail the startup on purpose. `.env` is gitignored; never commit it.

## Run

```bash
mvn spring-boot:run
```

The API listens on `http://localhost:8080/api/v1`. Check it with:

```bash
curl http://localhost:8080/api/v1/health
```

The response includes the PostGIS version, which proves the database and extension are working. Migrations run automatically on startup.

## Test

```bash
mvn clean verify
```

Tests run under the `test` profile against the `parcella_test` database, using the fixed JWT secret and CORS origin from `application-test.yml`. The database password still comes from your environment.

## Profiles

- `dev` is the default profile. It seeds an admin account from `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD`.
- **Production must set the real environment variable `SPRING_PROFILES_ACTIVE`** (for example `prod`). Otherwise the dev seeder runs.

## API (so far)

Base path `/api/v1`. All ids are UUID strings. Errors follow RFC 9457 (`application/problem+json`); validation failures are `422` with an `errors` map.

| Method | Path | Access |
| --- | --- | --- |
| GET | `/health` | public |
| POST | `/auth/register` | public |
| POST | `/auth/login` | public |
| GET | `/me` | any authenticated user |
| PATCH | `/me` | any authenticated user |

Send `Authorization: Bearer <accessToken>` on authenticated calls. Full request and response shapes are in `docs/api/slice-2.md`.

## Project layout

```
src/main/java/com/carmineabk/parcella/
├── config/      security, JWT, CORS, dev seeder
├── security/    JWT properties, token service, security error handler
├── web/v1/      controllers and DTOs
├── service/     business logic (transactions live here)
├── repository/  Spring Data repositories
├── domain/      entities and enums
├── exception/   global error handling
└── util/
src/main/resources/
├── application.yml, application-dev.yml, application-test.yml
└── db/migration/   Flyway versioned SQL (the only schema source)
```

## Conventions

- Schema changes only through Flyway migrations (`ddl-auto: validate`); one migration per slice.
- Enums are stored as strings. Money is whole-number XAF.
- Controllers stay thin; DTOs are mapped inside service transactions.
- Handoff notes and API contracts per slice live in `docs/`.
