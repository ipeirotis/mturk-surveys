# CLAUDE.md

## Project Overview

**mturk-surveys** is a Java Spring Boot web application that runs continuous demographic surveys of Amazon Mechanical Turk workers. It creates HITs (Human Intelligence Tasks) on MTurk, collects worker responses, and provides aggregated demographics analytics through a web dashboard.

Deployed on **Google App Engine** (Java 11 runtime) with **Google Cloud Datastore** for persistence.

## Tech Stack

- **Backend:** Java 11, Spring Boot 2.3.5, Jetty (not Tomcat)
- **ORM:** Objectify 6.0.6 (Google Cloud Datastore)
- **Cloud:** Google App Engine, Google Cloud Tasks
- **AWS:** MTurk SDK 2.5.49
- **Frontend:** AngularJS 1.2.15, Bootstrap 3.1.1, Google Charts
- **Build:** Maven, YUI Compressor (JS/CSS minification)
- **Templating:** FreeMarker 2.3.20

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

### Environment Variables (app.yaml)
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` — MTurk credentials
- `AWS_REGION` — defaults to `us-east-1`
- `PORT` — server port (default 8080)

## Important Notes

- The frontend JS files are **minified and concatenated** during build via YUI Compressor into `target/classes/static/js/script.js`
- The app uses **Jetty** (Tomcat is explicitly excluded in pom.xml)
- MTurk sandbox vs production is toggled in `MturkService`
- Datastore queries require composite indexes defined in `index.yaml`
- **CI/CD pipeline** configured via GitHub Actions (`.github/workflows/ci.yml` and `deploy.yml`)
- No test framework is present — be careful when modifying business logic
- **YUI Compressor plugin** was upgraded from 1.3.2 to 1.5.1 to fix Maven 3.9+ compatibility (see TASKS.md T0.1)

## Task Progress

See [TASKS.md](TASKS.md) for the full task list. Summary:

- [x] **T0.1** — Fix yuicompressor-maven-plugin build failure (1.3.2 → 1.5.1)
- [x] **T3.6** — Upgrade appengine-maven-plugin 2.4.0 → 2.8.1 (fixes deploy failure)
- [x] **Track 1** — CI/CD Pipeline (T1.1–T1.3)
- [ ] **Track 2** — Configuration & Security (T2.1–T2.2 done, T2.3–T2.4 remaining)
- [x] **Track 3** — Dependency Updates (T3.1–T3.5 completed)
- [ ] **Track 4** — Java 21 + Spring Boot 3.x Migration (T4.1–T4.7)
- [ ] **Track 5** — AWS SDK Update (T5.1–T5.3)
- [ ] **Track 6** — Google Cloud Libraries Update (T6.1–T6.3)
- [ ] **Track 7** — Frontend Modernization (T7.1–T7.3)
