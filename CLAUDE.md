# CLAUDE.md

## Project Overview

**mturk-surveys** is a Java Spring Boot web application that runs continuous demographic surveys of Amazon Mechanical Turk workers. It creates HITs (Human Intelligence Tasks) on MTurk, collects worker responses, and provides aggregated demographics analytics through a web dashboard.

Deployed on **Google App Engine** (Java 21 runtime, GCP project `mturk-demographics`) with **Google Cloud Datastore** for persistence. The production URL is **https://demographics.mturk-tracker.com/**. The demographics survey has been running since **2015** — there is no useful data before that year.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4.1, Jetty (not Tomcat)
- **ORM:** Objectify 6.1.3 (Google Cloud Datastore)
- **Cloud:** Google App Engine, Google Cloud Tasks, Google Secret Manager
- **AWS:** MTurk SDK 2.35.6
- **Frontend:** AngularJS 1.2.15, Bootstrap 3.1.1, Google Charts
- **Build:** Maven, YUI Compressor (JS/CSS minification)
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
│   ├── index.html                   # Main page (AngularJS app)
│   ├── css/style.css
│   ├── js/                          # AngularJS modules, controllers, services, directives
│   ├── views/                       # Partial templates (chart.html)
│   └── lib/                         # Vendored JS (ui-bootstrap)
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

## Important Notes

- The frontend JS files are **minified and concatenated** during build via YUI Compressor into `target/classes/static/js/script.js`
- The app uses **Jetty** (Tomcat is explicitly excluded in pom.xml)
- MTurk sandbox vs production is toggled in `MturkService`
- Datastore queries require composite indexes defined in `index.yaml`
- **CI/CD pipeline** configured via GitHub Actions (`.github/workflows/ci.yml` and `deploy.yml`)
- No test framework is present — be careful when modifying business logic
- **YUI Compressor plugin** was upgraded from 1.3.2 to 1.5.1 to fix Maven 3.9+ compatibility (see TASKS.md T0.1)
- **Spring Boot 3.4.1 / Jakarta namespace:** The project uses `jakarta.*` imports (not `javax.*`). Objectify 6.1.3 ships with both — use `ObjectifyService.Filter` (jakarta) not the deprecated `ObjectifyFilter` (javax)

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
- [ ] **Track 7** — Frontend Modernization (T7.1–T7.3, T7.4–T7.6 done, T7.8 done, T7.11 done, T7.16–T7.18 done)
- [x] **Track 8** — Data Access & API Quality (T8.1–T8.6: CORS, OpenAPI, counts endpoint, CSV export, enhanced filtering, BigQuery export)
