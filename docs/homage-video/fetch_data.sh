#!/usr/bin/env bash
# Pulls the aggregates behind the MTurk Tracker homage video from BigQuery.
# Output is aggregate-only (no worker IDs) and lands in ./data/.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p data
T='`mturk-demographics.demographics.responses`'
Q() { bq query --nouse_legacy_sql --format=csv --max_rows=100000 --quiet "$1"; }

# Overall totals
Q "SELECT COUNT(*) responses, COUNT(DISTINCT worker_id) workers, COUNT(DISTINCT hit_id) hits,
     COUNT(DISTINCT IF(country NOT IN ('', 'ZZ', '?'), country, NULL)) countries,
     COUNT(DISTINCT DATE(date)) days, MIN(DATE(date)) first_day, MAX(DATE(date)) last_day
   FROM $T" > data/totals.csv

# How many times each worker took the survey
Q "SELECT cnt AS cnt_tasks, COUNT(*) AS cnt_workers
   FROM (SELECT worker_id, COUNT(*) cnt FROM $T WHERE worker_id IS NOT NULL GROUP BY worker_id)
   GROUP BY cnt ORDER BY cnt" > data/taskdist.csv

# The most loyal worker
Q "SELECT COUNT(*) cnt, MIN(DATE(date)) first_day, MAX(DATE(date)) last_day
   FROM $T GROUP BY worker_id ORDER BY cnt DESC LIMIT 1" > data/top_worker.csv

# Daily responses and first-time workers
Q "WITH f AS (SELECT worker_id, MIN(DATE(date)) fd FROM $T GROUP BY 1)
   SELECT DATE(date) d, COUNT(*) n, COUNT(DISTINCT IF(f.fd = DATE(r.date), r.worker_id, NULL)) new_w
   FROM $T r LEFT JOIN f USING (worker_id) GROUP BY 1 ORDER BY 1" > data/daily.csv

# Time between a worker's first and last answer, in 30-day buckets
Q "SELECT span_bucket, COUNT(*) w FROM (
     SELECT DIV(DATE_DIFF(MAX(DATE(date)), MIN(DATE(date)), DAY), 30) span_bucket FROM $T
     WHERE worker_id IS NOT NULL GROUP BY worker_id)
   GROUP BY 1 ORDER BY 1" > data/span.csv

# Capture-recapture (Lincoln-Petersen) on consecutive months
Q "WITH r AS (SELECT DISTINCT DATE_TRUNC(DATE(date), MONTH) m, worker_id FROM $T),
   c AS (SELECT m, COUNT(*) n FROM r GROUP BY m)
   SELECT a.m, ca.n n1, cb.n n2, COUNT(b.worker_id) both_m, SAFE_DIVIDE(ca.n * cb.n, COUNT(b.worker_id)) lp
   FROM r a JOIN c ca ON ca.m = a.m JOIN c cb ON cb.m = DATE_ADD(a.m, INTERVAL 1 MONTH)
   LEFT JOIN r b ON b.worker_id = a.worker_id AND b.m = DATE_ADD(a.m, INTERVAL 1 MONTH)
   GROUP BY 1, 2, 3 ORDER BY 1" > data/lp.csv

# Monthly demographics
Q "SELECT DATE_TRUNC(DATE(date), MONTH) m, COUNT(*) n, COUNT(DISTINCT worker_id) w,
     COUNTIF(LOWER(gender) = 'female') / COUNT(*) female,
     COUNTIF(country = 'US') / COUNT(*) us, COUNTIF(country = 'IN') / COUNT(*) india,
     COUNTIF(country NOT IN ('US', 'IN')) / COUNT(*) other,
     COUNTIF(marital_status = 'married') / COUNT(*) married,
     COUNTIF(educational_level IN ('Bachelors degree', 'Graduate degree, Masters', 'Graduate degree, Doctorate'))
       / NULLIF(COUNTIF(educational_level != ''), 0) college
   FROM $T GROUP BY m ORDER BY m" > data/monthly.csv

# Per-year breakdowns
Q "SELECT EXTRACT(YEAR FROM date) y, country, COUNT(*) n, COUNT(DISTINCT worker_id) w FROM $T GROUP BY 1, 2" > data/country_year.csv
Q "SELECT EXTRACT(YEAR FROM date) y, year_of_birth, COUNT(*) n FROM $T GROUP BY 1, 2" > data/yob_year.csv
for f in time_spent_on_mturk weekly_income_from_mturk; do
  Q "SELECT EXTRACT(YEAR FROM date) y, $f v, COUNT(*) n FROM $T GROUP BY 1, 2" > "data/cat_$f.csv"
done
echo "done: $(ls data | wc -l) files in data/"
