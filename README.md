# AuthHub

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

### Environment variables (optional but recommended)

The configs default to placeholders; override these for real email and a secure
JWT secret:

```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-gmail-app-password"
export JWT_SECRET="<base64-64-byte-secret>"
```

> ⚠️ `security-api` currently has a hard-coded JWT secret in `application.yml`
> for local dev. For anything beyond local dev, move it to the `JWT_SECRET`
> env var and never commit a real secret.

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
├── build.gradle           # Root: plugins, shared deps, per-module wiring
├── settings.gradle        # Declares the 3 modules
├── gradlew / gradlew.bat  # Gradle wrapper
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

Per-module config lives in `src/main/resources/application.yml`. Key settings
(shared shape across modules):

| Key | Default | Notes |
|-----|---------|-------|
| `server.port` | 8080 (`security-api`) / 8082 (`todoapi`) | One per runnable module |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/jwt_auth` | Point at your DB |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-syncs schema in dev, no manual migrations yet |
| `jwt.secret` | (hard-coded key) | **Externalize via `JWT_SECRET` beyond local dev** |
| `jwt.expiration` | `PT24H` | Access-token lifetime (ISO-8601 duration) |
| `jwt.refresh-token.expiration` | `P7D` | Refresh-token lifetime |
| `app.frontend-url` | `http://localhost:3000` | Used for CORS / reset links |
| `spring.mail.*` | Gmail SMTP | Needs `MAIL_USERNAME` / `MAIL_PASSWORD` |

Google login is verified via ID token (`security/oauth/GoogleTokenVerifier`) —
there is no cookie-based OAuth2 redirect flow, and the old custom OTP feature has
been removed. See `docs/security-api-state.md` for the current state of this module.

---

## Legacy

`legacy/spring-jwt-auth/` is an earlier standalone JWT-auth practice project,
kept for reference. It is **not** wired into the Gradle build (not in
`settings.gradle`) and is superseded by `security-api`. See its own README.
