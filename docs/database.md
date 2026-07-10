# Database

- PostgreSQL, DB name `jwt_auth`, default local creds `postgres`/`123`.
- `spring.jpa.hibernate.ddl-auto: update` — schema auto-syncs in dev, no manual migrations yet.
- `jwt.secret` is hard-coded in `application.yml` for local dev; externalize via `JWT_SECRET` env var for anything beyond local — never commit a real secret.
