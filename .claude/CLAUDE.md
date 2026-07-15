# .claude directory

Local Claude Code configuration for this repo, separate from the
project-level `/CLAUDE.md` (which documents the codebase itself for any
contributor/agent).

- `rules/` — narrow, topic-specific rule files that supplement the root
  `CLAUDE.md`, each scoped to one concern and cross-referencing the matching
  `docs/` file for full detail:
  - `architecture.md` — module dependency direction, package boundaries.
  - `coding-style.md` — Lombok/MapStruct, `ApiResponse` envelope, exception
    handling, package layout.
  - `security.md` — secrets handling, `security-api`'s current auth model
    (what's in vs. removed).
  - `testing.md` — JUnit 5, `./gradlew build` vs. `test`, CI parity.
  - `git.md` — commit rules, mid-refactor `main` caution, destructive ops.
  - `review.md` — pre-review checklist, `/code-review` skill usage.
- `prompts/` — reusable prompt templates for recurring task types, each with
  a fill-in-the-blank body that bakes in the relevant `.claude/rules/*`
  constraints:
  - `new-feature.md`, `bug-fix.md`, `refactor.md` — the three general
    change shapes.
  - `api.md` — adding/changing a REST endpoint specifically.
  - `migration.md` — JPA entity/schema changes via a new Flyway migration
    file (see `docs/database.md` — ddl-auto=validate, Flyway is the schema
    authority).
  - `review.md` — requesting a review against the same checklist as
    `.claude/rules/review.md`.
  - `documentation.md` — which `docs/*` files to check when code changes.
- `context/` — supplementary background material that's useful for an agent
  working in this repo but is more narrative/investigative than the
  user-facing `docs/`:
  - `project.md` — what AuthHub is, where it stands right now, deliberate
    rough edges not to "fix" opportunistically.
  - `modules.md` — per-module package breakdown beyond
    `docs/architecture.md`'s summary.
  - `database.md` — Flyway as schema authority, `ddl-auto` per profile, the
    `test` profile's `create-drop` gap (migrations aren't exercised by CI).
  - `api-style.md` — how the `ApiResponse`/`StatusCode`/`ExitCode` envelope
    actually behaves in code, including known quirks (hardcoded status
    codes in `BaseController`/`GlobalExceptionHandler`).
  - `roadmap.md` — condensed `ROADMAP.md`: done vs. deliberately deferred
    vs. out of scope.
  - `tech-stack.md` — dependency versions pulled from the root
    `build.gradle` — re-verify before relying on a version number from here.
- `settings.local.json` — local, untracked-in-spirit Claude Code settings
  (permissions, hooks) for this machine/user.
