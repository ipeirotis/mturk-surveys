# CLAUDE.md

## Project Overview

**mturk-surveys** is a Java Spring Boot web application that ran continuous demographic surveys of Amazon Mechanical Turk workers. It created HITs (Human Intelligence Tasks) on MTurk, collected worker responses, and provides aggregated demographics analytics through a web dashboard.

> **Data collection has ended.** Amazon closed Mechanical Turk on **September 30, 2026**. All scheduled jobs are removed (`cron.yaml` is empty and the standalone Cloud Scheduler jobs were deleted on 2026-10-01). Once `MturkService.CLOSURE_DATE` has passed, every MTurk API call throws `MturkClosedException` (HTTP 410), the task endpoints return without calling MTurk, and `/saveAnswer` rejects new answers with 410. The dashboard and API keep serving the archived 2015–2026 data. The `/tasks/*` endpoints still work when called by hand with the admin key.

Deployed on **Google App Engine** (Java 21 runtime, GCP project `mturk-demographics`) with **Google Cloud Datastore** for persistence. The production URL is **https://demographics.mturk-tracker.com/**. The demographics survey ran from **March 2015** to **September 30, 2026** — there is no useful data outside that range.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4.1, Jetty (not Tomcat)
- **ORM:** Objectify 6.1.3 (Google Cloud Datastore)
- **Cloud:** Google App Engine, Google Cloud Tasks, Google Secret Manager
- **AWS:** MTurk SDK 2.35.6
- **Frontend:** Vue 3 (CDN, no build step), Vue Router 4, Bootstrap 5.3.3, Chart.js 4.4.7, D3.js v7
- **Build:** Maven
- **Templating:** FreeMarker 2.3.33

## Build & Run Commands

```bash
# Build the project
mvn clean install

# Run locally
mvn spring-boot:run

# Deploy to Google App Engine
mvn appengine:deploy
```

There are **no tests** configured in this project. No linter or formatter is set up.

### Required CLI Tools

When working on this project in a cloud/CI environment, ensure these tools are installed:

- **`gcloud`** — Google Cloud SDK (for `gcloud datastore`, `gcloud app deploy`, `gcloud auth`, `gcloud secrets`)
- **`bq`** — BigQuery CLI (bundled with Google Cloud SDK; for querying `demographics.responses` and `test.UserAnswer_2025MAR20`)
- **`gh`** — GitHub CLI (for creating PRs, managing issues)
- **`mvn`** — Maven 3.9+ (for building and deploying)
- **Java 21** — Required runtime

If `gcloud`/`bq` are unavailable, you can use the REST APIs with an access token from `gcloud auth print-access-token` (Datastore API, BigQuery API). The GCP project ID is `mturk-demographics`.

### Maven Proxy / Network Issues

Maven 3.9+ uses Apache HttpClient by default for dependency resolution, which may fail with proxy authentication (407 errors) or DNS resolution failures in restricted network environments. If `mvn clean install` fails with network errors:

1. **Create `~/.m2/settings.xml`** with proxy credentials (host, port, username, password) matching your environment's proxy settings.
2. **Use the Wagon transport** by passing `-Dmaven.resolver.transport=wagon` to Maven. The Wagon HTTP transport handles HTTPS proxy authentication correctly, whereas the default Apache resolver transport may not.
3. **Unset `JAVA_TOOL_OPTIONS`** proxy flags to avoid conflicts between JVM-level and Maven-level proxy settings.

Example build command for proxied environments:

```bash
JAVA_TOOL_OPTIONS="" mvn clean install -Dmaven.resolver.transport=wagon
```

## Project Structure

```
src/main/java/com/ipeirotis/
├── MturkSurveysApplication.java     # Entry point, registers Objectify entities
├── config/                          # Spring configuration (FilterConfig)
├── controller/
│   ├── MturkController.java         # MTurk HIT management endpoints
│   ├── SurveyController.java        # Survey CRUD & demographics analytics
│   ├── answer/                      # Save/get user answer endpoints
│   └── tasks/                       # Task controllers (manual or Cloud Tasks; no cron)
├── service/
│   ├── MturkService.java            # MTurk API integration
│   ├── SurveyService.java           # Survey business logic
│   └── UserAnswerService.java       # Answer processing & aggregation
├── dao/                             # Data access (SurveyDao, QuestionDao, UserAnswerDao)
├── entity/                          # Objectify entities (Survey, Question, Answer, Selection, UserAnswer)
├── dto/                             # Analytics DTOs (DemographicsSurveyAnswers, ByPeriod)
├── enums/                           # Country enum (255+ countries)
├── entity/enums/                    # AnswerType, QuestionContentType, SuggestionStyle
├── exception/                       # Custom exceptions + global handler
├── ofy/                             # Objectify base DAO & cursor pagination
└── util/                            # MD5 hashing, date/number formatting, Cloud Tasks utils

src/main/resources/
├── application.properties           # Server port 8080, logging config
├── static/                          # Frontend SPA
│   ├── index.html                   # Main page (Vue 3 app)
│   ├── css/style.css
│   └── vue/                         # Vue 3 components and composables
│       ├── app.js                   # Vue app creation + Vue Router
│       ├── composables/             # useLoading, useDateFilter, useChartData
│       └── components/              # ChartView, ChartjsChart, ChoroplethMap
└── templates/                       # FreeMarker templates for MTurk HIT HTML

src/main/appengine/
├── app.yaml                         # GAE config (F2 instance, env vars for AWS creds)
├── cron.yaml                        # Empty: no scheduled jobs after the MTurk closure
└── index.yaml                       # Datastore composite indexes
```

## Key API Endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/survey/{surveyId}` | GET | Get survey details |
| `/api/survey` | POST | Create a new survey |
| `/api/survey/demographics/answers` | GET | Paginated user answers |
| `/api/survey/demographics/aggregatedAnswers` | GET | Aggregated demographics by period |
| `/saveAnswer` | GET | Save worker answer (JSONP; 410 after MTurk closure) |
| `/getAnswer` | GET | Get previous worker answer |
| `/getHIT/{hitId}` | GET | Get MTurk HIT details |
| `/listHITs` | GET | List all HITs |
| `/tasks/createHIT` | GET | Create HIT (no-op after MTurk closure; cron removed) |
| `/tasks/deleteHITs` | GET | Delete old HITs (no-op after MTurk closure; cron removed) |

## Architecture & Conventions

### Code Patterns
- **MVC with service layer:** Controllers -> Services -> DAOs -> Objectify/Datastore
- **Objectify entities** use `@Entity`, `@Cache`, `@Id`, `@Index` annotations
- **Generic DAO base class:** `OfyBaseDao<T>` provides CRUD for all entities
- **Global exception handling** via `@ControllerAdvice` in `RestResponseEntityExceptionHandler`
- **Background tasks** were triggered by cron (cron.yaml, now empty) and use Google Cloud Tasks for fan-out and retries

### Naming Conventions
- Classes: `PascalCase` with suffixes (`Controller`, `Service`, `Dao`)
- Methods/fields: `camelCase`
- Package: `com.ipeirotis.*`

### Data Model
- **Survey** contains **Questions**, each with **Answers** (freetext or selection-based)
- **Selection** defines choices for selection-type answers
- **UserAnswer** stores a worker's response with geolocation, timestamp, and hashed worker ID
- Worker IDs are MD5-hashed in analytics output for privacy

### Environment Variables & Secrets

#### App Engine Environment Variables (`app.yaml`)
| Variable | Required | Default | Description |
|---|---|---|---|
| `AWS_REGION` | No | `us-east-1` | AWS region for MTurk API calls |
| `QUEUE_ID` | Yes | `default` | Google Cloud Tasks queue name |
| `LOCATION_ID` | Yes | `us-central1` | GCP region for Cloud Tasks |
| `GOOGLE_CLOUD_PROJECT` | Auto | *(set by GAE)* | GCP project ID (provided automatically on App Engine) |
| `PORT` | No | `8080` | Server port (provided automatically on App Engine) |
| `TASK_ADMIN_KEY` | No | *(none)* | Fallback admin API key for local dev only. In production, loaded from Secret Manager (`task-admin-key`). |
| `DEBUG_TASKS_ENABLED` | No | `false` | Set to `true` to enable `/tasks/debug/*` diagnostic endpoints |

#### GCP Secret Manager Secrets
AWS credentials are stored in **GCP Secret Manager** (not in env vars). The app reads them at startup via `AwsCredentialsConfig`. Create these secrets in the `mturk-demographics` project:

| Secret ID | Description |
|---|---|
| `aws-access-key-id` | AWS access key for the MTurk requester account |
| `aws-secret-access-key` | AWS secret key for the MTurk requester account |
| `task-admin-key` | Admin API key for manually triggering `/tasks/*` endpoints. Pass via `X-Task-Admin-Key` header. |

To create the secrets:
```bash
echo -n "AKIAIOSFODNN7EXAMPLE" | gcloud secrets create aws-access-key-id --data-file=-
echo -n "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY" | gcloud secrets create aws-secret-access-key --data-file=-
```

The App Engine default service account needs the **Secret Manager Secret Accessor** role (`roles/secretmanager.secretAccessor`).

For **local development**, the app falls back to the default AWS credential provider chain (env vars `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`, `~/.aws/credentials`, etc.).

#### GitHub Actions Secrets (CI/CD)
| Secret | Description |
|---|---|
| `GCP_SA_KEY` | JSON key for a GCP service account with App Engine Admin + Secret Manager access |

## Backup & Recovery

The system has multiple layers of backup for disaster recovery.

### Backup Layers

| Layer | Frequency | Location | What |
|---|---|---|---|
| **BigQuery `demographics.responses`** | Static since 2026-10-01 | BigQuery | **Canonical.** Every answer, worker IDs and IPs hashed. Append-only. |
| **Datastore export** | Manual only (weekly cron removed) | `gs://demographics_data_export/<date>/` | Raw entity backup. Last good export: 2026-03-18. The weekly job failed with 403 from at least September 2026 because the App Engine service account lacks `roles/datastore.importExportAdmin`. |
| **DemographicsSnapshot** | Manual only (daily cron removed) | Datastore | Pre-aggregated daily counts for the dashboard. Last built 2026-10-01 for 2026-09-30. |
| **DemographicsRollup** | Manual only | Datastore | Weekly/monthly aggregates built from snapshots |

### BigQuery Tables

| Table | Description |
|---|---|
| `demographics.responses` | **Canonical** public dataset (worker IDs + IPs SHA256-hashed, lowercase hex). 392,998 answers, 2015-03-26 to 2026-09-30. See "Canonical dataset" below. |
| `test.MISQ_DataSet` | View over `demographics.responses` that keeps the old MISQ column names, with `worker_id`/`ip_address` as raw SHA-256 bytes (`FROM_HEX`). Before 2026-10-01 it unioned the two backups below, without dedup or survey filtering. |
| `test.responses_backup_20261001` | Copy of `demographics.responses` taken right before the 2026-10-01 restore (329,772 rows). Safe to drop once the restore is accepted. |
| `test.UserAnswer_2025MAR20` | One-time Datastore backup from 2025-03-20 (raw entity export via GCS). Covers 2020-11-03 to 2025-03-20. |
| `test.userAnswers_oct2020` | Older Datastore backup. Covers 2015-03-26 to 2021-06-10. |

### Canonical dataset

`demographics.responses` is the source of truth for the archived 2015–2026 answers. Datastore and the backups are inputs to it, not the other way round.

- **Never delete or rewrite rows in it from Datastore.** `BigQueryExportService.exportDate` is append-only: it inserts only (hashed worker, HIT) pairs not already in the table for that day. It used to `DELETE` the day and rebuild it from Datastore, which is how a partial Datastore wiped out good rows.
- **Overlapping exports are serialized** by `BigQueryExportLock` leases in Datastore (10-minute expiry). Each export checks existing pairs over its date ± 1 day, so it takes the leases for all three dates in one transaction. Exports whose windows overlap run one at a time; exports at least three days apart still run in parallel. Without the leases, two overlapping exports could both see a pair as missing and insert it twice. An insert-only `MERGE` wouldn't help, because BigQuery doesn't detect conflicts between insert-only DML statements. A second export for the same date gets HTTP 409, and `/tasks/exportDateToBigQuery` returns non-2xx on any failure, so Cloud Tasks retries it.
- **2026-10-01 restore:** 63,226 answers from 2015-09 to 2021-06 were missing. The BigQuery backfill ran between the 2026-03-10 Datastore restore (which reused original IDs and only brought back part of each day) and the 2026-03-17 restore (new IDs, filled the rest), and the restored days were never re-exported. They were appended with one `INSERT … SELECT` from `test.userAnswers_oct2020` + `test.UserAnswer_2025MAR20` (`surveyId = 'demographics'`, earliest row per (workerId, hitId), `TO_HEX(SHA256(...))` for worker ID and IP, only pairs not already present).
- `demographics.responses` can't be a restore source: `restoreDateFromBigQuery`, `backfillRestoreFromBigQuery` and `smartRestoreFromBigQuery` reject `table=demographics.responses` with HTTP 400. They read the Datastore backup schema.
- To check the public table against Datastore: `GET /tasks/compareDatastoreBigQuery?from=…&to=…` (it defaults to `demographics.responses`).

#### Done on 2026-10-01

- PR #118 merged and deployed (append-only export, per-date export locks, restore → export step).
- All schedules removed: `cron.yaml` is empty, and the five standalone Cloud Scheduler jobs created on 2026-04-05 (`export-to-bigquery`, `snapshot-demographics`, `warm-chart-cache`, `backup-datastore`, `dedup-datastore-global`) were deleted. They duplicated the `cron.yaml` jobs and were rejected by `TaskAuthFilter` with 403 every run, because Cloud Scheduler doesn't send `X-Appengine-Cron`.
- `compareDatastoreBigQuery` defaults to `demographics.responses`, and the `data-check` / `backup-check` commands describe the archive state.
- The session hook unsets `CLOUDSDK_AUTH_ACCESS_TOKEN` once the service account is activated. Some cloud environments set it to an expired token, which overrides the service account and causes "Invalid Credentials".
- The "3 duplicate pairs" turned out not to be duplicates: 3 genuine answers (2016-12-24, 2016-12-28, 2020-05-03) have a HIT ID but no worker ID, and `COUNT(DISTINCT CONCAT(worker_id, '|', hit_id))` skips NULLs. They stay.

#### Optional follow-ups

- [ ] **Final Datastore export**, if a raw entity backup newer than 2026-03-18 is wanted: grant the App Engine service account `roles/datastore.importExportAdmin` (an owner must do this; see IAM Requirements) and call `/tasks/backupDatastore` once, or run `gcloud datastore export gs://demographics_data_export/<date>/` as an owner.
- [ ] **Timestamp precision:** the Java export wrote `date`/`hit_creation_date` truncated to whole seconds. The 63,226 rows appended on 2026-10-01 kept milliseconds. If consistency matters, run `UPDATE … SET date = TIMESTAMP_TRUNC(date, SECOND), hit_creation_date = TIMESTAMP_TRUNC(hit_creation_date, SECOND)` on those rows (production write, needs approval).
- [ ] **Dashboard vs. table:** snapshots and rollups are built from Datastore (393,001 total vs. 392,998 in `responses`). Rebuilding them from `responses` would make the dashboard match the canonical table exactly. It's a larger change to `DemographicsSnapshotService`.
- [ ] **Drop `test.responses_backup_20261001`** once the restore is accepted.

### GCS Bucket

- **`gs://demographics_data_export/`** — Stores Datastore exports (weekly until the 2026-10-01 cron removal; the last successful one is `2026-03-18T02:08:51_4585/`). Each export creates a timestamped subfolder with all entity data in Datastore's native export format.

### IAM Requirements

The App Engine default service account (`mturk-demographics@appspot.gserviceaccount.com`) needs:

- `roles/datastore.importExportAdmin` — For the weekly Datastore export to GCS
- `roles/secretmanager.secretAccessor` — For reading AWS credentials (already configured)

```bash
gcloud projects add-iam-policy-binding mturk-demographics \
  --member="serviceAccount:mturk-demographics@appspot.gserviceaccount.com" \
  --role="roles/datastore.importExportAdmin"
```

### Recovery Procedures

#### Full Datastore restore from GCS backup

```bash
# List available backups
gsutil ls gs://demographics_data_export/

# Import a specific backup (restores ALL entity kinds)
gcloud datastore import gs://demographics_data_export/2026-03-09/ --project=mturk-demographics

# Import only specific kinds
gcloud datastore import gs://demographics_data_export/2026-03-09/ \
  --kinds=UserAnswer,DemographicsSnapshot --project=mturk-demographics
```

#### Restore individual dates from BigQuery

```
# Compare Datastore vs demographics.responses counts for a date range
GET /tasks/compareDatastoreBigQuery?from=2024-01-01&to=2024-12-31

# Compare Datastore vs a backup table instead
GET /tasks/compareDatastoreBigQuery?from=2024-01-01&to=2024-12-31&table=UserAnswer_2025MAR20

# Restore a single day from BigQuery backup
GET /tasks/restoreDateFromBigQuery?date=2024-06-15

# Smart restore: only restore days where Datastore has fewer entries
GET /tasks/smartRestoreFromBigQuery?from=2024-01-01&to=2024-12-31
```

Restoring a day into Datastore doesn't touch `demographics.responses`, so `restoreDateFromBigQuery` now runs the BigQuery export for that day in the same request, every time. If the export fails, the request returns non-2xx and Cloud Tasks retries the whole restore. The retry restores nothing new but still exports the day. The export is append-only and adds only pairs missing from the table. This gap is what dropped ~63K 2015–2021 answers from the public table: the backfill ran after the 2026-03-10 restore but before the 2026-03-17 restore, and the restored days were never re-exported.

#### Rebuild snapshots and rollups

```
# Check snapshot coverage (shows missing dates per month)
GET /tasks/snapshotCoverage

# Backfill missing snapshots (enqueues Cloud Tasks)
GET /tasks/snapshotCoverage?backfill=true

# Rebuild all weekly + monthly rollups
GET /tasks/backfillRollups
```

#### Trigger manual backups

```
# Full Datastore export to GCS
GET /tasks/backupDatastore

# Export specific kinds only
GET /tasks/backupDatastore?kinds=UserAnswer,DemographicsSnapshot

# Export a single date to BigQuery demographics.responses
GET /tasks/exportDateToBigQuery?date=01/15/2024

# Full BigQuery backfill (all dates)
GET /tasks/backfillBigQuery?from=03/26/2015&to=03/11/2026
```

### Known Data Gaps

- **2019-02-21 to 2019-03-17** (~24 days) — Survey was paused, no data in any source
- **2019-11-09 to 2019-12-31** (~53 days) — Survey was paused
- **2020-01-01 to 2020-01-13** (~13 days) — Survey was paused

These gaps are legitimate (no survey activity) and appear as zero-response entries in snapshots/rollups.

## Important Notes

- The frontend uses **Vue 3 via CDN** (no build step, no npm/node required). JS files are loaded individually from `static/vue/`.
- The app uses **Jetty** (Tomcat is explicitly excluded in pom.xml)
- MTurk sandbox vs production is toggled in `MturkService`
- Datastore queries require composite indexes defined in `index.yaml`
- **CI/CD pipeline** configured via GitHub Actions (`.github/workflows/ci.yml` and `deploy.yml`)
- No test framework is present — be careful when modifying business logic
- **Spring Boot 3.4.1 / Jakarta namespace:** The project uses `jakarta.*` imports (not `javax.*`). Objectify 6.1.3 ships with both — use `ObjectifyService.Filter` (jakarta) not the deprecated `ObjectifyFilter` (javax)

## Cloud Credentials

Managed by **[cloud-bootstrap](https://github.com/ipeirotis/cloud-bootstrap)** (v1.2.2). The skill is installed in `.claude/skills/cloud-bootstrap/`.

- **Provider:** GCP
- **Project ID:** `mturk-demographics`
- **Service account:** `claude-agent@mturk-demographics.iam.gserviceaccount.com`
- **Config:** `.cloud-config.json`
- **Encrypted credentials:** `.cloud-credentials.<email>.enc` (per-user, AES-256-CBC, passphrase in `GCP_CREDENTIALS_KEY` or `CLOUD_CREDENTIALS_KEY` env var)

This is a multi-user setup: each team member has their own `.cloud-credentials.<email>.enc` file with their own passphrase.

### Roles Granted

| Role | Why |
|---|---|
| `roles/datastore.user` | Read/write Datastore entities (Survey, Question, UserAnswer, etc.) |
| `roles/cloudtasks.enqueuer` | Enqueue Cloud Tasks for background jobs |
| `roles/secretmanager.admin` | Read, create, and update secrets (AWS creds, future secrets) |
| `roles/appengine.deployer` | Deploy the app to App Engine |
| `roles/appengine.serviceAdmin` | Manage App Engine service versions |
| `roles/cloudbuild.builds.builder` | App Engine deployments use Cloud Build |
| `roles/storage.objectViewer` | Read Datastore export backups from GCS |
| `roles/bigquery.dataEditor` | Write to BigQuery tables for daily export |
| `roles/bigquery.jobUser` | Run BigQuery queries |

### How to Authenticate

Authentication is handled automatically by the cloud-bootstrap skill via the SessionStart hook. To authenticate manually:

```bash
USER_EMAIL=$(git config user.email)
echo "$GCP_CREDENTIALS_KEY" | openssl enc -d -aes-256-cbc -pbkdf2 \
  -pass stdin -in ".cloud-credentials.${USER_EMAIL}.enc" -out /tmp/credentials.json
gcloud auth activate-service-account --key-file=/tmp/credentials.json
gcloud config set project "$(jq -r .project_id .cloud-config.json)"
rm -f /tmp/credentials.json
```

### Adding New Team Members

The cloud-bootstrap skill handles this automatically via the **Add Team Member** workflow.

### Permission Escalation

If you hit a 403, stop and report:
1. The exact error
2. The role needed and why
3. Ask the user for a new bootstrap token (`gcloud auth print-access-token`)

Never modify IAM policies directly. Prefer granular roles over basic roles.

## Task Progress

See [TASKS.md](TASKS.md) for the full task list. Summary:

- [x] **T0.1** — Fix yuicompressor-maven-plugin build failure (1.3.2 → 1.5.1)
- [x] **T3.6** — Upgrade appengine-maven-plugin 2.4.0 → 2.8.1 (fixes deploy failure)
- [x] **Track 1** — CI/CD Pipeline (T1.1–T1.3)
- [x] **Track 2** — Configuration & Security (T2.1–T2.4 completed)
- [x] **Track 3** — Dependency Updates (T3.1–T3.5 completed)
- [x] **Track 4** — Java 21 + Spring Boot 3.x Migration (T4.1–T4.7 completed)
- [x] **Track 5** — AWS SDK Update (T5.1–T5.3 completed)
- [x] **Track 6** — Google Cloud Libraries Update (T6.1–T6.3 completed)
- [ ] **Track 7** — Frontend Modernization (T7.1 done: Vue 3 migration, T7.2–T7.3 done, T7.4–T7.8 done, T7.11 done, T7.15 done, T7.16–T7.19 done, T7.20–T7.22 done)
- [x] **Track 8** — Data Access & API Quality (T8.1–T8.6: CORS, OpenAPI, counts endpoint, CSV export, enhanced filtering, BigQuery export)
- [x] **Track 9** — Robustness & Reliability (all tasks completed: T9.1–T9.14)
- [x] **Track 11** — Observability & Operations (T11.1–T11.6: Actuator health, custom indicators, Micrometer/Stackdriver metrics, structured JSON logging, correlation IDs, task status endpoint)
- [ ] **Track 12** — API Security & Documentation (T12.1–T12.3, T12.6, T12.17 done)
