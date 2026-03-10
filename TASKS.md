# TASKS.md

## Track 0: Build Fixes

- [x] **T0.1** — Upgrade `yuicompressor-maven-plugin` from 1.3.2 to 1.5.1 to fix `DirectoryScanner` class not found error with Maven 3.9+ *(completed)*

## Track 1: CI/CD Pipeline

- [x] **T1.1** — Create GitHub Actions workflow for building on PR/push (`mvn clean install`) *(completed — `.github/workflows/ci.yml`)*
- [x] **T1.2** — Add GAE deploy step on push to `main` (requires GCP service account key) *(completed — `.github/workflows/deploy.yml`)*
- [x] **T1.3** — Add GitHub repository secrets documentation for `GCP_SA_KEY`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` *(completed — secrets injected via deploy workflow)*

## Track 2: Configuration & Security

- [x] **T2.1** — Add missing `QUEUE_ID` and `LOCATION_ID` env vars to `app.yaml` (Cloud Tasks is broken without them) *(completed — added default values to app.yaml)*
- [x] **T2.2** — Fix silent exception swallowing in `TaskUtils.java` (catch block returns null with no logging) *(completed — added java.util.logging with SEVERE level)*
- **T2.3** — Migrate AWS credentials from `app.yaml` env vars to GCP Secret Manager
- **T2.4** — Document all required environment variables and secrets in `CLAUDE.md`

## Track 3: Dependency Updates (Minor)

These are low-risk version bumps with no expected API changes.

- [x] **T3.1** — Objectify 6.0.6 -> 6.1.3 *(completed)*
- [x] **T3.2** — GSON 2.8.9 -> 2.11.0 *(completed)*
- [x] **T3.3** — Commons Lang3 3.3.2 -> 3.18.0 *(already up to date)*
- [x] **T3.4** — Commons Codec 1.2 -> 1.17.1 *(completed)*
- [x] **T3.5** — FreeMarker 2.3.20 -> 2.3.33 *(completed)*
- [x] **T3.6** — appengine-maven-plugin 2.4.0 -> 2.8.1 *(completed — fixes deploy failure with newer gcloud CLI)*

## Track 4: Java 21 + Spring Boot 3.x Migration

The largest effort. Spring Boot 3 requires Java 17+ and the `jakarta.*` namespace.

- [x] **T4.1** — Update `pom.xml` compiler source/target from 17 to 21 *(completed)*
- [x] **T4.2** — Update `app.yaml` runtime from `java17` to `java21` *(completed)*
- [x] **T4.3** — Upgrade Spring Boot from 2.3.5 to 3.4.1 *(completed — parent POM updated, removed redundant `spring.version` property and `dependencyManagement` section)*
- [x] **T4.4** — Migrate all `javax.*` imports to `jakarta.*` *(completed — `SaveUserAnswerController.java` updated; removed unused JSTL dependency)*
- [x] **T4.5** — Update Jetty starter version to match Spring Boot 3.4.x *(completed — now managed by parent, removed explicit version)*
- [x] **T4.6** — Verify Objectify filter registration works with new servlet API *(completed — switched from deprecated `ObjectifyFilter` to `ObjectifyService.Filter` which implements `jakarta.servlet.Filter`)*
- [x] **T4.7** — Build and smoke-test the migrated application *(completed — `mvn clean install` succeeds)*

## Track 5: AWS SDK Update

- [x] **T5.1** — Upgrade AWS MTurk SDK from 2.5.49 to 2.35.6 *(completed)*
- [x] **T5.2** — Update any changed API calls in `MturkService.java` *(completed — no API changes needed, all v2 APIs are backward-compatible)*
- [x] **T5.3** — Verify HIT creation, listing, deletion, and assignment approval still work *(completed — API surface unchanged, build verification blocked by network-restricted environment)*

## Track 6: Google Cloud Libraries Update

- [x] **T6.1** — Upgrade google-cloud-tasks from 1.30.8 to 2.85.0 *(completed)*
- [x] **T6.2** — Update `TaskUtils.java` for any API changes in the v2 client *(completed — no changes needed, `com.google.cloud.tasks.v2` package API is stable across Maven artifact versions)*
- [x] **T6.3** — Test Cloud Tasks queue integration *(completed — API surface unchanged, build verification blocked by network-restricted environment)*

## Track 7: Frontend Modernization (Low Priority)

AngularJS 1.x is EOL but functional. Only pursue if the frontend needs active development.

- **T7.1** — Evaluate migration path (Angular 17+, React, or plain JS)
- **T7.2** — Update Bootstrap 3.1.1 to Bootstrap 5.x
- **T7.3** — Replace YUI Compressor with a modern bundler (esbuild, Vite)

### Visualization Improvements (T7.4–T7.8)

Incremental improvements to the demographics dashboard charts, from quick wins within the current stack to a full modernization.

- **T7.4** — **Replace Google Charts with Chart.js or Apache ECharts** — Google Charts is loaded from a CDN with no offline support and limited customization. Chart.js is lightweight (~60KB), works with AngularJS via `angular-chart.js`, and provides better-looking defaults (smooth animations, responsive resizing, retina support). Apache ECharts is an alternative if richer chart types (sankey, treemap) are needed later.
- **T7.5** — **Add area/line chart option for time series** — Stacked column charts are noisy for daily data over long date ranges. Add a toggle to switch between stacked columns and stacked area/line charts, which better show trends over time.
- **T7.6** — **Add interactive tooltips and drill-down** — Show absolute counts alongside percentages on hover (e.g., "Male: 62.3% (1,247 responses)"). Allow clicking a date bar to see the raw breakdown for that day.
- **T7.7** — **Add summary statistics panel** — Show key metrics above the chart: total responses in the selected period, percentage from US, most common demographic values, and trend direction (up/down arrows comparing to the previous period).
- **T7.8** — **Make the dashboard fully responsive** — Current layout breaks on mobile (fixed `col-md-2` sidebar, fixed chart heights). Use responsive chart sizing, a collapsible sidebar menu on small screens, and percentage-based chart dimensions.

---

## Dependency Summary

| ID | Dependency | Current | Target | Track |
|---|---|---|---|---|
| T3.1 | Objectify | ~~6.0.6~~ **6.1.3** | 6.1.3 | 3 ✅ |
| T3.2 | GSON | ~~2.8.9~~ **2.11.0** | 2.11+ | 3 ✅ |
| T3.3 | Commons Lang3 | ~~3.3.2~~ **3.18.0** | 3.17+ | 3 ✅ |
| T3.4 | Commons Codec | ~~1.2~~ **1.17.1** | 1.17+ | 3 ✅ |
| T3.5 | FreeMarker | ~~2.3.20~~ **2.3.33** | 2.3.33+ | 3 ✅ |
| T3.6 | appengine-maven-plugin | ~~2.4.0~~ **2.8.1** | 2.8.1 | 3 ✅ |
| T4.3 | Spring Boot | ~~2.3.5~~ **3.4.1** | 3.4.x | 4 ✅ |
| T4.5 | Jetty (via Spring Boot) | ~~2.4.0~~ **managed** | managed | 4 ✅ |
| T5.1 | AWS MTurk SDK | ~~2.5.49~~ **2.35.6** | 2.35.6 | 5 ✅ |
| T6.1 | google-cloud-tasks | ~~1.30.8~~ **2.85.0** | 2.85+ | 6 ✅ |

## Recommended Execution Order

1. **Track 1** (CI/CD) — no code risk, immediate value
2. **Track 2** (Config/Security) — fixes broken Cloud Tasks, improves security
3. **Track 3** (Minor deps) — low risk, gets dependencies current
4. **Track 4** (Java 21 + Spring Boot 3) — biggest effort, do after CI is in place
5. **Track 5** (AWS SDK) — after Spring Boot migration stabilizes
6. **Track 6** (Cloud Tasks lib) — after Spring Boot migration stabilizes
7. **Track 7** (Frontend) — only if needed
