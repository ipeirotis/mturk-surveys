# TASKS.md

## Track 0: Build Fixes

- [x] **T0.1** — Upgrade `yuicompressor-maven-plugin` from 1.3.2 to 1.5.1 to fix `DirectoryScanner` class not found error with Maven 3.9+ *(completed)*

## Track 1: CI/CD Pipeline

- [x] **T1.1** — Create GitHub Actions workflow for building on PR/push (`mvn clean install`) *(completed — `.github/workflows/ci.yml`)*
- [x] **T1.2** — Add GAE deploy step on push to `main` (requires GCP service account key) *(completed — `.github/workflows/deploy.yml`)*
- [x] **T1.3** — Add GitHub repository secrets documentation for `GCP_SA_KEY`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` *(completed — secrets injected via deploy workflow)*

## Track 2: Configuration & Security

- **T2.1** — Add missing `QUEUE_ID` and `LOCATION_ID` env vars to `app.yaml` (Cloud Tasks is broken without them)
- **T2.2** — Fix silent exception swallowing in `TaskUtils.java` (catch block returns null with no logging)
- **T2.3** — Migrate AWS credentials from `app.yaml` env vars to GCP Secret Manager
- **T2.4** — Document all required environment variables and secrets in `CLAUDE.md`

## Track 3: Dependency Updates (Minor)

These are low-risk version bumps with no expected API changes.

- **T3.1** — Objectify 6.0.6 -> 6.1.3
- **T3.2** — GSON 2.8.6 -> 2.11+
- **T3.3** — Commons Lang3 3.3.2 -> 3.17+
- **T3.4** — Commons Codec 1.2 -> 1.17+
- **T3.5** — FreeMarker 2.3.20 -> 2.3.33+
- [x] **T3.6** — appengine-maven-plugin 2.4.0 -> 2.8.1 *(completed — fixes deploy failure with newer gcloud CLI)*

## Track 4: Java 21 + Spring Boot 3.x Migration

The largest effort. Spring Boot 3 requires Java 17+ and the `jakarta.*` namespace.

- **T4.1** — Update `pom.xml` compiler source/target from 11 to 21
- **T4.2** — Update `app.yaml` runtime from `java11` to `java21`
- **T4.3** — Upgrade Spring Boot from 2.3.5 to 3.4.x
- **T4.4** — Migrate all `javax.*` imports to `jakarta.*` (servlet API, JSTL, etc.)
- **T4.5** — Update Jetty starter version to match Spring Boot 3.4.x
- **T4.6** — Verify Objectify filter registration works with new servlet API
- **T4.7** — Build and smoke-test the migrated application

## Track 5: AWS SDK Update

- **T5.1** — Upgrade AWS MTurk SDK from 2.5.49 to 2.34.x
- **T5.2** — Update any changed API calls in `MturkService.java`
- **T5.3** — Verify HIT creation, listing, deletion, and assignment approval still work

## Track 6: Google Cloud Libraries Update

- **T6.1** — Upgrade google-cloud-tasks from 1.30.8 to 2.x
- **T6.2** — Update `TaskUtils.java` for any API changes in the v2 client
- **T6.3** — Test Cloud Tasks queue integration

## Track 7: Frontend Modernization (Low Priority)

AngularJS 1.x is EOL but functional. Only pursue if the frontend needs active development.

- **T7.1** — Evaluate migration path (Angular 17+, React, or plain JS)
- **T7.2** — Update Bootstrap 3.1.1 to Bootstrap 5.x
- **T7.3** — Replace YUI Compressor with a modern bundler (esbuild, Vite)

---

## Dependency Summary

| ID | Dependency | Current | Target | Track |
|---|---|---|---|---|
| T3.1 | Objectify | 6.0.6 | 6.1.3 | 3 |
| T3.2 | GSON | 2.8.6 | 2.11+ | 3 |
| T3.3 | Commons Lang3 | 3.3.2 | 3.17+ | 3 |
| T3.4 | Commons Codec | 1.2 | 1.17+ | 3 |
| T3.5 | FreeMarker | 2.3.20 | 2.3.33+ | 3 |
| T3.6 | appengine-maven-plugin | ~~2.4.0~~ **2.8.1** | 2.8.1 | 3 ✅ |
| T4.3 | Spring Boot | 2.3.5 | 3.4.x | 4 |
| T4.5 | Jetty (via Spring Boot) | 2.4.0 | managed | 4 |
| T5.1 | AWS MTurk SDK | 2.5.49 | 2.34.x | 5 |
| T6.1 | google-cloud-tasks | 1.30.8 | 2.85+ | 6 |

## Recommended Execution Order

1. **Track 1** (CI/CD) — no code risk, immediate value
2. **Track 2** (Config/Security) — fixes broken Cloud Tasks, improves security
3. **Track 3** (Minor deps) — low risk, gets dependencies current
4. **Track 4** (Java 21 + Spring Boot 3) — biggest effort, do after CI is in place
5. **Track 5** (AWS SDK) — after Spring Boot migration stabilizes
6. **Track 6** (Cloud Tasks lib) — after Spring Boot migration stabilizes
7. **Track 7** (Frontend) — only if needed
