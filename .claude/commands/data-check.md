Check data integrity of the archived survey data. Collection ended on 2026-09-30 (MTurk closed), so no new answers are expected. `demographics.responses` is the canonical table; Datastore feeds the dashboard snapshots.

In cloud sessions, prefix `bq`/`gcloud` with `env -u CLOUDSDK_AUTH_ACCESS_TOKEN` if they fail with "Invalid Credentials".

1. **Canonical table totals**: these should not change.

```
bq query --use_legacy_sql=false '
SELECT COUNT(*) AS total_rows, COUNT(DISTINCT worker_id) AS workers,
       MIN(date) AS first_answer, MAX(date) AS last_answer,
       COUNTIF(date >= TIMESTAMP("2026-10-01")) AS after_closure
FROM demographics.responses
'
```

Expect 392,998 rows, 2015-03-26 to 2026-09-30, and `after_closure = 0`. Any other number means something wrote to the table.

2. **Quarterly shape**: roughly 8.5–9K answers per quarter from 2015 Q2 to 2021 Q1. Lower quarters should only be the known pauses (2019 Q1, 2019 Q4, 2020 Q1; see CLAUDE.md "Known Data Gaps").

```
bq query --use_legacy_sql=false '
SELECT FORMAT("%d Q%d", EXTRACT(YEAR FROM date), EXTRACT(QUARTER FROM date)) AS quarter, COUNT(*) AS n
FROM demographics.responses GROUP BY 1 ORDER BY 1
'
```

3. **Datastore vs. canonical table**: the compare endpoint defaults to `demographics.responses`. It requires the admin key header. Check a few ranges, for example one year at a time:

```
curl -H "X-Task-Admin-Key: <admin-key>" "https://demographics.mturk-tracker.com/tasks/compareDatastoreBigQuery?from=2016-01-01&to=2016-12-31"
```

If the endpoint isn't reachable, say that only the BigQuery side was checked.

Report:
- Total rows, workers and date range, and whether they match the expected values
- Any quarter outside the expected range that isn't a known pause
- Days where Datastore and `demographics.responses` differ

If Datastore has rows the table lacks, `/tasks/exportDateToBigQuery?date=MM/dd/yyyy` appends only the missing pairs (it never deletes). If Datastore is missing rows, use `/tasks/smartRestoreFromBigQuery`, which restores from the Datastore backups and then appends the day to the table.
