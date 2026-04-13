Check the health of all backup systems. Run these checks and report a summary:

1. **Datastore exports**: Run `gsutil ls gs://demographics_data_export/ | tail -5` to show the most recent weekly backups.
2. **Snapshot coverage**: Describe how to check via `GET /tasks/snapshotCoverage` (requires admin key header).
3. **BigQuery freshness**: Run `bq query --use_legacy_sql=false 'SELECT MAX(date) as latest_date, COUNT(*) as total_rows FROM demographics.responses'` to check the latest export date.

Report a table summarizing: backup type, last run date, and status (OK/STALE/MISSING).
