# CLAUDE.md

## Project Overview

**mturk-surveys** is a Java Spring Boot web application that runs continuous demographic surveys of Amazon Mechanical Turk workers. It creates HITs (Human Intelligence Tasks) on MTurk, collects worker responses, and provides aggregated demographics analytics through a web dashboard.

Deployed on **Google App Engine** (Java 21 runtime, GCP project `mturk-demographics`) with **Google Cloud Datastore** for persistence. The production URL is **https://demographics.mturk-tracker.com/**. The demographics survey has been running since **2015** — there is no useful data before that year.

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
│   └── tasks/                       # Background task controllers (cron-triggered)
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
├── cron.yaml                        # Create HIT every 15min, delete HITs daily at 03:00
└── index.yaml                       # Datastore composite indexes
```

## Key API Endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/survey/{surveyId}` | GET | Get survey details |
| `/api/survey` | POST | Create a new survey |
| `/api/survey/demographics/answers` | GET | Paginated user answers |
| `/api/survey/demographics/aggregatedAnswers` | GET | Aggregated demographics by period |
| `/saveAnswer` | GET | Save worker answer (JSONP) |
| `/getAnswer` | GET | Get previous worker answer |
| `/getHIT/{hitId}` | GET | Get MTurk HIT details |
| `/listHITs` | GET | List all HITs |
| `/tasks/createHIT` | GET | Create HIT (cron-triggered) |
| `/tasks/deleteHITs` | GET | Delete old HITs (cron-triggered) |
| `/tasks/approveAssignments` | GET | Approve completed assignments |

## Architecture & Conventions

### Code Patterns
- **MVC with service layer:** Controllers -> Services -> DAOs -> Objectify/Datastore
- **Objectify entities** use `@Entity`, `@Cache`, `@Id`, `@Index` annotations
- **Generic DAO base class:** `OfyBaseDao<T>` provides CRUD for all entities
- **Global exception handling** via `@ControllerAdvice` in `RestResponseEntityExceptionHandler`
- **Background tasks** are triggered by cron (cron.yaml) and use Google Cloud Tasks for retries

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

#### GCP Secret Manager Secrets
AWS credentials are stored in **GCP Secret Manager** (not in env vars). The app reads them at startup via `AwsCredentialsConfig`. Create these secrets in the `mturk-demographics` project:

| Secret ID | Description |
|---|---|
| `aws-access-key-id` | AWS access key for the MTurk requester account |
| `aws-secret-access-key` | AWS secret key for the MTurk requester account |

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
| **Datastore export** | Weekly (Sun 06:00 UTC) | `gs://demographics_data_export/<date>/` | Full raw entity backup of all Datastore kinds |
| **BigQuery export** | Daily (05:00 UTC) | `demographics.responses` table | Individual UserAnswer rows with hashed worker IDs |
| **DemographicsSnapshot** | Daily (04:00 UTC) | Datastore | Pre-aggregated daily demographic counts |
| **DemographicsRollup** | Daily (04:15 UTC) | Datastore | Weekly/monthly aggregates built from snapshots |

### BigQuery Tables

| Table | Description |
|---|---|
| `demographics.responses` | Public dataset. Daily export of UserAnswer data (worker IDs + IPs SHA256-hashed). Covers 2015-03-26 to present. |
| `test.UserAnswer_2025MAR20` | One-time Datastore backup from 2025-03-20 (raw entity export via GCS). Covers 2020-11-03 to 2025-03-20. |
| `test.userAnswers_oct2020` | Older Datastore backup. Covers 2015-03-26 to 2021-06-10. |

### GCS Bucket

- **`gs://demographics_data_export/`** — Stores weekly Datastore exports. Each export creates a timestamped subfolder with all entity data in Datastore's native export format.

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
# Compare Datastore vs BigQuery counts for a date range
GET /tasks/compareDatastoreBigQuery?from=2024-01-01&to=2024-12-31

# Restore a single day from BigQuery backup
GET /tasks/restoreDateFromBigQuery?date=2024-06-15

# Smart restore: only restore days where Datastore has fewer entries
GET /tasks/smartRestoreFromBigQuery?from=2024-01-01&to=2024-12-31
```

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

## GCP

- **Project ID:** `mturk-demographics`
- **Service account:** `claude-agent@mturk-demographics.iam.gserviceaccount.com`
- **Encrypted key:** `.gcp-sa-key.enc` (AES-256-CBC, passphrase in `GCP_CREDENTIALS_KEY` env var)
- **Config:** `.gcp-config.json` (project ID and service account email)

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

```bash
# 1. Decrypt the key
openssl enc -d -aes-256-cbc -pbkdf2 \
  -pass env:GCP_CREDENTIALS_KEY \
  -in .gcp-sa-key.enc -out /tmp/sa-key.json

# 2. Activate the service account
gcloud auth activate-service-account --key-file=/tmp/sa-key.json

# 3. Set the project
gcloud config set project $(jq -r .project_id .gcp-config.json)

# 4. Delete the plaintext key immediately
rm /tmp/sa-key.json
```

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
- [ ] **Track 9** — Robustness & Reliability (T9.3 done, T9.6 done)
