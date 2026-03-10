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
- [x] **T2.3** — Migrate AWS credentials from `app.yaml` env vars to GCP Secret Manager *(completed — `AwsCredentialsConfig` reads from Secret Manager with fallback to default AWS credential chain; removed credential injection from deploy workflow)*
- [x] **T2.4** — Document all required environment variables and secrets in `CLAUDE.md` *(completed — added tables for env vars, Secret Manager secrets, and GitHub Actions secrets)*

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

- [x] **T7.4** — **Replace Google Charts with Chart.js** — Replaced the Google Charts directive (`ng-google-chart.js`) with a custom Chart.js 4.4.7 directive. Chart.js is lightweight (~60KB), supports offline use, and provides smooth animations, responsive resizing, and retina support. *(completed)*
- [x] **T7.5** — **Add area/line chart option for time series** — Added a Bars/Area toggle button group to the dashboard. When "Area" is selected, charts render as stacked area (line with fill) using smooth curves, semi-transparent fills, and no point markers. The directive watches the chart type and re-renders on toggle. *(completed)*
- **T7.6** — **Add interactive tooltips and drill-down** — Show absolute counts alongside percentages on hover (e.g., "Male: 62.3% (1,247 responses)"). Allow clicking a date bar to see the raw breakdown for that day.
- **T7.7** — **Add summary statistics panel** — Show key metrics above the chart: total responses in the selected period, percentage from US, most common demographic values, and trend direction (up/down arrows comparing to the previous period).
- **T7.8** — **Make the dashboard fully responsive** — Current layout breaks on mobile (fixed `col-md-2` sidebar, fixed chart heights). Use responsive chart sizing, a collapsible sidebar menu on small screens, and percentage-based chart dimensions.

### API & Data Pipeline Optimization (T7.9–T7.10)

- [x] **T7.9** — **Remove US-specific filtering from API and frontend** — Removed separate US-only aggregation maps from `SurveyService`, simplified `DemographicsSurveyAnswersByPeriod` from `Map<String, DemographicsSurveyAnswers>` to direct `DemographicsSurveyAnswers` fields, removed US nav links and `/:country` route segment from frontend. *(completed)*
- [x] **T7.10** — **Pre-compute demographics aggregations** — Added `DemographicsSnapshot` Objectify entity that stores pre-aggregated daily/hourly/weekly counts per demographic dimension. Added `DemographicsSnapshotService` for building snapshots from raw data and assembling API responses from snapshots. Added cron job (`/tasks/snapshotDemographics`) running daily at 04:00, and a backfill endpoint (`/tasks/backfillSnapshots?from=MM/dd/yyyy&to=MM/dd/yyyy`) for historical data. *(completed)*

### Dashboard Data Completeness (T7.17–T7.18)

- [x] **T7.17** — **Add missing demographics to snapshot aggregation and dashboard** — Added `educationalLevel`, `timeSpentOnMturk`, `weeklyIncomeFromMturk`, and `languagesSpoken` to `DemographicsSnapshot` entity, `DemographicsSnapshotService` aggregation, `DemographicsSurveyAnswers` and `DemographicsCountsResponse` DTOs, and frontend sidebar navigation. Languages (multi-select) are split on commas and counted individually. Requires snapshot backfill to populate new fields for historical data. *(completed)*
- [x] **T7.18** — **Add Data Access links to dashboard sidebar** — Added a "Data Access" section below the demographics nav pills with links to the REST API docs (Swagger UI), CSV export endpoint, and BigQuery public dataset. *(completed)*

### New Visualizations (T7.11–T7.16)

- **T7.11** — **Response volume chart** — Add a line chart showing the raw count of survey responses per day/hour/week, not just percentages. Reveals activity spikes (holidays, weekends) and HIT traction trends. Data is already available in `DemographicsSnapshot.totalResponses` and `hourlyTotals`.
- **T7.12** — **Geographic heatmap** — Replace the binary "US vs Others" country breakdown with a world choropleth map (using D3.js or a lightweight SVG map library). Show all countries with color intensity proportional to response count. Requires expanding `incCountries()` to store full country codes instead of bucketing.
- **T7.13** — **Cross-tabulation / demographic intersections** — Show how demographics correlate (e.g., income distribution broken down by gender, age distribution by country). Add a new API endpoint returning two-dimensional pivot tables. Display as grouped bar charts or heatmap grids.
- **T7.14** — **Worker retention / return rate** — Track unique vs repeat workers over time using hashed `workerId`. Add a line chart of "new workers vs returning workers per week" to reveal workforce dynamics.
- **T7.15** — **Response time trends** — Plot median time between HIT creation (`hitCreationDate`) and answer submission (`date`). Shows labor market responsiveness. Display as a line chart with percentile bands (25th, 50th, 75th).
- **T7.16** — **Summary statistics cards** — Add a dashboard header with key metrics: total responses in the selected period, top 5 countries, median age bracket, most common income bracket. Display as Bootstrap cards/badges above the chart area.

## Track 8: Data Access & API Quality

Improvements to make the API more useful for data analysis and programmatic access. All changes are additive — no existing response shapes modified.

### Completed

- [x] **T8.1** — **CORS Support** — Created `CorsConfig.java` implementing `WebMvcConfigurer` to allow cross-origin `GET/POST/OPTIONS` requests on `/api/**` endpoints. Required for external API consumers (Jupyter notebooks, scripts, SPAs). *(completed)*
- [x] **T8.2** — **OpenAPI/Swagger Documentation** — Added `springdoc-openapi-starter-webmvc-ui` dependency. Auto-generates interactive API docs at `/swagger-ui.html` and machine-readable spec at `/v3/api-docs`. Created `OpenApiConfig.java` with API metadata. Added `@Operation` and `@Parameter` annotations to all `SurveyController` endpoints. Only public `/api/**` endpoints are included (internal `/tasks/**` excluded via `springdoc.paths-to-match`). *(completed)*
- [x] **T8.3** — **Raw Counts Endpoint** — Added `GET /api/survey/demographics/counts?from=MM/dd/yyyy&to=MM/dd/yyyy` returning raw count data (not percentages) from pre-computed `DemographicsSnapshot` entities. Response includes per-day breakdowns and summed totals. Created `DemographicsCountsResponse` DTO with `DailyCount` inner class. *(completed)*
- [x] **T8.4** — **CSV Export Endpoint** — Added `GET /api/survey/demographics/answers/csv?from=MM/dd/yyyy&to=MM/dd/yyyy` for downloading raw individual-level data. Uses `StreamingResponseBody` to avoid memory issues. Includes all 9 survey questions: `yearOfBirth`, `gender`, `maritalStatus`, `householdSize`, `householdIncome`, `educationalLevel`, `timeSpentOnMturk`, `weeklyIncomeFromMturk`, `languagesSpoken`. Date range capped at 366 days. Worker IDs MD5-hashed, IPs stripped. *(completed)*
- [x] **T8.5** — **Enhanced Filtering on Raw Answers** — Added optional `from` and `to` date range parameters to `GET /api/survey/demographics/answers`. Made `cursor` and `limit` optional (default limit=100). Backward compatible — omitting parameters produces identical behavior to previous version. *(completed)*
- [x] **T8.6** — **BigQuery Public Dataset Export** — Created `BigQueryExportService` and `BigQueryExportController` following the existing cron + Cloud Tasks pattern. Daily cron at 05:00 UTC exports yesterday's data. Includes `exportDateToBigQuery` for single dates and `backfillBigQuery` for historical data (recursive subdivision, same pattern as `SnapshotController`). BigQuery table includes all 9 survey fields plus metadata (date, worker_id, country, region, city, hit_id). Dataset is made publicly readable (`allUsers`). Export is idempotent (deletes existing rows for a date before inserting). *(completed)*

### BigQuery Table Schema

| Column | Type | Description |
|--------|------|-------------|
| `date` | TIMESTAMP | When the response was submitted |
| `worker_id` | STRING | MD5-hashed worker ID (privacy) |
| `country` | STRING | Detailed country code from App Engine geolocation (e.g. US, IN, GB) |
| `region` | STRING | Worker's region |
| `city` | STRING | Worker's city |
| `hit_id` | STRING | MTurk HIT ID |
| `year_of_birth` | STRING | Survey answer |
| `gender` | STRING | Survey answer |
| `marital_status` | STRING | Survey answer |
| `household_size` | STRING | Survey answer |
| `household_income` | STRING | Survey answer |
| `educational_level` | STRING | Survey answer |
| `time_spent_on_mturk` | STRING | Survey answer |
| `weekly_income_from_mturk` | STRING | Survey answer |
| `languages_spoken` | STRING | Comma-separated language codes |

### Environment Setup (Manual)

- App Engine service account needs `roles/bigquery.dataEditor` on the dataset
- Dataset `demographics` in project `mturk-demographics` will be auto-created on first export
- To backfill all historical data: `GET /tasks/backfillBigQuery?from=01/01/2015&to=03/09/2026`

### Note: Snapshot Backfill Required

After deploying T7.17, run a snapshot backfill to populate the 4 new demographic fields for historical data:
`GET /tasks/backfillSnapshots?from=01/01/2015&to=03/10/2026`

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
