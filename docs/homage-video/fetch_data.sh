#!/usr/bin/env bash
# Pulls the aggregates behind the MTurk Tracker homage video from BigQuery.
# Every query runs over merged.sql's all_answers (public table + Datastore backups).
# Output is aggregate-only (no worker IDs) and lands in ./data/.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p data
T='all_answers'
Q() { { cat merged.sql; echo "$1"; } | bq query --nouse_legacy_sql --format=csv --max_rows=100000 --quiet; }

# Overall totals
Q "SELECT COUNT(*) responses, COUNT(DISTINCT worker_id) workers, COUNT(DISTINCT hit_id) hits,
     COUNT(DISTINCT IF(country NOT IN ('', 'ZZ', '?'), country, NULL)) countries,
     COUNT(DISTINCT DATE(date)) days, MIN(DATE(date)) first_day, MAX(DATE(date)) last_day
   FROM $T" > data/totals.csv

# How many times each worker took the survey
Q "SELECT cnt AS cnt_tasks, COUNT(*) AS cnt_workers
   FROM (SELECT worker_id, COUNT(*) cnt FROM $T GROUP BY worker_id)
   GROUP BY cnt ORDER BY cnt" > data/taskdist.csv

# The most loyal worker
Q "SELECT COUNT(*) cnt, MIN(DATE(date)) first_day, MAX(DATE(date)) last_day
   FROM $T GROUP BY worker_id ORDER BY cnt DESC LIMIT 1" > data/top_worker.csv

# Daily responses and first-time workers
Q "SELECT d, COUNT(*) n, COUNT(DISTINCT IF(fd = d, worker_id, NULL)) new_w FROM (
     SELECT worker_id, DATE(date) d, MIN(DATE(date)) OVER (PARTITION BY worker_id) fd FROM $T)
   GROUP BY 1 ORDER BY 1" > data/daily.csv

# Time between a worker's first and last answer, in 30-day buckets
Q "SELECT span_bucket, COUNT(*) w FROM (
     SELECT DIV(DATE_DIFF(MAX(DATE(date)), MIN(DATE(date)), DAY), 30) span_bucket FROM $T GROUP BY worker_id)
   GROUP BY 1 ORDER BY 1" > data/span.csv

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

# United States vs India, one row per country (2015-2022, when India was a real share)
Q "SELECT country, COUNT(*) n, COUNT(DISTINCT worker_id) workers,
     COUNTIF(LOWER(gender) = 'female') / COUNT(*) female,
     APPROX_QUANTILES(EXTRACT(YEAR FROM date) - SAFE_CAST(year_of_birth AS INT64), 100)[OFFSET(50)] median_age,
     COUNTIF(marital_status = 'married') / COUNT(*) married,
     COUNTIF(educational_level IN ('Bachelors degree', 'Graduate degree, Masters', 'Graduate degree, Doctorate'))
       / NULLIF(COUNTIF(educational_level != ''), 0) college,
     COUNTIF(educational_level IN ('Graduate degree, Masters', 'Graduate degree, Doctorate'))
       / NULLIF(COUNTIF(educational_level != ''), 0) graduate,
     COUNTIF(time_spent_on_mturk IN ('20-40 hours per week', 'More than 40 hours per week'))
       / NULLIF(COUNTIF(time_spent_on_mturk != ''), 0) hours20,
     COUNTIF(household_income = 'Less than \$10,000') / COUNT(*) income_lt10k,
     COUNTIF(household_size IN ('4', '5+')) / COUNT(*) household4
   FROM $T WHERE country IN ('US', 'IN') AND date < '2023-01-01' GROUP BY 1" > data/us_india.csv

# US households
Q "SELECT 'income' q, household_income v, COUNT(*) n FROM $T WHERE country = 'US' GROUP BY 2
   UNION ALL
   SELECT 'size', household_size, COUNT(*) FROM $T WHERE country = 'US' GROUP BY 2" > data/us_household.csv

# Languages spoken (multi-select), by country group
Q "SELECT IF(country = 'IN', 'IN', IF(country = 'US', 'US', 'other')) grp, TRIM(l) lang, COUNT(*) n
   FROM $T, UNNEST(SPLIT(languages_spoken, ',')) l WHERE TRIM(l) != '' GROUP BY 1, 2" > data/languages.csv
# How many languages each answer listed
Q "SELECT ARRAY_LENGTH(SPLIT(languages_spoken, ',')) k, COUNT(*) n FROM $T
   WHERE TRIM(languages_spoken) != '' GROUP BY 1 ORDER BY 1" > data/lang_counts.csv
echo "done: $(ls data | wc -l) files in data/"
