# AuthHub

[![CI](https://github.com/Hen-Heang/authhub/actions/workflows/ci.yml/badge.svg)](https://github.com/Hen-Heang/authhub/actions/workflows/ci.yml)

A multi-module **Spring Boot 3.5** monorepo for authentication and a sample
business API. It provides JWT-based authentication (with refresh + revocation),
Google ID-token login, MFA (TOTP), password reset, audit logging, and rate
limiting — exposed as reusable modules that other services build on top of.

- **Group:** `com.henheang`
- **Java:** 17 (set in the root `build.gradle`)
- **Build tool:** Gradle (wrapper included — `./gradlew`)
- **Spring Boot:** 3.5.15
- **Database:** PostgreSQL

---

## Module Architecture

AuthHub is a Gradle multi-project build with three modules. Dependencies flow in
one direction only (no cycles):

```
common-api   (base library — shared utilities, no app of its own)
    ▲
    │
security-api (authentication service — JWT, Google login, MFA, audit)  ── runnable
    ▲
    │
todoapi      (sample business API protected by security-api)           ── runnable
```

| Module         | Port  | bootJar | Purpose |
|----------------|-------|---------|---------|
| `common-api`   | —     | ❌ off  | Shared library: API response envelopes (`ApiResponse`, `ApiStatus`, `StatusCode`), pagination, enum converters, interceptors. Built as a plain `jar` and consumed by the other modules — not run directly. |
| `security-api` | 8080  | ✅ on   | Core authentication service: signup/login, JWT issue/refresh/revocation, Google ID-token login, MFA, password reset, user management, audit logging, rate limiting, Swagger UI. |
| `todoapi`      | 8082  | ✅ on   | Example business API (to-do lists/items) that depends on `security-api` for authentication. |

Dependency wiring lives in the **root `build.gradle`** (`subprojects { ... }`
plus per-project blocks), so most dependencies are declared once at the top.

---

## Prerequisites

1. **JDK 17** (the build targets Java 17).
2. **PostgreSQL** running locally on port `5432` with a database named `jwt_auth`.
   - Default credentials in the configs: user `postgres`, password `123`.
   - Schema is auto-created/updated on startup (`spring.jpa.hibernate.ddl-auto: update`).
3. *(Optional)* Gmail SMTP credentials for email-based features (password reset).

### Create the database

```bash
createdb -U postgres jwt_auth
# or inside psql:  CREATE DATABASE jwt_auth;
```

### Configuration profiles

Each runnable module (`common-api`'s context, `security-api`, `todoapi`) loads
`application.yml` plus a profile file selected by `SPRING_PROFILES_ACTIVE`
(Spring's standard `application-{profile}.yml` convention):

| Profile | File | Used by | Secrets |
|---------|------|---------|---------|
| `local` (default) | `application-local.yml` | `./gradlew bootRun` with no env vars | Has fallback defaults (DB password `123`, placeholder JWT secret) for zero-config local dev. **Git-ignored** — copy `.env.example` and fill in your own values instead of relying on the placeholder. |
| `dev` | `application-dev.yml` | Shared dev/staging | Requires real `DB_PASSWORD` / `JWT_SECRET` env vars, no fallback. |
| `test` | `application-test.yml` | `./gradlew test` / CI (forced via the root `build.gradle` `Test` task config) | Separate `jwt_auth_test` DB, `ddl-auto: create-drop`, a fixed non-sensitive JWT secret default (safe — test-only). |
| `prod` | `application-prod.yml` | Production | No fallback defaults anywhere — fails fast at startup if `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `JWT_SECRET` aren't set. `ddl-auto: validate` (no auto schema changes). |

Copy `.env.example` to `.env` (or export the vars directly) and set:

```bash
export SPRING_PROFILES_ACTIVE="local"   # local | dev | test | prod
export DB_PASSWORD="..."
export JWT_SECRET="<base64 64-byte secret>"   # generate via JwtSecretGenerator#main
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-gmail-app-password"
```

> ⚠️ No `application*.yml` file commits a real secret anymore. See
> [SECURITY.md](SECURITY.md) for the full policy and how to rotate the
> secret that *was* previously committed in `security-api`'s local config.

---

## Build

From the `AuthHub/` directory:

```bash
# Build everything (compiles, runs tests, produces jars)
./gradlew build

# Build a single module
./gradlew :security-api:build
./gradlew :todoapi:build

# Skip tests
./gradlew build -x test
```

### Code quality tooling

Wired into every module via the root `build.gradle`:

| Tool | Task | Enforced? |
|------|------|-----------|
| **Spotless** (Google Java Format) | `spotlessCheck` / `spotlessApply` | Yes — part of `check`/`build`. Run `./gradlew spotlessApply` to auto-format. |
| **Checkstyle** | `checkstyleMain` / `checkstyleTest` | Advisory — reports only (`ignoreFailures = true`) until the existing codebase is brought into compliance. Rules in `config/checkstyle/checkstyle.xml`. |
| **PMD** | `pmdMain` / `pmdTest` | Advisory — same reasoning. Rules in `config/pmd/ruleset.xml`. |
| **JaCoCo** | `jacocoTestReport` | Runs after every `test` task; reports under `*/build/reports/jacoco/`. No minimum-coverage gate yet. |

See `docs/coding-standards.md` before tightening any of these to fail the build.

## Run

Each runnable module is started with the Spring Boot plugin:

```bash
# Start the auth service (port 8080)
./gradlew :security-api:bootRun

# Start the todo API (port 8082) — needs security-api's JWT to authenticate
./gradlew :todoapi:bootRun
```

`common-api` is a library (`bootJar` disabled) and is **not** run directly — it
is pulled in as a dependency by the other two.

Once `security-api` is up, open the API docs:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health/ping:** http://localhost:8080/api/public/ping

---

## API Overview (`security-api`, port 8080)

### Authentication — `/api/auth`
| Method | Path                          | Description |
|--------|-------------------------------|-------------|
| POST   | `/api/auth/signup`            | Register a new user |
| POST   | `/api/auth/login`              | Authenticate, returns access + refresh tokens |
| POST   | `/api/auth/oauth2/google`      | Log in with a Google ID token |
| POST   | `/api/auth/refresh`            | Exchange a refresh token for a new access token |
| POST   | `/api/auth/logout`             | Revoke a refresh token |
| GET    | `/api/auth/user`               | Get the current authenticated user |
| POST   | `/api/auth/forgot-password`    | Start password reset (sends email token) |
| GET    | `/api/auth/reset-password?token=` | Validate a reset token |
| POST   | `/api/auth/reset-password`     | Set a new password |

### MFA — `/api/auth/mfa`
| Method | Path             | Description |
|--------|------------------|-------------|
| POST   | `/api/auth/mfa/setup`   | Generate a new TOTP secret/QR for the current user |
| POST   | `/api/auth/mfa/enable`  | Confirm a TOTP code and enable MFA |
| POST   | `/api/auth/mfa/disable` | Disable MFA for the current user |
| POST   | `/api/auth/mfa/verify`  | Verify a TOTP code during login |

### Users — `/api/users`
| Method | Path               | Description |
|--------|--------------------|-------------|
| GET    | `/api/users`       | List users |
| PATCH  | `/api/users/{id}`  | Update a user |
| DELETE | `/api/users/{id}`  | Delete a user |

### Audit logs — `/api/admin/audit-logs`
| Method | Path                              | Description |
|--------|------------------------------------|-------------|
| GET    | `/api/admin/audit-logs`            | List audit events (paginated) |
| GET    | `/api/admin/audit-logs/user/{userId}` | List audit events for a specific user (paginated) |

### Public — `/api/public`
| Method | Path               | Description |
|--------|--------------------|-------------|
| GET    | `/api/public/ping` | Unauthenticated health check |

Requests are also subject to `RateLimitingFilter`, and access/refresh tokens can
be revoked via `TokenBlacklistService` (backed by `RevokedToken`).

### Todo API (`todoapi`, port 8082)
| Method | Path                   | Description |
|--------|------------------------|-------------|
| POST   | `/api/todo/v1/create`  | Create a todo list for the authenticated user |

---

## Project Layout

```
AuthHub/
├── build.gradle           # Root: plugins, shared deps, per-module wiring, quality tooling
├── settings.gradle        # Declares the 3 modules
├── gradlew / gradlew.bat  # Gradle wrapper
├── config/checkstyle/, config/pmd/  # Checkstyle & PMD rule files (see build.gradle)
├── .github/workflows/ci.yml         # CI: build + test + lint reports on push/PR
├── .env.example            # Template for local env vars — copy to .env
├── ROADMAP.md / CHANGELOG.md / SECURITY.md / CONTRIBUTING.md / LICENSE
│
├── common-api/            # Shared library (no bootJar)
│   └── src/main/java/com/henheang/commonapi/
│       └── components/     # ApiResponse, Pagination, enum converters, interceptor
│
├── security-api/          # Auth service (port 8080)
│   └── src/main/java/com/henheang/securityapi/
│       ├── config/         # WebSecurityConfig, CorsConfig, JwtConfig, OpenApiConfig,
│       │                   #   DataInitializer, ScheduledTasks
│       ├── controller/     # AuthController, UserController, AuditController,
│       │                   #   MfaController, PublicController, SwaggerController
│       ├── domain/         # User, Role, RefreshToken, RevokedToken, PasswordResetToken,
│       │                   #   AuditEvent, AuditEventType, AuthProvider
│       ├── repository/     # Spring Data JPA repositories
│       ├── security/       # JWT filter/provider, RateLimitingFilter, UserPrincipal,
│       │                   #   oauth/GoogleTokenVerifier (ID-token verification)
│       ├── service/ (+impl)# AuthService, UserService, MfaService, AuditLogService,
│       │                   #   TokenBlacklistService, RefreshTokenService, EmailService, ...
│       ├── payload/        # Request/response DTOs (+ MFA request/response types)
│       ├── exception/      # GlobalExceptionHandler + custom exceptions
│       ├── validation/     # @ValidIdentifier custom constraint
│       └── utils/          # JwtSecretGenerator, PhoneNumberUtil
│
├── todoapi/               # Sample business API (port 8082)
│   └── src/main/java/com/test/todoapi/
│       ├── controller/  service/  repository/
│       ├── domain/       # TodoList, TodoItem, Tag, ListShare, TodoComment, ...
│       ├── payload/  enums/  util/
│
└── legacy/spring-jwt-auth/ # Archived, not part of the Gradle build (see its README)
```

---

## Development Workflow

1. **Start dependencies** — make sure PostgreSQL is running and `jwt_auth` exists.
2. **Make changes** — shared code goes in `common-api`; auth logic in `security-api`;
   business features in `todoapi`. Keep the dependency direction
   (`todoapi → security-api → common-api`) intact to avoid build cycles.
3. **Run tests** — `./gradlew test` (or per module, e.g. `./gradlew :security-api:test`).
   Tests use JUnit 5 (`useJUnitPlatform()`).
4. **Run locally** — `./gradlew :security-api:bootRun`, then exercise endpoints via
   Swagger UI or an HTTP client. Start `todoapi` too if you need the business API.
5. **Verify the build** — `./gradlew build` before committing.

### Conventions
- **Lombok** is enabled across all modules — use it for boilerplate (getters,
  builders, constructors).
- **MapStruct** is used for DTO ↔ entity mapping in `common-api` and `security-api`.
- API responses are wrapped using the envelope types in
  `common-api` (`ApiResponse` / `ApiStatus` / `StatusCode`) for a consistent shape.
- Exceptions are translated centrally by `GlobalExceptionHandler` in `security-api`.

---

## Configuration Reference

Per-module config lives in `src/main/resources/application.yml` (profile-agnostic
base) plus `application-{local,dev,test,prod}.yml` (see
[Configuration profiles](#configuration-profiles) above). Key settings (shared
shape across modules):

| Key | Source | Notes |
|-----|--------|-------|
| `server.port` | `${SERVER_PORT}` | Defaults: 8081 (`common-api`, not run directly), 8080 (`security-api`), 8082 (`todoapi`) |
| `spring.datasource.url` | `${DB_URL}` | Defaults to `jdbc:postgresql://localhost:5432/jwt_auth` (`jwt_auth_test` under the `test` profile) |
| `spring.jpa.hibernate.ddl-auto` | per profile | `update` (local/dev), `create-drop` (test), `validate` (prod — no auto schema changes) |
| `jwt.secret` | `${JWT_SECRET}` | No fallback default outside `local`/`test`. **Required env var in `dev`/`prod`.** |
| `jwt.expiration` | `PT24H` | Access-token lifetime (ISO-8601 duration) |
| `jwt.refresh-token.expiration` | `P7D` | Refresh-token lifetime |
| `app.frontend-url` | `${FRONTEND_URL}` | Used for CORS / reset links |
| `spring.mail.*` | `${MAIL_USERNAME}` / `${MAIL_PASSWORD}` | Gmail SMTP |

Google login is verified via ID token (`security/oauth/GoogleTokenVerifier`) —
there is no cookie-based OAuth2 redirect flow, and the old custom OTP feature has
been removed. See `docs/security-api-state.md` for the current state of this module.

---

## Legacy

`legacy/spring-jwt-auth/` is an earlier standalone JWT-auth practice project,
kept for reference. It is **not** wired into the Gradle build (not in
`settings.gradle`) and is superseded by `security-api`. See its own README.
