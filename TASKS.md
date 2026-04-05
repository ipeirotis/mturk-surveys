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

- [x] **T7.1** — **Migrate from AngularJS to Vue 3** — Replaced the entire AngularJS 1.8.3 frontend with Vue 3 (CDN, no build step) + Vue Router 4. Removed jQuery, angular-cookies, angular-resource, angular-route, angular-sanitize, ui-bootstrap. Created Vue composables (useLoading, useDateFilter, useChartData) and components (ChartView, ChartjsChart, ChoroplethMap). Chart.js 4.4.7, D3.js v7, Bootstrap 5.3.3 remain unchanged. All 12 navigation views, 4 chart types, 2 map views, trend arrows, Top-N filtering, and date range selection preserved. *(completed)*
- [x] **T7.2** — **Update Bootstrap 3.1.1 to Bootstrap 5.3.3** — Replaced Bootstrap 3 CDN with Bootstrap 5.3.3, added Bootstrap Icons 1.11.3. Updated all CSS classes (`col-xs-*` → `col-*`, `btn-default` → `btn-outline-secondary`, `glyphicon` → `bi`, `text-right` → `text-end`, sidebar collapse `in` → `show`). Added CSS compatibility layer for ui-bootstrap datepicker (maps `.glyphicon-chevron-*` to Bootstrap Icons font, `.btn-default` and `.input-group-btn` shims). Updated nav-pills directive to toggle `.active` on `<a>` instead of `<li>`. *(completed)*
- [x] **T7.3** — **Replace YUI Compressor with individual script loading** — Removed the unmaintained `yuicompressor-maven-plugin` from pom.xml. JS files are now loaded individually in `index.html` (no bundling step needed). This simplifies the build, improves debuggability, and eliminates the last non-standard Maven plugin. *(completed)*

### Visualization Improvements (T7.4–T7.8)

Incremental improvements to the demographics dashboard charts, from quick wins within the current stack to a full modernization.

- [x] **T7.4** — **Replace Google Charts with Chart.js** — Replaced the Google Charts directive (`ng-google-chart.js`) with a custom Chart.js 4.4.7 directive. Chart.js is lightweight (~60KB), supports offline use, and provides smooth animations, responsive resizing, and retina support. *(completed)*
- [x] **T7.5** — **Add area/line chart option for time series** — Added a Bars/Area toggle button group to the dashboard. When "Area" is selected, charts render as stacked area (line with fill) using smooth curves, semi-transparent fills, and no point markers. The directive watches the chart type and re-renders on toggle. *(completed)*
- [x] **T7.6** — **Add interactive tooltips and drill-down** — Tooltips now show absolute counts alongside percentages on hover (e.g., "Male: 62.3% (1,247 of 2,003)") for daily charts by loading data from the `/api/survey/demographics/counts` endpoint in parallel. Tooltip footer shows total responses for the period. *(completed)*
- [x] **T7.7** — **Add trend arrows to summary statistics cards** — Added up/down/flat trend arrows to all 4 stat cards comparing the current date range against the equivalent prior period. Loads prior period data via a second API call (non-blocking, cached). Shows percentage change with colored arrows (green up, red down, gray flat). *(completed)*
- [x] **T7.8** — **Make the dashboard fully responsive** — Added viewport meta tag, collapsible sidebar menu (hamburger toggle on screens <992px), responsive chart container heights (500px/350px/280px breakpoints), responsive stat cards, and fluid footer for mobile. Date pickers and chart pills use `col-xs-*` classes for proper stacking. *(completed)*

### API & Data Pipeline Optimization (T7.9–T7.10)

- [x] **T7.9** — **Remove US-specific filtering from API and frontend** — Removed separate US-only aggregation maps from `SurveyService`, simplified `DemographicsSurveyAnswersByPeriod` from `Map<String, DemographicsSurveyAnswers>` to direct `DemographicsSurveyAnswers` fields, removed US nav links and `/:country` route segment from frontend. *(completed)*
- [x] **T7.10** — **Pre-compute demographics aggregations** — Added `DemographicsSnapshot` Objectify entity that stores pre-aggregated daily/hourly/weekly counts per demographic dimension. Added `DemographicsSnapshotService` for building snapshots from raw data and assembling API responses from snapshots. Added cron job (`/tasks/snapshotDemographics`) running daily at 04:00, and a backfill endpoint (`/tasks/backfillSnapshots?from=MM/dd/yyyy&to=MM/dd/yyyy`) for historical data. *(completed)*

### Dashboard Data Completeness (T7.17–T7.18)

- [x] **T7.17** — **Add missing demographics to snapshot aggregation and dashboard** — Added `educationalLevel`, `timeSpentOnMturk`, `weeklyIncomeFromMturk`, and `languagesSpoken` to `DemographicsSnapshot` entity, `DemographicsSnapshotService` aggregation, `DemographicsSurveyAnswers` and `DemographicsCountsResponse` DTOs, and frontend sidebar navigation. Languages (multi-select) are split on commas and counted individually. Requires snapshot backfill to populate new fields for historical data. *(completed)*
- [x] **T7.18** — **Add Data Access links to dashboard sidebar** — Added a "Data Access" section below the demographics nav pills with links to the REST API docs (Swagger UI), CSV export endpoint, and BigQuery public dataset. *(completed)*

### New Visualizations (T7.11–T7.16)

- [x] **T7.11** — **Response volume chart** — Added a "Volume" tab to the chart pills that shows a filled line chart of daily response counts. Uses the `/api/survey/demographics/counts` endpoint (loaded in parallel with aggregated answers). Chart has its own styling (no legend, y-axis labeled "Responses", smooth line with small data points). *(completed)*
- [x] **T7.12** — **Geographic choropleth maps** — Added world map and US states choropleth views using D3.js v7 + TopoJSON. World map shows response counts by country (ISO codes) with blue color scale. US states map shows per-capita response rates (per million residents, 2020 Census) with yellow-red scale, with a toggle for raw counts. Backend: added `countriesDetailed` (full ISO country codes) and `usStates` (2-letter state codes) fields to `DemographicsSnapshot`, `DemographicsRollup`, and all DTOs/builders. State data comes from App Engine's `X-AppEngine-Region` header (already stored in `UserAnswer.locationRegion`). Requires snapshot backfill to populate new fields for historical data. *(completed)*
- **T7.13** — **Cross-tabulation / demographic intersections** — Show how demographics correlate (e.g., income distribution broken down by gender, age distribution by country). Add a new API endpoint returning two-dimensional pivot tables. Display as grouped bar charts or heatmap grids. *Effort: ~1-2 weeks. **Hard** — `UserAnswer.answers` is an unindexed map, so cross-dimensional queries require full table scans (~7M entities). `DemographicsSnapshot` stores single-dimension counts only; adding all dimension pairs would explode storage. Best approaches: (a) add a BigQuery read path for on-demand queries, (b) pre-compute a curated set of ~5 popular cross-tabs in snapshots, or (c) compute on-the-fly with a tight date cap (~30 days). Recommend deferring until a BigQuery read path exists.*
- **T7.14** — **Worker retention / return rate** — Track unique vs repeat workers over time using hashed `workerId`. Add a line chart of "new workers vs returning workers per week" to reveal workforce dynamics. *Effort: ~1-2 weeks. **Hard** — Datastore has no `COUNT DISTINCT`. Classifying workers as new vs returning requires loading all historical `UserAnswer` entities and maintaining a running set of all previously-seen worker IDs (millions of entries). Not feasible at request time. Requires either BigQuery (`SELECT DATE_TRUNC(date, WEEK), COUNT(DISTINCT worker_id)` is trivial there) or a carefully designed pre-computation pipeline with persistent worker-set storage. Recommend deferring until a BigQuery read path exists.*
- [x] **T7.15** — **Response time trends** — Added response time percentile tracking (p25, median, p75) to `DemographicsSnapshot` and `DemographicsRollup` entities. The snapshot builder calculates percentiles from `hitCreationDate` vs `date` deltas (capped at 7 days to filter outliers). Response time data flows through all counts builders (daily, grouped, rollup-based) into `DemographicsCountsResponse`. Frontend: added "Response Time" sidebar link under new "Insights" section, a dedicated line chart with shaded p25-p75 band and bold median line. Tooltips format values as hours+minutes. Requires snapshot backfill to populate new fields for historical data. *(completed)*
- [x] **T7.16** — **Summary statistics cards** — Added four stat cards above the chart area showing: total responses, average responses per day, top country (with percentage), and top gender (with percentage). Cards use responsive grid (2 per row on mobile, 4 on desktop) with subtle styling. Data sourced from the counts API. *(completed)*

### Other Visualization Ideas to Consider

All frontend-only — no backend changes needed. Data for all categories already comes back in the `/chartData` response.

- [x] **T7.19** — **Small multiples / sparklines** — Added `SparklineGrid` Vue component that renders a responsive grid of small individual sparkline charts, one per category. Each sparkline shows the time series as a filled line chart with the category label and latest percentage value. Grid is sorted by total value descending. Added "Grid" display mode button to the chart toolbar (alongside Bars/Area/Line/Donut). Works with Top-N filtering. CSS grid layout auto-fills with 200px minimum column width. *(completed)*
- [x] **T7.20** — **Top-N filter** — Added an All/Top 5/Top 10/Top 15 button group that filters chart categories by total value, grouping the remainder into an "Other" series. Works with all display modes (bar, area, line, donut). Categories are ranked by sum of values across all periods. *(completed)*
- [x] **T7.21** — **Highlight on legend click** — Clicking a legend item now dims all other datasets instead of hiding them. Clicked dataset gets full opacity and thicker border; others fade to ~20% opacity. Click again to restore all. Uses Chart.js 4.x `legend.onClick` override. *(completed)*
- [x] **T7.22** — **Pie/donut chart for latest period** — Added a "Donut" display mode button. Shows the most recent period's breakdown as a doughnut chart with right-aligned legend, percentage tooltips, and Top-N filtering support. *(completed)*
- **T7.23** — **Heatmap view** — For fields with many categories over time (countries, income brackets), a heatmap where color intensity = percentage could be more readable than either stacked bars or spaghetti lines. *Effort: ~2-3 days. **Moderate** — Chart.js has no native heatmap. Best approach: custom D3.js directive following the established choropleth pattern, or use `chartjs-chart-matrix` plugin.*

### Recommended Priority (Remaining Track 7)

| Priority | Task | Effort | Backend? | Value |
|----------|------|--------|----------|-------|
| ~~1~~ | ~~T7.20 Top-N filter~~ | ~~1d~~ | ~~No~~ | ~~High~~ — **Done** |
| ~~2~~ | ~~T7.21 Legend highlight~~ | ~~0.5d~~ | ~~No~~ | ~~Medium~~ — **Done** |
| ~~3~~ | ~~T7.22 Pie/donut~~ | ~~1d~~ | ~~No~~ | ~~Medium~~ — **Done** |
| ~~4~~ | ~~T7.7 Trend arrows~~ | ~~1d~~ | ~~Minor~~ | ~~Medium~~ — **Done** |
| ~~1~~ | ~~T7.19 Sparklines~~ | ~~2-3d~~ | ~~No~~ | ~~High~~ — **Done** |
| ~~2~~ | ~~T7.15 Response time~~ | ~~2-3d~~ | ~~Medium~~ | ~~Medium~~ — **Done** |
| 1 | T7.23 Heatmap | 2-3d | No | Medium — nice to have |
| 4 | T7.13 Cross-tabs | 1-2w | **Heavy** | High — defer for BigQuery read path |
| 5 | T7.14 Worker retention | 1-2w | **Heavy** | High — defer for BigQuery read path |

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

## Track 9: Robustness & Reliability

Improvements to error handling, resilience, and operational stability for a production system running since 2015.

### Task Endpoint Security (Critical)

- [x] **T9.1** — **Authenticate `/tasks/` endpoints** — Added `TaskAuthFilter` (registered on `/tasks/*` in `FilterConfig`) that verifies requests originate from App Engine cron (`X-Appengine-Cron: true`), Cloud Tasks (`X-CloudTasks-TaskName` or `X-AppEngine-TaskName` header), or an admin API key (`X-Task-Admin-Key` header matching `TASK_ADMIN_KEY` env var). Rejects unauthorized requests with 403 JSON response. Allows all requests in local development (no `GAE_APPLICATION` env var). On App Engine, cron/task headers are stripped from external requests by infrastructure, so they can only be present on genuine internal requests. *(completed)*

- [ ] **T9.2** — **Add input validation to task endpoints** — Add `@NotBlank` and date format validation to all `@RequestParam` on task controllers (`SnapshotController`, `BigQueryExportController`, `DatastoreRestoreController`, `DatastoreBackupController`). Validate date ranges (from ≤ to, max range limits) to prevent resource exhaustion from unbounded queries.

### Error Handling & Resilience

- [x] **T9.3** — **Add global catch-all exception handler** — Extended `RestResponseEntityExceptionHandler` with handlers for `MturkException` → 502, `ParseException` → 400, `IllegalArgumentException` → 400, `TaskEnqueueException` → 502, and catch-all `Exception` → 500. All handlers return structured `ErrorResponse` JSON bodies with timestamp, status code, and message. Severe errors are logged. Extracted `buildResponse()` helper to reduce duplication. *(completed)*

- [ ] **T9.4** — **Add retry with backoff on MTurk API calls** — Wrap `MturkService` methods with Spring Retry (`@Retryable`) or manual exponential backoff for transient failures (network errors, rate limiting). Configure max 3 retries with 1s/2s/4s delays. Add `spring-retry` dependency.

- [ ] **T9.5** — **Add retry limits to task re-enqueuing** — In `CreateHITController`, `DeleteHITsController`, and `ApproveAssignmentsController`, track retry count via a request parameter (e.g., `?retryCount=N`). Stop re-enqueuing after 5 attempts and log a SEVERE error instead of silently retrying forever.

- [x] **T9.6** — **Fix silent failure in TaskUtils.queueTask()** — Created `TaskEnqueueException` (extends `RuntimeException`). Changed `TaskUtils.queueTask()` to throw `TaskEnqueueException` instead of returning `null` on failure. The exception is caught by the global exception handler (T9.3) which returns a 502 JSON response. Existing callers (task controllers) propagate the exception naturally since it's unchecked. *(completed)*

### Timeouts & Resource Management

- [ ] **T9.7** — **Configure MTurk client timeouts** — Set connect timeout (5s), read timeout (10s), and total API call timeout (30s) on the `MturkClient` via `MturkClient.builder().overrideConfiguration(...)`. Close clients properly with try-with-resources or a shared singleton with `@PreDestroy` cleanup.

- [ ] **T9.8** — **Configure BigQuery client timeouts** — Set `QueryJobConfiguration` timeouts and `BigQueryOptions` retry settings. Add a 60s timeout on `bigQuery.query()` calls and a 120s timeout on bulk `insertAll()` operations.

- [ ] **T9.9** — **Fix HttpURLConnection resource leak** — In `DatastoreBackupController`, wrap `HttpURLConnection` usage in try-with-resources. Set connect timeout (10s) and read timeout (30s).

### Memory & Query Bounds

- [ ] **T9.10** — **Paginate large in-memory queries** — Refactor `UserAnswerService.listByDateRange()` and `SurveyService.listAnswersByDateRange()` to use cursor-based iteration instead of loading all results into a `List`. Use Objectify's `QueryResultIterator` with chunked processing (e.g., 500 entities at a time) for snapshot building and BigQuery export.

- [ ] **T9.11** — **Stream CSV export from Datastore** — Refactor `SurveyController.exportAnswersCsv()` to use cursor-based pagination inside the `StreamingResponseBody`, fetching 500 entities at a time and writing directly to the output stream, instead of loading the entire date range into memory first.

- [ ] **T9.12** — **Add cache eviction policy** — Replace `ConcurrentMapCacheManager` with Caffeine cache (`spring-boot-starter-cache` + `caffeine` dependency). Configure maximum cache size (e.g., 100 entries), TTL (1 hour), and eviction listeners for logging. This prevents unbounded memory growth on long-running instances.

### Idempotency & Data Integrity

- [ ] **T9.13** — **Make HIT creation idempotent** — Add a `UniqueRequestToken` to `CreateHitRequest` using a deterministic key (e.g., hash of survey ID + date + hour). MTurk's API will reject duplicate creation attempts with the same token, preventing double-HIT creation on task retries.

- [ ] **T9.14** — **Add optimistic locking to snapshot writes** — Before saving a `DemographicsSnapshot`, check if one already exists for the same date. If it does and was updated recently (within 5 minutes), skip the write to prevent concurrent builds from overwriting each other.

## Track 10: Scalability & Performance

Improvements to handle growing data volume and reduce latency.

### Batch Operations

- [ ] **T10.1** — **Batch MTurk API calls in DeleteHITs and ApproveAssignments** — Currently these controllers make one AWS API call per `UserAnswer` (N+1 pattern). Collect HIT IDs / assignment IDs into batches of 10-20, then process batches. Add a configurable rate limiter (e.g., Guava `RateLimiter` at 5 requests/second) to stay within AWS API limits.

- [ ] **T10.2** — **Increase task processing batch sizes** — `DeleteHITsController` and `ApproveAssignmentsController` use `limit(30)` per task execution. Increase to `limit(100)` with task-level timeout awareness (check remaining time vs. App Engine 10-min limit before processing next batch).

### Caching

- [ ] **T10.3** — **Add Memcache/Redis for distributed caching** — The current in-memory cache is per-instance. On App Engine with auto-scaling (2+ instances), each instance maintains a separate cache. Add App Engine Memcache or Cloud Memorystore (Redis) for shared caching of `chartData` and `aggregatedAnswers`.

- [ ] **T10.4** — **Implement incremental cache invalidation** — Currently `@CacheEvict(allEntries=true)` clears the entire cache when any snapshot is built. Instead, evict only the affected cache keys (by date range) so that unrelated queries remain cached.

### Database Optimization

- [ ] **T10.5** — **Review and optimize Datastore indexes** — Audit `index.yaml` against actual query patterns. Remove unused composite indexes (each index adds write latency). Add missing indexes for new query patterns (e.g., `DemographicsSnapshot` by date range, `DemographicsRollup` by period + type).

- [ ] **T10.6** — **Add Datastore query projection** — For aggregation queries that only need a few fields (e.g., snapshot building needs only `answers`, `date`, `locationCountryCode`), use Objectify projection queries to avoid deserializing full entities. This reduces both Datastore read costs and memory usage.

## Track 11: Observability & Operations

Monitoring, logging, and operational tooling for production visibility.

### Health & Monitoring

- [ ] **T11.1** — **Add Spring Boot Actuator health endpoint** — Add `spring-boot-starter-actuator` dependency. Configure `/actuator/health` as a liveness probe and `/actuator/info` with build metadata. Expose only health and info endpoints (not all actuator endpoints). Configure App Engine's `liveness_check` in `app.yaml` to use it.

- [ ] **T11.2** — **Add custom health indicators** — Implement `HealthIndicator` beans for: (a) Datastore connectivity (simple read test), (b) MTurk API reachability (describe account call), (c) BigQuery connectivity. Report `DOWN` if any external dependency is unreachable.

- [ ] **T11.3** — **Add Micrometer metrics** — Add `micrometer-registry-stackdriver` (or `micrometer-registry-prometheus`) for exporting metrics to Cloud Monitoring. Instrument: API request latency histograms, MTurk API call counts/durations, BigQuery export success/failure rates, snapshot build durations, task queue depths.

### Logging

- [ ] **T11.4** — **Switch to structured JSON logging** — Replace `java.util.logging` with SLF4J + Logback. Configure JSON output format for Cloud Logging integration (automatic severity parsing, trace ID extraction). Add MDC context for request IDs.

- [ ] **T11.5** — **Add request correlation IDs** — Add a servlet filter that generates a UUID per request (or extracts `X-Cloud-Trace-Context` from App Engine). Propagate via MDC to all log statements. Pass as a parameter when enqueuing Cloud Tasks for end-to-end tracing.

### Alerting

- [ ] **T11.6** — **Add task failure monitoring endpoint** — Create a `/tasks/status` diagnostic endpoint (authenticated) that reports: number of pending tasks in queue, last successful snapshot date, last successful BigQuery export date, and last successful HIT creation time. Useful for operational dashboards and alerting.

## Track 12: API Security & Documentation Alignment

Focused hardening and cleanup tasks to reduce operational risk and improve contributor onboarding.

### Endpoint Security

- [x] **T12.1** — **Restrict `/tasks/**` and `/tasks/debug/**` endpoints** — Added `TaskAuthFilter` registered on `/tasks/*` in `FilterConfig` (order=2, after Objectify filter). Verifies `X-Appengine-Cron: true`, `X-CloudTasks-TaskName`/`X-AppEngine-TaskName`, or admin API key (`X-Task-Admin-Key` matching `TASK_ADMIN_KEY` env var). Returns 403 JSON for unauthorized callers. Bypassed in local dev (no `GAE_APPLICATION` env var). Shared implementation with T9.1. *(completed)*

- [x] **T12.2** — **Move dangerous debug controllers behind feature flag** — Added `@ConditionalOnProperty(name = "debug.tasks.enabled", havingValue = "true", matchIfMissing = false)` to both `DebugDatastoreController` and `DiagnosticController`. Beans are not registered unless `DEBUG_TASKS_ENABLED=true` env var is set. Added property binding in `application.properties`. *(completed)*

- [x] **T12.3** — **Require non-GET methods for mutating task endpoints** — Changed `TaskUtils.queueTask()` from `HttpMethod.GET` to `HttpMethod.POST` with form-encoded body (`application/x-www-form-urlencoded`). Cloud Tasks-only endpoints changed to `@PostMapping`. Endpoints called by both cron (GET) and Cloud Tasks (POST) use `@RequestMapping(method = {GET, POST})`. Cron-only and read-only endpoints remain `@GetMapping`. Spring's `@RequestParam` reads from both query params and form body automatically. *(completed)*

### API Modernization

- [ ] **T12.4** — **Migrate JSONP answer endpoints to JSON APIs** — Replace `/saveAnswer` and `/getAnswer` callback-based responses with `application/json` contracts, using standard request/response DTOs.

- [ ] **T12.5** — **Add deprecation window for legacy JSONP clients** — Keep compatibility wrappers for a defined period (e.g., 60-90 days), emit deprecation headers/log warnings, and remove JSONP after migration.

### CORS & Transport Controls

- [ ] **T12.6** — **Tighten CORS allowlist** — Replace wildcard origins in `CorsConfig` with an explicit list of trusted frontend domains (prod + optional staging), configurable via env var.

- [ ] **T12.7** — **Add rate limiting for public API endpoints** — Add per-IP limits on `/api/**` read endpoints and stricter limits on write/submit endpoints to mitigate abuse and traffic spikes.

### Data & Performance Hygiene

- [ ] **T12.8** — **Stream CSV export with cursor pagination** — Refactor `SurveyController.exportAnswersCsv()` to iterate through UserAnswer records in chunks and stream rows without loading full date ranges into memory.

- [ ] **T12.9** — **Replace unbounded cache with Caffeine** — Swap `ConcurrentMapCacheManager` for Caffeine with explicit max size, TTL, and optional metrics hooks.

- [ ] **T12.12** — **Fix duplicate-write race condition in answer ingestion** — Replace check-then-save dedup with transactional/idempotent persistence keyed by `(workerId, hitId)` to prevent concurrent duplicate inserts.

- [ ] **T12.13** — **Bound recursive/self-enqueued retries** — Add retry counters + exponential backoff to self-requeue paths (e.g., HIT creation/backfill recursion) and dead-letter handling after max attempts.

- [ ] **T12.14** — **Externalize hardcoded backup project/bucket configuration** — Move `DatastoreBackupController` constants to required env config with fail-fast startup validation.

- [ ] **T12.15** — **Standardize date formats across API/task endpoints** — Prefer ISO `yyyy-MM-dd` (or dual parser with strict validation) to reduce operator mistakes across `/api/**` and `/tasks/**`.

- [ ] **T12.16** — **Harden request context handling in answer ingestion** — Guard `getIp()` against null/non-servlet contexts and malformed forwarding headers.

- [ ] **T12.17** — **Reuse MTurk clients instead of per-call creation** — Introduce managed singleton clients (prod/sandbox) with explicit timeout + lifecycle management.

- [ ] **T12.18** — **Make global dedup memory-safe** — Refactor `deduplicateGlobal()` to streaming/partitioned processing rather than loading all groups into memory.

- [ ] **T12.19** — **Expose reliable export failure signals** — Adjust BigQuery export task contract so failures are observable/retriable (status codes + metrics + reconciliation workflow), not silently treated as success.

- [ ] **T12.20** — **Refactor `SurveyService` into focused modules** — Split CRUD/template/validation/legacy aggregation responsibilities to reduce coupling and rule drift.

- [ ] **T12.21** — **Add controller/security coverage for critical routes** — Add MVC tests for task auth guards, answer ingestion contracts, and export/restore endpoints.

- [ ] **T12.22** — **Reduce reflection-heavy tests** — Convert private-method reflection tests to behavior-focused tests over public APIs and collaborator contracts.

- [ ] **T12.23** — **Add concurrency tests for idempotency** — Add parallel-submission tests to verify duplicate prevention under contention.

### Documentation Quality

- [ ] **T12.10** — **Expand README for onboarding** — Add setup prerequisites, local run instructions, environment variable requirements, test commands, and high-level architecture.

- [ ] **T12.11** — **Fix documentation drift in `CLAUDE.md`** — Update inaccurate notes (e.g., test availability) and align operational docs with current code behavior/endpoints.

## Recommended Execution Order

1. **Track 1** (CI/CD) — no code risk, immediate value
2. **Track 2** (Config/Security) — fixes broken Cloud Tasks, improves security
3. **Track 3** (Minor deps) — low risk, gets dependencies current
4. **Track 4** (Java 21 + Spring Boot 3) — biggest effort, do after CI is in place
5. **Track 5** (AWS SDK) — after Spring Boot migration stabilizes
6. **Track 6** (Cloud Tasks lib) — after Spring Boot migration stabilizes
7. **Track 7** (Frontend) — only if needed
8. **Track 9** (Robustness) — highest priority for production stability; T9.1 is critical security fix
9. **Track 10** (Scalability) — pursue when data volume growth demands it
10. **Track 11** (Observability) — high value for ongoing operations
11. **Track 12** (API Security & Docs) — immediate hardening + developer-experience improvements
