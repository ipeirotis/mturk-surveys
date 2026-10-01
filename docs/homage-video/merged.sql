-- All demographics answers: the public table plus the two Datastore backups,
-- with backup worker IDs hashed the same way (SHA-256 hex), one row per (worker, HIT).
WITH src AS (
  SELECT date, worker_id, hit_id, country, year_of_birth, gender, marital_status,
         household_size, household_income, educational_level, time_spent_on_mturk,
         weekly_income_from_mturk, languages_spoken, 1 AS pri
  FROM `mturk-demographics.demographics.responses`
  WHERE worker_id IS NOT NULL
  UNION ALL
  SELECT date, TO_HEX(SHA256(workerId)), hitId, locationCountry, answers.yearOfBirth,
         answers.gender, answers.maritalStatus, answers.householdSize, answers.householdIncome,
         answers.educationalLevel, answers.timeSpentOnMturk, answers.weeklyIncomeFromMturk,
         answers.languagesSpoken, 2
  FROM `mturk-demographics.test.userAnswers_oct2020`
  WHERE surveyId = 'demographics' AND workerId IS NOT NULL
  UNION ALL
  SELECT date, TO_HEX(SHA256(workerId)), hitId, locationCountry, answers.yearOfBirth,
         answers.gender, answers.maritalStatus, answers.householdSize, answers.householdIncome,
         answers.educationalLevel, answers.timeSpentOnMturk, answers.weeklyIncomeFromMturk,
         answers.languagesSpoken, 3
  FROM `mturk-demographics.test.UserAnswer_2025MAR20`
  WHERE surveyId = 'demographics' AND workerId IS NOT NULL
),
all_answers AS (
  SELECT * EXCEPT (pri) FROM src
  QUALIFY ROW_NUMBER() OVER (PARTITION BY worker_id, COALESCE(hit_id, CAST(date AS STRING))
                             ORDER BY pri) = 1
)
