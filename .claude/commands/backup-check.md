Check the health of the backup systems. Collection ended on 2026-09-30, so the data is static and nothing new needs exporting.

In cloud sessions, prefix `bq`/`gcloud`/`gsutil` with `env -u CLOUDSDK_AUTH_ACCESS_TOKEN` if they fail with "Invalid Credentials".

1. **Datastore exports**: run `gsutil ls gs://demographics_data_export/ | tail -5` to show the most recent backups. The weekly export job was removed; the final export is `2026-10-02-final/`, and no newer one is expected.
2. **Snapshot coverage**: describe how to check via `GET /tasks/snapshotCoverage` (requires admin key header).
3. **Canonical BigQuery table**: run `bq query --use_legacy_sql=false 'SELECT MAX(date) AS latest_date, COUNT(*) AS total_rows FROM demographics.responses'`. Expect `latest_date` on 2026-09-30 and 392,998 rows. The daily export cron was removed after the closure, so a latest date in the past is expected, not stale.

Report a table summarizing: backup type, last run date, and status (OK/STALE/MISSING). For the BigQuery table, status is OK when the totals match, and anything else is a discrepancy to investigate.
