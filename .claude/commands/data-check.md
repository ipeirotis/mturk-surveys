Check data integrity by comparing Datastore and BigQuery for recent dates.

1. **BigQuery side**: Run a query to check the last 7 days:

```
bq query --use_legacy_sql=false '
SELECT DATE(date) as day, COUNT(*) as responses
FROM demographics.responses
WHERE DATE(date) >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY)
GROUP BY day
ORDER BY day DESC
'
```

2. **Datastore vs BigQuery comparison**: Call the app's comparison endpoint to check for discrepancies. This requires the admin key header:

```
curl -H "X-Task-Admin-Key: <admin-key>" "https://demographics.mturk-tracker.com/tasks/compareDatastoreBigQuery?from=<7-days-ago>&to=<today>"
```

If the endpoint is not accessible, note that only the BigQuery side was checked.

Report:
- Whether data is flowing daily (expect responses every day)
- Any days with zero or unusually low response counts (typical range: 50-200/day)
- Any discrepancies between Datastore and BigQuery counts
- The most recent date with data

If there are gaps, suggest using the `/tasks/smartRestoreFromBigQuery` endpoint to backfill.
