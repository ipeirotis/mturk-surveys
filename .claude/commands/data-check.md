Check data integrity by comparing Datastore and BigQuery for recent dates.

Run a BigQuery query to check the last 7 days of data:

```
bq query --use_legacy_sql=false '
SELECT date, COUNT(*) as responses
FROM demographics.responses
WHERE date >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY)
GROUP BY date
ORDER BY date DESC
'
```

Report:
- Whether data is flowing daily (expect responses every day)
- Any days with zero or unusually low response counts (typical range: 50-200/day)
- The most recent date with data

If there are gaps, suggest using the `/tasks/smartRestoreFromBigQuery` endpoint to backfill.
