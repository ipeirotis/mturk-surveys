package com.ipeirotis.service;

import com.google.cloud.bigquery.*;
import com.ipeirotis.dao.DemographicsRollupDao;
import com.ipeirotis.dao.DemographicsSnapshotDao;
import com.ipeirotis.dto.DemographicsChartData;
import com.ipeirotis.dto.DemographicsCountsResponse;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.entity.DemographicsRollup;
import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DemographicsSnapshotService {

    private static final Logger logger = Logger.getLogger(DemographicsSnapshotService.class.getName());

    private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final Set<String> INCOME_LABELS = new LinkedHashSet<>();

    private static final int WEEKLY_THRESHOLD = 365;   // >365 days → weekly
    private static final int MONTHLY_THRESHOLD = 730;  // >2 years → monthly

    private static final String BQ_DATASET = "demographics";
    private static final String BQ_TABLE = "responses";

    // Datastore backup table used as a second fallback when demographics.responses
    // has no data. This was loaded from the Cloud Storage export at
    // gs://demographics_data_export/2025-03-20T18:52:11_94601/
    private static final String BQ_BACKUP_DATASET = "test";
    private static final String BQ_BACKUP_TABLE = "UserAnswer_2025MAR20";

    static {
        INCOME_LABELS.add("Less than $10,000");
        INCOME_LABELS.add("$10,000-$14,999");
        INCOME_LABELS.add("$15,000-$24,999");
        INCOME_LABELS.add("$25,000-$39,999");
        INCOME_LABELS.add("$40,000-$59,999");
        INCOME_LABELS.add("$60,000-$74,999");
        INCOME_LABELS.add("$75,000-$99,999");
        INCOME_LABELS.add("$100,000 or more");
    }

    @Autowired
    private DemographicsSnapshotDao snapshotDao;

    @Autowired
    private DemographicsRollupDao rollupDao;

    @Autowired
    private SurveyService surveyService;

    /**
     * Compare daily counts between BigQuery demographics.responses and
     * DemographicsSnapshot totalResponses in Datastore for a date range.
     * Returns all days (including matches) with their counts and delta.
     *
     * @param fromDate start date in yyyy-MM-dd format
     * @param toDate   end date in yyyy-MM-dd format
     * @return list of maps with {date, snapshotCount, bigqueryCount, delta}
     */
    public List<Map<String, Object>> compareDailyCounts(String fromDate, String toDate) {
        // 1. Get BigQuery daily counts from demographics.responses
        Map<String, Long> bqCounts = getBigQueryResponseCounts(fromDate, toDate);

        // 2. Get Datastore snapshot counts
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate start = LocalDate.parse(fromDate);
        LocalDate end = LocalDate.parse(toDate);
        String displayFrom = start.format(dtf);
        String displayTo = end.format(dtf);

        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(displayFrom, displayTo);
        Map<String, Integer> snapshotCounts = new LinkedHashMap<>();
        for (DemographicsSnapshot s : snapshots) {
            snapshotCounts.put(s.getDate(), s.getTotalResponses());
        }

        // 3. Merge all dates from both sources
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(bqCounts.keySet());
        allDates.addAll(snapshotCounts.keySet());
        // Also fill in any dates in the range that have neither
        LocalDate current = start;
        while (!current.isAfter(end)) {
            allDates.add(current.toString());
            current = current.plusDays(1);
        }

        // 4. Compare
        List<Map<String, Object>> results = new ArrayList<>();
        for (String dateKey : allDates) {
            long bqCount = bqCounts.getOrDefault(dateKey, 0L);
            int snapCount = snapshotCounts.getOrDefault(dateKey, 0);
            long delta = bqCount - snapCount;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", dateKey);
            row.put("snapshotCount", snapCount);
            row.put("bigqueryCount", bqCount);
            row.put("delta", delta);
            results.add(row);
        }

        return results;
    }

    /**
     * Query BigQuery demographics.responses for daily counts.
     */
    private Map<String, Long> getBigQueryResponseCounts(String fromDate, String toDate) {
        Map<String, Long> counts = new LinkedHashMap<>();
        try {
            BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
            String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

            String sql = String.format(
                    "SELECT FORMAT_DATE('%%Y-%%m-%%d', DATE(date)) AS day, COUNT(*) AS cnt "
                    + "FROM `%s.%s.%s` "
                    + "WHERE DATE(date) >= '%s' AND DATE(date) <= '%s' "
                    + "GROUP BY day ORDER BY day",
                    projectId, BQ_DATASET, BQ_TABLE, fromDate, toDate);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult result = bigQuery.query(queryConfig);

            for (FieldValueList row : result.iterateAll()) {
                String day = row.get("day").getStringValue();
                long cnt = row.get("cnt").getLongValue();
                counts.put(day, cnt);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted querying BigQuery response counts", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to query BigQuery response counts: " + e.getMessage(), e);
        }
        return counts;
    }

    /**
     * Build and save a snapshot for the given date from raw UserAnswer data.
     * Evicts all cached chart/aggregated/counts data since the underlying data has changed.
     */
    @CacheEvict(value = {"chartData", "aggregatedAnswers", "counts"}, allEntries = true)
    public DemographicsSnapshot buildSnapshot(String dateStr) throws ParseException {
        Calendar dateFrom = Calendar.getInstance();
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        dateFrom.setTime(df.parse(dateStr));
        CalendarUtils.truncateToDay(dateFrom);

        Calendar dateTo = Calendar.getInstance();
        dateTo.setTime(dateFrom.getTime());
        dateTo.add(Calendar.DAY_OF_MONTH, 1);

        // Query without surveyId filter: old UserAnswer entities may have surveyId=null
        // and would be excluded by the composite index (surveyId, date) equality filter.
        List<UserAnswer> answers = surveyService.listAnswersByDateRange(dateFrom.getTime(), dateTo.getTime());

        DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
        String sortableDate = sortableDf.format(dateFrom.getTime());

        if (answers.isEmpty()) {
            logger.info("No responses in Datastore for " + dateStr + ", trying BigQuery");
            answers = loadFromBigQuery(sortableDate);
            if (answers.isEmpty()) {
                logger.info("No responses in BigQuery for " + dateStr + " either, skipping snapshot");
                return null;
            }
        } else {
            // Datastore may have partial data (e.g. after a restore where entity IDs
            // were reused). Check BigQuery and use whichever source has more valid entries.
            long dsValid = answers.stream().filter(this::hasDemographicAnswers).count();
            List<UserAnswer> bqAnswers = loadFromBigQuery(sortableDate);
            long bqValid = bqAnswers.stream().filter(this::hasDemographicAnswers).count();
            if (bqValid > dsValid) {
                logger.info("BigQuery has more valid entries for " + dateStr
                        + " (BQ=" + bqValid + " vs DS=" + dsValid + "), using BigQuery");
                answers = bqAnswers;
            }
        }

        // Deduplicate by (workerId, hitId), keeping the earliest entry per group.
        // This prevents inflated counts when duplicates exist in Datastore or BigQuery.
        // Only dedup entries that have both workerId and hitId populated — BigQuery-sourced
        // entries may not have these fields, so we must not collapse them.
        Map<String, UserAnswer> dedupMap = new LinkedHashMap<>();
        List<UserAnswer> noKeyEntries = new ArrayList<>();
        for (UserAnswer ua : answers) {
            if (ua.getWorkerId() == null || ua.getWorkerId().isEmpty()
                    || ua.getHitId() == null || ua.getHitId().isEmpty()) {
                noKeyEntries.add(ua);
                continue;
            }
            String key = ua.getWorkerId() + "|" + ua.getHitId();
            UserAnswer existing = dedupMap.get(key);
            if (existing == null || (ua.getDate() != null && existing.getDate() != null
                    && ua.getDate().before(existing.getDate()))) {
                dedupMap.put(key, ua);
            }
        }
        int originalSize = answers.size();
        answers = new ArrayList<>(dedupMap.values());
        answers.addAll(noKeyEntries);
        if (answers.size() < originalSize) {
            logger.info("Snapshot dedup for " + dateStr + ": " + originalSize + " -> " + answers.size()
                    + " (" + (originalSize - answers.size()) + " duplicates removed)");
        }

        DemographicsSnapshot snapshot = new DemographicsSnapshot();
        snapshot.setId(dateStr);
        snapshot.setDate(sortableDate);
        snapshot.setDayOfWeek(DAYS[dateFrom.get(Calendar.DAY_OF_WEEK) - 1]);

        Map<String, Integer> countries = new HashMap<>();
        Map<String, Integer> yearOfBirth = new HashMap<>();
        Map<String, Integer> gender = new HashMap<>();
        Map<String, Integer> maritalStatus = new HashMap<>();
        Map<String, Integer> householdSize = new HashMap<>();
        Map<String, Integer> householdIncome = new HashMap<>();
        Map<String, Integer> educationalLevel = new HashMap<>();
        Map<String, Integer> timeSpentOnMturk = new HashMap<>();
        Map<String, Integer> weeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> languagesSpoken = new HashMap<>();
        Map<String, Integer> countriesDetailed = new HashMap<>();
        Map<String, Integer> usStates = new HashMap<>();

        Map<String, Integer> hourlyTotals = new HashMap<>();
        Map<String, Integer> hourlyCountries = new HashMap<>();
        Map<String, Integer> hourlyYearOfBirth = new HashMap<>();
        Map<String, Integer> hourlyGender = new HashMap<>();
        Map<String, Integer> hourlyMaritalStatus = new HashMap<>();
        Map<String, Integer> hourlyHouseholdSize = new HashMap<>();
        Map<String, Integer> hourlyHouseholdIncome = new HashMap<>();
        Map<String, Integer> hourlyEducationalLevel = new HashMap<>();
        Map<String, Integer> hourlyTimeSpentOnMturk = new HashMap<>();
        Map<String, Integer> hourlyWeeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> hourlyLanguagesSpoken = new HashMap<>();

        List<Long> responseTimesMinutes = new ArrayList<>();

        int validCount = 0;
        for (UserAnswer ua : answers) {
            // Only count UserAnswers that have a populated answers map with at least
            // one expected demographic key. This filters out non-demographics entities
            // and old-format entities that only have locationCountry (from AppEngine
            // headers) but no demographic answers — which would inflate country/total
            // counts while leaving all other dimensions empty, causing chart gaps.
            if (!hasDemographicAnswers(ua)) {
                continue;
            }
            validCount++;

            Calendar cal = Calendar.getInstance();
            cal.setTime(ua.getDate());
            String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

            // Daily totals
            incrementCountry(ua.getLocationCountry(), countries);
            incrementCountryDetailed(ua.getLocationCountry(), countriesDetailed);
            incrementUsState(ua.getLocationCountry(), ua.getLocationRegion(), usStates);
            incrementDemographic("yearOfBirth", ua.getAnswers(), yearOfBirth, true);
            incrementDemographic("gender", ua.getAnswers(), gender, false);
            incrementDemographic("maritalStatus", ua.getAnswers(), maritalStatus, false);
            incrementDemographic("householdSize", ua.getAnswers(), householdSize, false);
            incrementDemographic("householdIncome", ua.getAnswers(), householdIncome, false);
            incrementDemographic("educationalLevel", ua.getAnswers(), educationalLevel, false);
            incrementDemographic("timeSpentOnMturk", ua.getAnswers(), timeSpentOnMturk, false);
            incrementDemographic("weeklyIncomeFromMturk", ua.getAnswers(), weeklyIncomeFromMturk, false);
            incrementMultiValue("languagesSpoken", ua.getAnswers(), languagesSpoken);

            // Hourly totals
            increment(hourlyTotals, hour);
            incrementCountry(ua.getLocationCountry(), hourlyCountries, hour);
            incrementDemographic("yearOfBirth", ua.getAnswers(), hourlyYearOfBirth, hour, true);
            incrementDemographic("gender", ua.getAnswers(), hourlyGender, hour, false);
            incrementDemographic("maritalStatus", ua.getAnswers(), hourlyMaritalStatus, hour, false);
            incrementDemographic("householdSize", ua.getAnswers(), hourlyHouseholdSize, hour, false);
            incrementDemographic("householdIncome", ua.getAnswers(), hourlyHouseholdIncome, hour, false);
            incrementDemographic("educationalLevel", ua.getAnswers(), hourlyEducationalLevel, hour, false);
            incrementDemographic("timeSpentOnMturk", ua.getAnswers(), hourlyTimeSpentOnMturk, hour, false);
            incrementDemographic("weeklyIncomeFromMturk", ua.getAnswers(), hourlyWeeklyIncomeFromMturk, hour, false);
            incrementMultiValue("languagesSpoken", ua.getAnswers(), hourlyLanguagesSpoken, hour);

            // Response time: time between HIT creation and answer submission
            collectResponseTime(ua, responseTimesMinutes);
        }

        if (validCount == 0) {
            logger.info("No valid demographics responses for " + dateStr + " in Datastore (" + answers.size() + " total UAs), falling back to BigQuery");

            List<UserAnswer> bqAnswers = loadFromBigQuery(sortableDate);
            if (bqAnswers.isEmpty()) {
                logger.info("No responses in BigQuery for " + dateStr + " either, skipping snapshot");
                return null;
            }

            // Re-aggregate from BigQuery data
            countries.clear(); yearOfBirth.clear(); gender.clear(); maritalStatus.clear();
            householdSize.clear(); householdIncome.clear(); educationalLevel.clear();
            timeSpentOnMturk.clear(); weeklyIncomeFromMturk.clear(); languagesSpoken.clear();
            countriesDetailed.clear(); usStates.clear();
            hourlyTotals.clear(); hourlyCountries.clear(); hourlyYearOfBirth.clear();
            hourlyGender.clear(); hourlyMaritalStatus.clear(); hourlyHouseholdSize.clear();
            hourlyHouseholdIncome.clear(); hourlyEducationalLevel.clear();
            hourlyTimeSpentOnMturk.clear(); hourlyWeeklyIncomeFromMturk.clear();
            hourlyLanguagesSpoken.clear();
            responseTimesMinutes.clear();

            for (UserAnswer ua : bqAnswers) {
                if (!hasDemographicAnswers(ua)) {
                    continue;
                }
                validCount++;

                Calendar cal = Calendar.getInstance();
                cal.setTime(ua.getDate());
                String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

                incrementCountry(ua.getLocationCountry(), countries);
                incrementCountryDetailed(ua.getLocationCountry(), countriesDetailed);
                incrementUsState(ua.getLocationCountry(), ua.getLocationRegion(), usStates);
                incrementDemographic("yearOfBirth", ua.getAnswers(), yearOfBirth, true);
                incrementDemographic("gender", ua.getAnswers(), gender, false);
                incrementDemographic("maritalStatus", ua.getAnswers(), maritalStatus, false);
                incrementDemographic("householdSize", ua.getAnswers(), householdSize, false);
                incrementDemographic("householdIncome", ua.getAnswers(), householdIncome, false);
                incrementDemographic("educationalLevel", ua.getAnswers(), educationalLevel, false);
                incrementDemographic("timeSpentOnMturk", ua.getAnswers(), timeSpentOnMturk, false);
                incrementDemographic("weeklyIncomeFromMturk", ua.getAnswers(), weeklyIncomeFromMturk, false);
                incrementMultiValue("languagesSpoken", ua.getAnswers(), languagesSpoken);

                increment(hourlyTotals, hour);
                incrementCountry(ua.getLocationCountry(), hourlyCountries, hour);
                incrementDemographic("yearOfBirth", ua.getAnswers(), hourlyYearOfBirth, hour, true);
                incrementDemographic("gender", ua.getAnswers(), hourlyGender, hour, false);
                incrementDemographic("maritalStatus", ua.getAnswers(), hourlyMaritalStatus, hour, false);
                incrementDemographic("householdSize", ua.getAnswers(), hourlyHouseholdSize, hour, false);
                incrementDemographic("householdIncome", ua.getAnswers(), hourlyHouseholdIncome, hour, false);
                incrementDemographic("educationalLevel", ua.getAnswers(), hourlyEducationalLevel, hour, false);
                incrementDemographic("timeSpentOnMturk", ua.getAnswers(), hourlyTimeSpentOnMturk, hour, false);
                incrementDemographic("weeklyIncomeFromMturk", ua.getAnswers(), hourlyWeeklyIncomeFromMturk, hour, false);
                incrementMultiValue("languagesSpoken", ua.getAnswers(), hourlyLanguagesSpoken, hour);

                collectResponseTime(ua, responseTimesMinutes);
            }

            if (validCount == 0) {
                logger.info("No valid demographics in BigQuery data for " + dateStr + " (" + bqAnswers.size() + " rows), skipping snapshot");
                return null;
            }

            logger.info("Built snapshot for " + dateStr + " from BigQuery: " + validCount + " valid of " + bqAnswers.size() + " rows");
        }

        snapshot.setTotalResponses(validCount);
        snapshot.setCountries(countries);
        snapshot.setYearOfBirth(yearOfBirth);
        snapshot.setGender(gender);
        snapshot.setMaritalStatus(maritalStatus);
        snapshot.setHouseholdSize(householdSize);
        snapshot.setHouseholdIncome(householdIncome);
        snapshot.setEducationalLevel(educationalLevel);
        snapshot.setTimeSpentOnMturk(timeSpentOnMturk);
        snapshot.setWeeklyIncomeFromMturk(weeklyIncomeFromMturk);
        snapshot.setLanguagesSpoken(languagesSpoken);
        snapshot.setCountriesDetailed(countriesDetailed);
        snapshot.setUsStates(usStates);
        snapshot.setHourlyTotals(hourlyTotals);
        snapshot.setHourlyCountries(hourlyCountries);
        snapshot.setHourlyYearOfBirth(hourlyYearOfBirth);
        snapshot.setHourlyGender(hourlyGender);
        snapshot.setHourlyMaritalStatus(hourlyMaritalStatus);
        snapshot.setHourlyHouseholdSize(hourlyHouseholdSize);
        snapshot.setHourlyHouseholdIncome(hourlyHouseholdIncome);
        snapshot.setHourlyEducationalLevel(hourlyEducationalLevel);
        snapshot.setHourlyTimeSpentOnMturk(hourlyTimeSpentOnMturk);
        snapshot.setHourlyWeeklyIncomeFromMturk(hourlyWeeklyIncomeFromMturk);
        snapshot.setHourlyLanguagesSpoken(hourlyLanguagesSpoken);

        // Response time percentiles
        if (!responseTimesMinutes.isEmpty()) {
            Collections.sort(responseTimesMinutes);
            snapshot.setResponseTimeCount(responseTimesMinutes.size());
            snapshot.setP25ResponseTimeMinutes(percentile(responseTimesMinutes, 25));
            snapshot.setMedianResponseTimeMinutes(percentile(responseTimesMinutes, 50));
            snapshot.setP75ResponseTimeMinutes(percentile(responseTimesMinutes, 75));
        }

        snapshotDao.save(snapshot);
        logger.info("Saved snapshot for " + dateStr + " with " + answers.size() + " responses");
        return snapshot;
    }

    /**
     * Build and save a weekly rollup by merging daily snapshots for the ISO week
     * starting on the given Monday (yyyy-MM-dd format).
     * Falls back to BigQuery if daily snapshots are missing.
     */
    public DemographicsRollup buildWeeklyRollup(String mondayDate) {
        LocalDate monday = LocalDate.parse(mondayDate);
        LocalDate sunday = monday.plusDays(7);
        DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
        DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");

        String fromDisplay, toDisplay;
        try {
            fromDisplay = displayDf.format(sortableDf.parse(monday.toString()));
            toDisplay = displayDf.format(sortableDf.parse(sunday.toString()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(fromDisplay, toDisplay);
        DemographicsRollup rollup = mergeSnapshotsIntoRollup(snapshots, "weekly", mondayDate);
        if (rollup == null) {
            // No daily snapshots — try building directly from BigQuery
            rollup = buildRollupFromBigQuery("weekly", mondayDate,
                    monday.toString(), sunday.toString());
        }
        if (rollup != null) {
            rollupDao.save(rollup);
            logger.info("Saved weekly rollup for " + mondayDate + " with " + rollup.getTotalResponses() + " responses");
        }
        return rollup;
    }

    /**
     * Build and save a monthly rollup by merging daily snapshots for the given month.
     * monthStart should be yyyy-MM-dd of the 1st of the month.
     * Falls back to BigQuery if daily snapshots are missing.
     */
    public DemographicsRollup buildMonthlyRollup(String monthStart) {
        LocalDate start = LocalDate.parse(monthStart);
        LocalDate end = start.plusMonths(1);
        DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
        DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");

        String fromDisplay, toDisplay;
        try {
            fromDisplay = displayDf.format(sortableDf.parse(start.toString()));
            toDisplay = displayDf.format(sortableDf.parse(end.toString()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(fromDisplay, toDisplay);
        DemographicsRollup rollup = mergeSnapshotsIntoRollup(snapshots, "monthly", monthStart);
        if (rollup == null) {
            // No daily snapshots — try building directly from BigQuery
            rollup = buildRollupFromBigQuery("monthly", monthStart,
                    start.toString(), end.toString());
        }
        if (rollup != null) {
            rollupDao.save(rollup);
            logger.info("Saved monthly rollup for " + monthStart + " with " + rollup.getTotalResponses() + " responses");
        }
        return rollup;
    }

    private DemographicsRollup mergeSnapshotsIntoRollup(List<DemographicsSnapshot> snapshots, String granularity, String dateKey) {
        if (snapshots.isEmpty()) return null;

        int totalResponses = 0;
        Map<String, Integer> countries = new HashMap<>();
        Map<String, Integer> yearOfBirth = new HashMap<>();
        Map<String, Integer> gender = new HashMap<>();
        Map<String, Integer> maritalStatus = new HashMap<>();
        Map<String, Integer> householdSize = new HashMap<>();
        Map<String, Integer> householdIncome = new HashMap<>();
        Map<String, Integer> educationalLevel = new HashMap<>();
        Map<String, Integer> timeSpentOnMturk = new HashMap<>();
        Map<String, Integer> weeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> languagesSpoken = new HashMap<>();
        Map<String, Integer> countriesDetailed = new HashMap<>();
        Map<String, Integer> usStates = new HashMap<>();

        // Weighted response time accumulators
        long rtWeightedMedian = 0, rtWeightedP25 = 0, rtWeightedP75 = 0;
        int rtTotalCount = 0;

        for (DemographicsSnapshot snap : snapshots) {
            totalResponses += snap.getTotalResponses();
            mergeCounts(snap.getCountries(), countries);
            mergeCounts(snap.getYearOfBirth(), yearOfBirth);
            mergeCounts(snap.getGender(), gender);
            mergeCounts(snap.getMaritalStatus(), maritalStatus);
            mergeCounts(snap.getHouseholdSize(), householdSize);
            mergeCounts(snap.getHouseholdIncome(), householdIncome);
            mergeCounts(snap.getEducationalLevel(), educationalLevel);
            mergeCounts(snap.getTimeSpentOnMturk(), timeSpentOnMturk);
            mergeCounts(snap.getWeeklyIncomeFromMturk(), weeklyIncomeFromMturk);
            mergeCounts(snap.getLanguagesSpoken(), languagesSpoken);
            mergeCounts(snap.getCountriesDetailed(), countriesDetailed);
            mergeCounts(snap.getUsStates(), usStates);

            // Weighted-average response time percentiles
            Integer rtCount = snap.getResponseTimeCount();
            if (rtCount != null && rtCount > 0) {
                rtTotalCount += rtCount;
                if (snap.getMedianResponseTimeMinutes() != null) rtWeightedMedian += snap.getMedianResponseTimeMinutes() * rtCount;
                if (snap.getP25ResponseTimeMinutes() != null) rtWeightedP25 += snap.getP25ResponseTimeMinutes() * rtCount;
                if (snap.getP75ResponseTimeMinutes() != null) rtWeightedP75 += snap.getP75ResponseTimeMinutes() * rtCount;
            }
        }

        DemographicsRollup rollup = new DemographicsRollup();
        rollup.setId(granularity + ":" + dateKey);
        rollup.setGranularity(granularity);
        rollup.setDate(dateKey);
        rollup.setTotalResponses(totalResponses);
        rollup.setCountries(countries);
        rollup.setYearOfBirth(yearOfBirth);
        rollup.setGender(gender);
        rollup.setMaritalStatus(maritalStatus);
        rollup.setHouseholdSize(householdSize);
        rollup.setHouseholdIncome(householdIncome);
        rollup.setEducationalLevel(educationalLevel);
        rollup.setTimeSpentOnMturk(timeSpentOnMturk);
        rollup.setWeeklyIncomeFromMturk(weeklyIncomeFromMturk);
        rollup.setLanguagesSpoken(languagesSpoken);
        rollup.setCountriesDetailed(countriesDetailed);
        rollup.setUsStates(usStates);
        if (rtTotalCount > 0) {
            rollup.setMedianResponseTimeMinutes(rtWeightedMedian / rtTotalCount);
            rollup.setP25ResponseTimeMinutes(rtWeightedP25 / rtTotalCount);
            rollup.setP75ResponseTimeMinutes(rtWeightedP75 / rtTotalCount);
            rollup.setResponseTimeCount(rtTotalCount);
        }
        return rollup;
    }

    /**
     * Combined endpoint: returns both aggregated percentages and raw counts from a single
     * Datastore read. Cached in memory so repeated requests for the same range are instant.
     */
    @Cacheable(value = "chartData", key = "#from + '_' + #to")
    public DemographicsChartData getChartData(String from, String to) {
        String granularity = estimateGranularity(from, to);

        // Try rollups for weekly/monthly ranges — much faster than loading daily snapshots
        if (!"daily".equals(granularity)) {
            List<DemographicsRollup> rollups = rollupDao.listByGranularityAndDateRange(granularity, from, to);
            if (!rollups.isEmpty()) {
                DemographicsChartData result = new DemographicsChartData();
                result.setAggregated(buildAggregatedFromRollups(rollups, granularity));
                result.setCounts(buildCountsFromRollups(rollups, granularity));
                return result;
            }
            logger.info("No " + granularity + " rollups found for " + from + "-" + to + ", falling back to daily snapshots");
        }

        // Fallback: load daily snapshots (small ranges, or rollups not yet built)
        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(from, to);
        granularity = chooseGranularity(snapshots.size());

        DemographicsChartData result = new DemographicsChartData();
        result.setAggregated(buildAggregated(snapshots, granularity));
        result.setCounts(buildCounts(snapshots, granularity));
        return result;
    }

    /**
     * Get aggregated answers from pre-computed snapshots.
     * Auto-selects granularity: ≤365 days → daily, 366–730 → weekly, >730 → monthly.
     */
    @Cacheable(value = "aggregatedAnswers", key = "#from + '_' + #to")
    public DemographicsSurveyAnswersByPeriod getAggregatedAnswers(String from, String to) {
        String granularity = estimateGranularity(from, to);
        if (!"daily".equals(granularity)) {
            List<DemographicsRollup> rollups = rollupDao.listByGranularityAndDateRange(granularity, from, to);
            if (!rollups.isEmpty()) {
                return buildAggregatedFromRollups(rollups, granularity);
            }
        }
        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(from, to);
        return buildAggregated(snapshots, chooseGranularity(snapshots.size()));
    }

    /**
     * Get raw count data from pre-computed snapshots.
     * Auto-selects granularity to match aggregatedAnswers.
     */
    @Cacheable(value = "counts", key = "#from + '_' + #to")
    public DemographicsCountsResponse getCounts(String from, String to) {
        String granularity = estimateGranularity(from, to);
        if (!"daily".equals(granularity)) {
            List<DemographicsRollup> rollups = rollupDao.listByGranularityAndDateRange(granularity, from, to);
            if (!rollups.isEmpty()) {
                return buildCountsFromRollups(rollups, granularity);
            }
        }
        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(from, to);
        return buildCounts(snapshots, chooseGranularity(snapshots.size()));
    }

    /**
     * Estimate granularity from date strings without loading data.
     */
    private String estimateGranularity(String from, String to) {
        try {
            DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
            long days = ChronoUnit.DAYS.between(
                    new java.sql.Date(df.parse(from).getTime()).toLocalDate(),
                    new java.sql.Date(df.parse(to).getTime()).toLocalDate());
            if (days > MONTHLY_THRESHOLD) return "monthly";
            if (days > WEEKLY_THRESHOLD) return "weekly";
            return "daily";
        } catch (ParseException e) {
            return "daily";
        }
    }

    // --- Internal builders ---

    private DemographicsSurveyAnswersByPeriod buildAggregated(List<DemographicsSnapshot> snapshots, String granularity) {
        DemographicsSurveyAnswersByPeriod result = new DemographicsSurveyAnswersByPeriod();
        result.setDailyGranularity(granularity);

        switch (granularity) {
            case "monthly":
                result.setDaily(buildGroupedPercentages(snapshots, "monthly"));
                break;
            case "weekly":
                result.setDaily(buildGroupedPercentages(snapshots, "weekly"));
                break;
            default:
                result.setDaily(buildDailyAggregation(snapshots));
                break;
        }

        result.setHourly(buildHourlyAggregation(snapshots));
        result.setWeekly(buildWeeklyAggregation(snapshots));
        return result;
    }

    private DemographicsCountsResponse buildCounts(List<DemographicsSnapshot> snapshots, String granularity) {
        if ("daily".equals(granularity)) {
            return buildDailyCounts(snapshots, granularity);
        }
        return buildGroupedCounts(snapshots, granularity);
    }

    private String chooseGranularity(int numSnapshots) {
        if (numSnapshots > MONTHLY_THRESHOLD) return "monthly";
        if (numSnapshots > WEEKLY_THRESHOLD) return "weekly";
        return "daily";
    }

    // --- Rollup-based builders (no re-grouping needed, each rollup = one chart period) ---

    private DemographicsSurveyAnswersByPeriod buildAggregatedFromRollups(List<DemographicsRollup> rollups, String granularity) {
        DemographicsSurveyAnswersByPeriod result = new DemographicsSurveyAnswersByPeriod();
        result.setDailyGranularity(granularity);

        // Each rollup is already a period — convert directly to percentages
        Map<String, Map<String, Float>> countries = new LinkedHashMap<>();
        Map<String, Map<String, Float>> yearOfBirth = new LinkedHashMap<>();
        Map<String, Map<String, Float>> gender = new LinkedHashMap<>();
        Map<String, Map<String, Float>> maritalStatus = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdSize = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdIncome = new LinkedHashMap<>();
        Map<String, Map<String, Float>> educationalLevel = new LinkedHashMap<>();
        Map<String, Map<String, Float>> timeSpentOnMturk = new LinkedHashMap<>();
        Map<String, Map<String, Float>> weeklyIncomeFromMturk = new LinkedHashMap<>();
        Map<String, Map<String, Float>> languagesSpoken = new LinkedHashMap<>();
        Map<String, Set<String>> labels = new HashMap<>();

        // Sort rollups by date
        rollups.sort(Comparator.comparing(DemographicsRollup::getDate));

        for (DemographicsRollup r : rollups) {
            if (r.getTotalResponses() == 0) continue;
            // Skip rollups where all demographic maps are empty — these were built
            // from snapshots that only had geolocation data (non-demographics entities)
            if (!hasAnyDemographicData(r)) continue;
            // Use Date.toString() key for frontend compatibility
            String key = rollupDateToKey(r.getDate(), granularity);
            countries.put(key, toPercentageMap(r.getCountries(), labels, "countries"));
            yearOfBirth.put(key, toPercentageMap(r.getYearOfBirth(), labels, "yearOfBirth"));
            gender.put(key, toPercentageMap(r.getGender(), labels, "gender"));
            maritalStatus.put(key, toPercentageMap(r.getMaritalStatus(), labels, "maritalStatus"));
            householdSize.put(key, toPercentageMap(r.getHouseholdSize(), labels, "householdSize"));
            householdIncome.put(key, toPercentageMap(r.getHouseholdIncome(), labels, "householdIncome"));
            educationalLevel.put(key, toPercentageMap(r.getEducationalLevel(), labels, "educationalLevel"));
            timeSpentOnMturk.put(key, toPercentageMap(r.getTimeSpentOnMturk(), labels, "timeSpentOnMturk"));
            weeklyIncomeFromMturk.put(key, toPercentageMap(r.getWeeklyIncomeFromMturk(), labels, "weeklyIncomeFromMturk"));
            languagesSpoken.put(key, toPercentageMap(r.getLanguagesSpoken(), labels, "languagesSpoken"));
        }

        filterIncomeLabels(labels);
        DemographicsSurveyAnswers daily = new DemographicsSurveyAnswers();
        daily.setCountries(countries);
        daily.setYearOfBirth(yearOfBirth);
        daily.setGender(gender);
        daily.setMaritalStatus(maritalStatus);
        daily.setHouseholdSize(householdSize);
        daily.setHouseholdIncome(householdIncome);
        daily.setEducationalLevel(educationalLevel);
        daily.setTimeSpentOnMturk(timeSpentOnMturk);
        daily.setWeeklyIncomeFromMturk(weeklyIncomeFromMturk);
        daily.setLanguagesSpoken(languagesSpoken);
        daily.setLabels(labels);
        result.setDaily(daily);

        // Hourly and day-of-week aggregations aren't available from rollups;
        // set empty so frontend doesn't break
        result.setHourly(new DemographicsSurveyAnswers());
        result.setWeekly(new DemographicsSurveyAnswers());
        return result;
    }

    private DemographicsCountsResponse buildCountsFromRollups(List<DemographicsRollup> rollups, String granularity) {
        rollups.sort(Comparator.comparing(DemographicsRollup::getDate));

        List<DemographicsCountsResponse.DailyCount> periods = new ArrayList<>();
        int totalResponses = 0;
        Map<String, Integer> totalCountries = new HashMap<>();
        Map<String, Integer> totalYearOfBirth = new HashMap<>();
        Map<String, Integer> totalGender = new HashMap<>();
        Map<String, Integer> totalMaritalStatus = new HashMap<>();
        Map<String, Integer> totalHouseholdSize = new HashMap<>();
        Map<String, Integer> totalHouseholdIncome = new HashMap<>();
        Map<String, Integer> totalEducationalLevel = new HashMap<>();
        Map<String, Integer> totalTimeSpentOnMturk = new HashMap<>();
        Map<String, Integer> totalWeeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> totalLanguagesSpoken = new HashMap<>();
        Map<String, Integer> totalCountriesDetailed = new HashMap<>();
        Map<String, Integer> totalUsStates = new HashMap<>();
        long rtWeightedMedian = 0, rtWeightedP25 = 0, rtWeightedP75 = 0;
        int rtTotalCount = 0;

        for (DemographicsRollup r : rollups) {
            // Skip rollups where all demographic maps are empty
            if (!hasAnyDemographicData(r)) continue;
            DemographicsCountsResponse.DailyCount period = new DemographicsCountsResponse.DailyCount();
            period.setDate(r.getDate()); // already yyyy-MM-dd
            period.setTotalResponses(r.getTotalResponses());
            period.setCountries(r.getCountries());
            period.setYearOfBirth(r.getYearOfBirth());
            period.setGender(r.getGender());
            period.setMaritalStatus(r.getMaritalStatus());
            period.setHouseholdSize(r.getHouseholdSize());
            period.setHouseholdIncome(r.getHouseholdIncome());
            period.setEducationalLevel(r.getEducationalLevel());
            period.setTimeSpentOnMturk(r.getTimeSpentOnMturk());
            period.setWeeklyIncomeFromMturk(r.getWeeklyIncomeFromMturk());
            period.setLanguagesSpoken(r.getLanguagesSpoken());
            period.setCountriesDetailed(r.getCountriesDetailed());
            period.setUsStates(r.getUsStates());
            period.setMedianResponseTimeMinutes(r.getMedianResponseTimeMinutes());
            period.setP25ResponseTimeMinutes(r.getP25ResponseTimeMinutes());
            period.setP75ResponseTimeMinutes(r.getP75ResponseTimeMinutes());
            periods.add(period);

            totalResponses += r.getTotalResponses();
            mergeCounts(r.getCountries(), totalCountries);
            mergeCounts(r.getYearOfBirth(), totalYearOfBirth);
            mergeCounts(r.getGender(), totalGender);
            mergeCounts(r.getMaritalStatus(), totalMaritalStatus);
            mergeCounts(r.getHouseholdSize(), totalHouseholdSize);
            mergeCounts(r.getHouseholdIncome(), totalHouseholdIncome);
            mergeCounts(r.getEducationalLevel(), totalEducationalLevel);
            mergeCounts(r.getTimeSpentOnMturk(), totalTimeSpentOnMturk);
            mergeCounts(r.getWeeklyIncomeFromMturk(), totalWeeklyIncomeFromMturk);
            mergeCounts(r.getLanguagesSpoken(), totalLanguagesSpoken);
            mergeCounts(r.getCountriesDetailed(), totalCountriesDetailed);
            mergeCounts(r.getUsStates(), totalUsStates);

            Integer rtCount = r.getResponseTimeCount();
            if (rtCount != null && rtCount > 0) {
                rtTotalCount += rtCount;
                if (r.getMedianResponseTimeMinutes() != null) rtWeightedMedian += r.getMedianResponseTimeMinutes() * rtCount;
                if (r.getP25ResponseTimeMinutes() != null) rtWeightedP25 += r.getP25ResponseTimeMinutes() * rtCount;
                if (r.getP75ResponseTimeMinutes() != null) rtWeightedP75 += r.getP75ResponseTimeMinutes() * rtCount;
            }
        }

        DemographicsCountsResponse response = new DemographicsCountsResponse();
        response.setGranularity(granularity);
        response.setDays(periods);
        response.setTotalResponses(totalResponses);
        response.setTotalCountries(totalCountries);
        response.setTotalYearOfBirth(totalYearOfBirth);
        response.setTotalGender(totalGender);
        response.setTotalMaritalStatus(totalMaritalStatus);
        response.setTotalHouseholdSize(totalHouseholdSize);
        response.setTotalHouseholdIncome(totalHouseholdIncome);
        response.setTotalEducationalLevel(totalEducationalLevel);
        response.setTotalTimeSpentOnMturk(totalTimeSpentOnMturk);
        response.setTotalWeeklyIncomeFromMturk(totalWeeklyIncomeFromMturk);
        response.setTotalLanguagesSpoken(totalLanguagesSpoken);
        response.setTotalCountriesDetailed(totalCountriesDetailed);
        response.setTotalUsStates(totalUsStates);
        if (rtTotalCount > 0) {
            response.setTotalMedianResponseTimeMinutes(rtWeightedMedian / rtTotalCount);
            response.setTotalP25ResponseTimeMinutes(rtWeightedP25 / rtTotalCount);
            response.setTotalP75ResponseTimeMinutes(rtWeightedP75 / rtTotalCount);
        }
        return response;
    }

    /**
     * Convert rollup date (yyyy-MM-dd) to a Date.toString() key for frontend compatibility.
     */
    private String rollupDateToKey(String dateStr, String granularity) {
        try {
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sortableDf.parse(dateStr));
            CalendarUtils.truncateToDay(cal);
            return cal.getTime().toString();
        } catch (ParseException e) {
            return dateStr;
        }
    }

    // --- Daily aggregation (one entry per day) ---

    private DemographicsSurveyAnswers buildDailyAggregation(List<DemographicsSnapshot> snapshots) {
        Map<String, Map<String, Float>> countries = new LinkedHashMap<>();
        Map<String, Map<String, Float>> yearOfBirth = new LinkedHashMap<>();
        Map<String, Map<String, Float>> gender = new LinkedHashMap<>();
        Map<String, Map<String, Float>> maritalStatus = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdSize = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdIncome = new LinkedHashMap<>();
        Map<String, Map<String, Float>> educationalLevel = new LinkedHashMap<>();
        Map<String, Map<String, Float>> timeSpentOnMturk = new LinkedHashMap<>();
        Map<String, Map<String, Float>> weeklyIncomeFromMturk = new LinkedHashMap<>();
        Map<String, Map<String, Float>> languagesSpoken = new LinkedHashMap<>();
        Map<String, Set<String>> labels = new HashMap<>();

        for (DemographicsSnapshot snap : snapshots) {
            if (snap.getTotalResponses() == 0) continue;
            if (!hasAnyDemographicData(snap)) continue;

            String key;
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(SafeDateFormat.forPattern("MM/dd/yyyy").parse(snap.getId()));
                CalendarUtils.truncateToDay(cal);
                key = cal.getTime().toString();
            } catch (ParseException e) {
                continue;
            }

            countries.put(key, toPercentageMap(snap.getCountries(), labels, "countries"));
            yearOfBirth.put(key, toPercentageMap(snap.getYearOfBirth(), labels, "yearOfBirth"));
            gender.put(key, toPercentageMap(snap.getGender(), labels, "gender"));
            maritalStatus.put(key, toPercentageMap(snap.getMaritalStatus(), labels, "maritalStatus"));
            householdSize.put(key, toPercentageMap(snap.getHouseholdSize(), labels, "householdSize"));
            householdIncome.put(key, toPercentageMap(snap.getHouseholdIncome(), labels, "householdIncome"));
            educationalLevel.put(key, toPercentageMap(snap.getEducationalLevel(), labels, "educationalLevel"));
            timeSpentOnMturk.put(key, toPercentageMap(snap.getTimeSpentOnMturk(), labels, "timeSpentOnMturk"));
            weeklyIncomeFromMturk.put(key, toPercentageMap(snap.getWeeklyIncomeFromMturk(), labels, "weeklyIncomeFromMturk"));
            languagesSpoken.put(key, toPercentageMap(snap.getLanguagesSpoken(), labels, "languagesSpoken"));
        }

        filterIncomeLabels(labels);

        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countries);
        result.setYearOfBirth(yearOfBirth);
        result.setGender(gender);
        result.setMaritalStatus(maritalStatus);
        result.setHouseholdSize(householdSize);
        result.setHouseholdIncome(householdIncome);
        result.setEducationalLevel(educationalLevel);
        result.setTimeSpentOnMturk(timeSpentOnMturk);
        result.setWeeklyIncomeFromMturk(weeklyIncomeFromMturk);
        result.setLanguagesSpoken(languagesSpoken);
        result.setLabels(labels);
        return result;
    }

    // --- Grouped percentages (calendar-weekly or monthly) ---

    private DemographicsSurveyAnswers buildGroupedPercentages(List<DemographicsSnapshot> snapshots, String granularity) {
        Map<String, List<DemographicsSnapshot>> groups = groupSnapshots(snapshots, granularity);

        Map<String, Map<String, Integer>> countryCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> yearOfBirthCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> genderCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> maritalStatusCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdSizeCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdIncomeCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> educationalLevelCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> timeSpentOnMturkCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> weeklyIncomeFromMturkCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> languagesSpokenCounts = new LinkedHashMap<>();

        for (Map.Entry<String, List<DemographicsSnapshot>> group : groups.entrySet()) {
            String key = group.getKey();
            countryCounts.put(key, new HashMap<>());
            yearOfBirthCounts.put(key, new HashMap<>());
            genderCounts.put(key, new HashMap<>());
            maritalStatusCounts.put(key, new HashMap<>());
            householdSizeCounts.put(key, new HashMap<>());
            householdIncomeCounts.put(key, new HashMap<>());
            educationalLevelCounts.put(key, new HashMap<>());
            timeSpentOnMturkCounts.put(key, new HashMap<>());
            weeklyIncomeFromMturkCounts.put(key, new HashMap<>());
            languagesSpokenCounts.put(key, new HashMap<>());

            for (DemographicsSnapshot snap : group.getValue()) {
                mergeCounts(snap.getCountries(), countryCounts.get(key));
                mergeCounts(snap.getYearOfBirth(), yearOfBirthCounts.get(key));
                mergeCounts(snap.getGender(), genderCounts.get(key));
                mergeCounts(snap.getMaritalStatus(), maritalStatusCounts.get(key));
                mergeCounts(snap.getHouseholdSize(), householdSizeCounts.get(key));
                mergeCounts(snap.getHouseholdIncome(), householdIncomeCounts.get(key));
                mergeCounts(snap.getEducationalLevel(), educationalLevelCounts.get(key));
                mergeCounts(snap.getTimeSpentOnMturk(), timeSpentOnMturkCounts.get(key));
                mergeCounts(snap.getWeeklyIncomeFromMturk(), weeklyIncomeFromMturkCounts.get(key));
                mergeCounts(snap.getLanguagesSpoken(), languagesSpokenCounts.get(key));
            }
        }

        Map<String, Set<String>> labels = new HashMap<>();
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countsToPercentages(countryCounts, labels, "countries"));
        result.setYearOfBirth(countsToPercentages(yearOfBirthCounts, labels, "yearOfBirth"));
        result.setGender(countsToPercentages(genderCounts, labels, "gender"));
        result.setMaritalStatus(countsToPercentages(maritalStatusCounts, labels, "maritalStatus"));
        result.setHouseholdSize(countsToPercentages(householdSizeCounts, labels, "householdSize"));
        result.setHouseholdIncome(countsToPercentages(householdIncomeCounts, labels, "householdIncome"));
        result.setEducationalLevel(countsToPercentages(educationalLevelCounts, labels, "educationalLevel"));
        result.setTimeSpentOnMturk(countsToPercentages(timeSpentOnMturkCounts, labels, "timeSpentOnMturk"));
        result.setWeeklyIncomeFromMturk(countsToPercentages(weeklyIncomeFromMturkCounts, labels, "weeklyIncomeFromMturk"));
        result.setLanguagesSpoken(countsToPercentages(languagesSpokenCounts, labels, "languagesSpoken"));
        filterIncomeLabels(labels);
        result.setLabels(labels);
        return result;
    }

    /**
     * Group snapshots by period. Uses Date.toString() keys (Monday for weekly, 1st for monthly)
     * to remain compatible with the frontend's new Date() parsing.
     */
    private Map<String, List<DemographicsSnapshot>> groupSnapshots(List<DemographicsSnapshot> snapshots, String granularity) {
        Map<String, List<DemographicsSnapshot>> groups = new LinkedHashMap<>();
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

        for (DemographicsSnapshot snap : snapshots) {
            if (snap.getTotalResponses() == 0) continue;
            if (!hasAnyDemographicData(snap)) continue;
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(df.parse(snap.getId()));
                String key = "monthly".equals(granularity) ? getFirstOfMonthKey(cal) : getMondayKey(cal);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(snap);
            } catch (ParseException e) {
                // skip
            }
        }
        return groups;
    }

    private String getMondayKey(Calendar cal) {
        Calendar monday = (Calendar) cal.clone();
        int dow = monday.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;
        monday.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        CalendarUtils.truncateToDay(monday);
        return monday.getTime().toString();
    }

    private String getFirstOfMonthKey(Calendar cal) {
        Calendar first = (Calendar) cal.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        CalendarUtils.truncateToDay(first);
        return first.getTime().toString();
    }

    // --- Hourly aggregation (unchanged) ---

    private DemographicsSurveyAnswers buildHourlyAggregation(List<DemographicsSnapshot> snapshots) {
        Map<String, Map<String, Integer>> countryCounts = new HashMap<>();
        Map<String, Map<String, Integer>> yearOfBirthCounts = new HashMap<>();
        Map<String, Map<String, Integer>> genderCounts = new HashMap<>();
        Map<String, Map<String, Integer>> maritalStatusCounts = new HashMap<>();
        Map<String, Map<String, Integer>> householdSizeCounts = new HashMap<>();
        Map<String, Map<String, Integer>> householdIncomeCounts = new HashMap<>();
        Map<String, Map<String, Integer>> educationalLevelCounts = new HashMap<>();
        Map<String, Map<String, Integer>> timeSpentOnMturkCounts = new HashMap<>();
        Map<String, Map<String, Integer>> weeklyIncomeFromMturkCounts = new HashMap<>();
        Map<String, Map<String, Integer>> languagesSpokenCounts = new HashMap<>();

        for (DemographicsSnapshot snap : snapshots) {
            mergeHourlyCounts(snap.getHourlyCountries(), countryCounts);
            mergeHourlyCounts(snap.getHourlyYearOfBirth(), yearOfBirthCounts);
            mergeHourlyCounts(snap.getHourlyGender(), genderCounts);
            mergeHourlyCounts(snap.getHourlyMaritalStatus(), maritalStatusCounts);
            mergeHourlyCounts(snap.getHourlyHouseholdSize(), householdSizeCounts);
            mergeHourlyCounts(snap.getHourlyHouseholdIncome(), householdIncomeCounts);
            mergeHourlyCounts(snap.getHourlyEducationalLevel(), educationalLevelCounts);
            mergeHourlyCounts(snap.getHourlyTimeSpentOnMturk(), timeSpentOnMturkCounts);
            mergeHourlyCounts(snap.getHourlyWeeklyIncomeFromMturk(), weeklyIncomeFromMturkCounts);
            mergeHourlyCounts(snap.getHourlyLanguagesSpoken(), languagesSpokenCounts);
        }

        Map<String, Set<String>> labels = new HashMap<>();
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countsToPercentages(countryCounts, labels, "countries"));
        result.setYearOfBirth(countsToPercentages(yearOfBirthCounts, labels, "yearOfBirth"));
        result.setGender(countsToPercentages(genderCounts, labels, "gender"));
        result.setMaritalStatus(countsToPercentages(maritalStatusCounts, labels, "maritalStatus"));
        result.setHouseholdSize(countsToPercentages(householdSizeCounts, labels, "householdSize"));
        result.setHouseholdIncome(countsToPercentages(householdIncomeCounts, labels, "householdIncome"));
        result.setEducationalLevel(countsToPercentages(educationalLevelCounts, labels, "educationalLevel"));
        result.setTimeSpentOnMturk(countsToPercentages(timeSpentOnMturkCounts, labels, "timeSpentOnMturk"));
        result.setWeeklyIncomeFromMturk(countsToPercentages(weeklyIncomeFromMturkCounts, labels, "weeklyIncomeFromMturk"));
        result.setLanguagesSpoken(countsToPercentages(languagesSpokenCounts, labels, "languagesSpoken"));
        filterIncomeLabels(labels);
        result.setLabels(labels);
        return result;
    }

    // --- Day-of-week aggregation (unchanged) ---

    private DemographicsSurveyAnswers buildWeeklyAggregation(List<DemographicsSnapshot> snapshots) {
        Map<String, Map<String, Integer>> countryCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> yearOfBirthCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> genderCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> maritalStatusCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdSizeCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdIncomeCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> educationalLevelCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> timeSpentOnMturkCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> weeklyIncomeFromMturkCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> languagesSpokenCounts = new LinkedHashMap<>();

        for (String day : DAYS) {
            countryCounts.put(day, new HashMap<>());
            yearOfBirthCounts.put(day, new HashMap<>());
            genderCounts.put(day, new HashMap<>());
            maritalStatusCounts.put(day, new HashMap<>());
            householdSizeCounts.put(day, new HashMap<>());
            householdIncomeCounts.put(day, new HashMap<>());
            educationalLevelCounts.put(day, new HashMap<>());
            timeSpentOnMturkCounts.put(day, new HashMap<>());
            weeklyIncomeFromMturkCounts.put(day, new HashMap<>());
            languagesSpokenCounts.put(day, new HashMap<>());
        }

        for (DemographicsSnapshot snap : snapshots) {
            String dow = snap.getDayOfWeek();
            if (dow == null) continue;
            mergeCounts(snap.getCountries(), countryCounts.get(dow));
            mergeCounts(snap.getYearOfBirth(), yearOfBirthCounts.get(dow));
            mergeCounts(snap.getGender(), genderCounts.get(dow));
            mergeCounts(snap.getMaritalStatus(), maritalStatusCounts.get(dow));
            mergeCounts(snap.getHouseholdSize(), householdSizeCounts.get(dow));
            mergeCounts(snap.getHouseholdIncome(), householdIncomeCounts.get(dow));
            mergeCounts(snap.getEducationalLevel(), educationalLevelCounts.get(dow));
            mergeCounts(snap.getTimeSpentOnMturk(), timeSpentOnMturkCounts.get(dow));
            mergeCounts(snap.getWeeklyIncomeFromMturk(), weeklyIncomeFromMturkCounts.get(dow));
            mergeCounts(snap.getLanguagesSpoken(), languagesSpokenCounts.get(dow));
        }

        Map<String, Set<String>> labels = new HashMap<>();
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countsToPercentages(countryCounts, labels, "countries"));
        result.setYearOfBirth(countsToPercentages(yearOfBirthCounts, labels, "yearOfBirth"));
        result.setGender(countsToPercentages(genderCounts, labels, "gender"));
        result.setMaritalStatus(countsToPercentages(maritalStatusCounts, labels, "maritalStatus"));
        result.setHouseholdSize(countsToPercentages(householdSizeCounts, labels, "householdSize"));
        result.setHouseholdIncome(countsToPercentages(householdIncomeCounts, labels, "householdIncome"));
        result.setEducationalLevel(countsToPercentages(educationalLevelCounts, labels, "educationalLevel"));
        result.setTimeSpentOnMturk(countsToPercentages(timeSpentOnMturkCounts, labels, "timeSpentOnMturk"));
        result.setWeeklyIncomeFromMturk(countsToPercentages(weeklyIncomeFromMturkCounts, labels, "weeklyIncomeFromMturk"));
        result.setLanguagesSpoken(countsToPercentages(languagesSpokenCounts, labels, "languagesSpoken"));
        filterIncomeLabels(labels);
        result.setLabels(labels);
        return result;
    }

    // --- Counts builders ---

    private DemographicsCountsResponse buildDailyCounts(List<DemographicsSnapshot> snapshots, String granularity) {
        List<DemographicsCountsResponse.DailyCount> days = new ArrayList<>();
        int totalResponses = 0;
        Map<String, Integer> totalCountries = new HashMap<>();
        Map<String, Integer> totalYearOfBirth = new HashMap<>();
        Map<String, Integer> totalGender = new HashMap<>();
        Map<String, Integer> totalMaritalStatus = new HashMap<>();
        Map<String, Integer> totalHouseholdSize = new HashMap<>();
        Map<String, Integer> totalHouseholdIncome = new HashMap<>();
        Map<String, Integer> totalEducationalLevel = new HashMap<>();
        Map<String, Integer> totalTimeSpentOnMturk = new HashMap<>();
        Map<String, Integer> totalWeeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> totalLanguagesSpoken = new HashMap<>();
        Map<String, Integer> totalCountriesDetailed = new HashMap<>();
        Map<String, Integer> totalUsStates = new HashMap<>();
        long rtWeightedMedian = 0, rtWeightedP25 = 0, rtWeightedP75 = 0;
        int rtTotalCount = 0;

        for (DemographicsSnapshot snap : snapshots) {
            if (!hasAnyDemographicData(snap)) continue;
            DemographicsCountsResponse.DailyCount day = new DemographicsCountsResponse.DailyCount();
            day.setDate(snap.getDate());
            day.setTotalResponses(snap.getTotalResponses());
            day.setCountries(snap.getCountries());
            day.setYearOfBirth(snap.getYearOfBirth());
            day.setGender(snap.getGender());
            day.setMaritalStatus(snap.getMaritalStatus());
            day.setHouseholdSize(snap.getHouseholdSize());
            day.setHouseholdIncome(snap.getHouseholdIncome());
            day.setEducationalLevel(snap.getEducationalLevel());
            day.setTimeSpentOnMturk(snap.getTimeSpentOnMturk());
            day.setWeeklyIncomeFromMturk(snap.getWeeklyIncomeFromMturk());
            day.setLanguagesSpoken(snap.getLanguagesSpoken());
            day.setCountriesDetailed(snap.getCountriesDetailed());
            day.setUsStates(snap.getUsStates());
            day.setMedianResponseTimeMinutes(snap.getMedianResponseTimeMinutes());
            day.setP25ResponseTimeMinutes(snap.getP25ResponseTimeMinutes());
            day.setP75ResponseTimeMinutes(snap.getP75ResponseTimeMinutes());
            days.add(day);

            totalResponses += snap.getTotalResponses();
            mergeCounts(snap.getCountries(), totalCountries);
            mergeCounts(snap.getYearOfBirth(), totalYearOfBirth);
            mergeCounts(snap.getGender(), totalGender);
            mergeCounts(snap.getMaritalStatus(), totalMaritalStatus);
            mergeCounts(snap.getHouseholdSize(), totalHouseholdSize);
            mergeCounts(snap.getHouseholdIncome(), totalHouseholdIncome);
            mergeCounts(snap.getEducationalLevel(), totalEducationalLevel);
            mergeCounts(snap.getTimeSpentOnMturk(), totalTimeSpentOnMturk);
            mergeCounts(snap.getWeeklyIncomeFromMturk(), totalWeeklyIncomeFromMturk);
            mergeCounts(snap.getLanguagesSpoken(), totalLanguagesSpoken);
            mergeCounts(snap.getCountriesDetailed(), totalCountriesDetailed);
            mergeCounts(snap.getUsStates(), totalUsStates);

            Integer rtCount = snap.getResponseTimeCount();
            if (rtCount != null && rtCount > 0) {
                rtTotalCount += rtCount;
                if (snap.getMedianResponseTimeMinutes() != null) rtWeightedMedian += snap.getMedianResponseTimeMinutes() * rtCount;
                if (snap.getP25ResponseTimeMinutes() != null) rtWeightedP25 += snap.getP25ResponseTimeMinutes() * rtCount;
                if (snap.getP75ResponseTimeMinutes() != null) rtWeightedP75 += snap.getP75ResponseTimeMinutes() * rtCount;
            }
        }

        DemographicsCountsResponse response = new DemographicsCountsResponse();
        response.setGranularity(granularity);
        response.setDays(days);
        response.setTotalResponses(totalResponses);
        response.setTotalCountries(totalCountries);
        response.setTotalYearOfBirth(totalYearOfBirth);
        response.setTotalGender(totalGender);
        response.setTotalMaritalStatus(totalMaritalStatus);
        response.setTotalHouseholdSize(totalHouseholdSize);
        response.setTotalHouseholdIncome(totalHouseholdIncome);
        response.setTotalEducationalLevel(totalEducationalLevel);
        response.setTotalTimeSpentOnMturk(totalTimeSpentOnMturk);
        response.setTotalWeeklyIncomeFromMturk(totalWeeklyIncomeFromMturk);
        response.setTotalLanguagesSpoken(totalLanguagesSpoken);
        response.setTotalCountriesDetailed(totalCountriesDetailed);
        response.setTotalUsStates(totalUsStates);
        if (rtTotalCount > 0) {
            response.setTotalMedianResponseTimeMinutes(rtWeightedMedian / rtTotalCount);
            response.setTotalP25ResponseTimeMinutes(rtWeightedP25 / rtTotalCount);
            response.setTotalP75ResponseTimeMinutes(rtWeightedP75 / rtTotalCount);
        }
        return response;
    }

    private DemographicsCountsResponse buildGroupedCounts(List<DemographicsSnapshot> snapshots, String granularity) {
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");

        // Group snapshots, using sortable date keys for the counts response
        Map<String, List<DemographicsSnapshot>> groups = new LinkedHashMap<>();
        for (DemographicsSnapshot snap : snapshots) {
            if (!hasAnyDemographicData(snap)) continue;
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(df.parse(snap.getId()));
                String periodKey = "monthly".equals(granularity) ?
                        getFirstOfMonthSortableDate(cal, sortableDf) :
                        getMondaySortableDate(cal, sortableDf);
                groups.computeIfAbsent(periodKey, k -> new ArrayList<>()).add(snap);
            } catch (ParseException e) {
                // skip
            }
        }

        List<DemographicsCountsResponse.DailyCount> periods = new ArrayList<>();
        int totalResponses = 0;
        Map<String, Integer> totalCountries = new HashMap<>();
        Map<String, Integer> totalYearOfBirth = new HashMap<>();
        Map<String, Integer> totalGender = new HashMap<>();
        Map<String, Integer> totalMaritalStatus = new HashMap<>();
        Map<String, Integer> totalHouseholdSize = new HashMap<>();
        Map<String, Integer> totalHouseholdIncome = new HashMap<>();
        Map<String, Integer> totalEducationalLevel = new HashMap<>();
        Map<String, Integer> totalTimeSpentOnMturk = new HashMap<>();
        Map<String, Integer> totalWeeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> totalLanguagesSpoken = new HashMap<>();
        Map<String, Integer> totalCountriesDetailed = new HashMap<>();
        Map<String, Integer> totalUsStates = new HashMap<>();
        long rtWeightedMedian = 0, rtWeightedP25 = 0, rtWeightedP75 = 0;
        int rtTotalCount = 0;

        for (Map.Entry<String, List<DemographicsSnapshot>> group : groups.entrySet()) {
            DemographicsCountsResponse.DailyCount period = new DemographicsCountsResponse.DailyCount();
            period.setDate(group.getKey());

            Map<String, Integer> pCountries = new HashMap<>();
            Map<String, Integer> pYearOfBirth = new HashMap<>();
            Map<String, Integer> pGender = new HashMap<>();
            Map<String, Integer> pMaritalStatus = new HashMap<>();
            Map<String, Integer> pHouseholdSize = new HashMap<>();
            Map<String, Integer> pHouseholdIncome = new HashMap<>();
            Map<String, Integer> pEducationalLevel = new HashMap<>();
            Map<String, Integer> pTimeSpentOnMturk = new HashMap<>();
            Map<String, Integer> pWeeklyIncomeFromMturk = new HashMap<>();
            Map<String, Integer> pLanguagesSpoken = new HashMap<>();
            Map<String, Integer> pCountriesDetailed = new HashMap<>();
            Map<String, Integer> pUsStates = new HashMap<>();
            int periodResponses = 0;
            long pRtWeightedMedian = 0, pRtWeightedP25 = 0, pRtWeightedP75 = 0;
            int pRtCount = 0;

            for (DemographicsSnapshot snap : group.getValue()) {
                periodResponses += snap.getTotalResponses();
                mergeCounts(snap.getCountries(), pCountries);
                mergeCounts(snap.getYearOfBirth(), pYearOfBirth);
                mergeCounts(snap.getGender(), pGender);
                mergeCounts(snap.getMaritalStatus(), pMaritalStatus);
                mergeCounts(snap.getHouseholdSize(), pHouseholdSize);
                mergeCounts(snap.getHouseholdIncome(), pHouseholdIncome);
                mergeCounts(snap.getEducationalLevel(), pEducationalLevel);
                mergeCounts(snap.getTimeSpentOnMturk(), pTimeSpentOnMturk);
                mergeCounts(snap.getWeeklyIncomeFromMturk(), pWeeklyIncomeFromMturk);
                mergeCounts(snap.getLanguagesSpoken(), pLanguagesSpoken);
                mergeCounts(snap.getCountriesDetailed(), pCountriesDetailed);
                mergeCounts(snap.getUsStates(), pUsStates);

                Integer rtCount = snap.getResponseTimeCount();
                if (rtCount != null && rtCount > 0) {
                    pRtCount += rtCount;
                    if (snap.getMedianResponseTimeMinutes() != null) pRtWeightedMedian += snap.getMedianResponseTimeMinutes() * rtCount;
                    if (snap.getP25ResponseTimeMinutes() != null) pRtWeightedP25 += snap.getP25ResponseTimeMinutes() * rtCount;
                    if (snap.getP75ResponseTimeMinutes() != null) pRtWeightedP75 += snap.getP75ResponseTimeMinutes() * rtCount;
                }
            }

            period.setTotalResponses(periodResponses);
            period.setCountries(pCountries);
            period.setYearOfBirth(pYearOfBirth);
            period.setGender(pGender);
            period.setMaritalStatus(pMaritalStatus);
            period.setHouseholdSize(pHouseholdSize);
            period.setHouseholdIncome(pHouseholdIncome);
            period.setEducationalLevel(pEducationalLevel);
            period.setTimeSpentOnMturk(pTimeSpentOnMturk);
            period.setWeeklyIncomeFromMturk(pWeeklyIncomeFromMturk);
            period.setLanguagesSpoken(pLanguagesSpoken);
            period.setCountriesDetailed(pCountriesDetailed);
            period.setUsStates(pUsStates);
            if (pRtCount > 0) {
                period.setMedianResponseTimeMinutes(pRtWeightedMedian / pRtCount);
                period.setP25ResponseTimeMinutes(pRtWeightedP25 / pRtCount);
                period.setP75ResponseTimeMinutes(pRtWeightedP75 / pRtCount);
            }
            periods.add(period);

            totalResponses += periodResponses;
            mergeCounts(pCountries, totalCountries);
            mergeCounts(pYearOfBirth, totalYearOfBirth);
            mergeCounts(pGender, totalGender);
            mergeCounts(pMaritalStatus, totalMaritalStatus);
            mergeCounts(pHouseholdSize, totalHouseholdSize);
            mergeCounts(pHouseholdIncome, totalHouseholdIncome);
            mergeCounts(pEducationalLevel, totalEducationalLevel);
            mergeCounts(pTimeSpentOnMturk, totalTimeSpentOnMturk);
            mergeCounts(pWeeklyIncomeFromMturk, totalWeeklyIncomeFromMturk);
            mergeCounts(pLanguagesSpoken, totalLanguagesSpoken);
            mergeCounts(pCountriesDetailed, totalCountriesDetailed);
            mergeCounts(pUsStates, totalUsStates);
            if (pRtCount > 0) {
                rtTotalCount += pRtCount;
                rtWeightedMedian += pRtWeightedMedian;
                rtWeightedP25 += pRtWeightedP25;
                rtWeightedP75 += pRtWeightedP75;
            }
        }

        DemographicsCountsResponse response = new DemographicsCountsResponse();
        response.setGranularity(granularity);
        response.setDays(periods);
        response.setTotalResponses(totalResponses);
        response.setTotalCountries(totalCountries);
        response.setTotalYearOfBirth(totalYearOfBirth);
        response.setTotalGender(totalGender);
        response.setTotalMaritalStatus(totalMaritalStatus);
        response.setTotalHouseholdSize(totalHouseholdSize);
        response.setTotalHouseholdIncome(totalHouseholdIncome);
        response.setTotalEducationalLevel(totalEducationalLevel);
        response.setTotalTimeSpentOnMturk(totalTimeSpentOnMturk);
        response.setTotalWeeklyIncomeFromMturk(totalWeeklyIncomeFromMturk);
        response.setTotalLanguagesSpoken(totalLanguagesSpoken);
        response.setTotalCountriesDetailed(totalCountriesDetailed);
        response.setTotalUsStates(totalUsStates);
        if (rtTotalCount > 0) {
            response.setTotalMedianResponseTimeMinutes(rtWeightedMedian / rtTotalCount);
            response.setTotalP25ResponseTimeMinutes(rtWeightedP25 / rtTotalCount);
            response.setTotalP75ResponseTimeMinutes(rtWeightedP75 / rtTotalCount);
        }
        return response;
    }

    private String getMondaySortableDate(Calendar cal, DateFormat sortableDf) {
        Calendar monday = (Calendar) cal.clone();
        int dow = monday.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;
        monday.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        CalendarUtils.truncateToDay(monday);
        return sortableDf.format(monday.getTime());
    }

    private String getFirstOfMonthSortableDate(Calendar cal, DateFormat sortableDf) {
        Calendar first = (Calendar) cal.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        CalendarUtils.truncateToDay(first);
        return sortableDf.format(first.getTime());
    }

    // --- Helper methods ---

    private void mergeHourlyCounts(Map<String, Integer> hourlyCounts, Map<String, Map<String, Integer>> target) {
        if (hourlyCounts == null) return;
        for (Map.Entry<String, Integer> entry : hourlyCounts.entrySet()) {
            // Key format: "hour:value"
            String[] parts = entry.getKey().split(":", 2);
            if (parts.length != 2) continue;
            String hour = parts[0];
            String value = parts[1];
            target.computeIfAbsent(hour, k -> new HashMap<>())
                    .merge(value, entry.getValue(), Integer::sum);
        }
    }

    private void mergeCounts(Map<String, Integer> source, Map<String, Integer> target) {
        if (source == null) return;
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    private Map<String, Map<String, Float>> countsToPercentages(
            Map<String, Map<String, Integer>> counts, Map<String, Set<String>> labels, String labelKey) {
        Map<String, Map<String, Float>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : counts.entrySet()) {
            result.put(entry.getKey(), toPercentageMap(entry.getValue(), labels, labelKey));
        }
        return result;
    }

    private Map<String, Float> toPercentageMap(Map<String, Integer> counts,
                                                Map<String, Set<String>> labels, String labelKey) {
        Map<String, Float> percentages = new HashMap<>();
        if (counts == null || counts.isEmpty()) return percentages;

        int total = 0;
        for (int v : counts.values()) {
            total += v;
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            float pct = total == 0 ? 0f : (entry.getValue() / (float) total) * 100f;
            percentages.put(entry.getKey(), pct);
            labels.computeIfAbsent(labelKey, k -> new TreeSet<>()).add(entry.getKey());
        }
        return percentages;
    }

    private void filterIncomeLabels(Map<String, Set<String>> labels) {
        if (labels.get("householdIncome") != null) {
            Set<String> existing = labels.get("householdIncome");
            Set<String> filtered = new LinkedHashSet<>();
            for (String label : INCOME_LABELS) {
                if (existing.contains(label)) {
                    filtered.add(label);
                }
            }
            labels.put("householdIncome", filtered);
        }
    }

    /**
     * Returns true if the rollup has at least one non-empty demographic map.
     * Rollups with only country data (from non-demographics entities) are considered empty.
     */
    private boolean hasAnyDemographicData(DemographicsRollup r) {
        return isNonEmpty(r.getGender()) || isNonEmpty(r.getYearOfBirth())
                || isNonEmpty(r.getMaritalStatus()) || isNonEmpty(r.getHouseholdSize())
                || isNonEmpty(r.getHouseholdIncome()) || isNonEmpty(r.getEducationalLevel())
                || isNonEmpty(r.getTimeSpentOnMturk()) || isNonEmpty(r.getWeeklyIncomeFromMturk())
                || isNonEmpty(r.getLanguagesSpoken());
    }

    /**
     * Returns true if the snapshot has at least one non-empty demographic map.
     */
    private boolean hasAnyDemographicData(DemographicsSnapshot s) {
        return isNonEmpty(s.getGender()) || isNonEmpty(s.getYearOfBirth())
                || isNonEmpty(s.getMaritalStatus()) || isNonEmpty(s.getHouseholdSize())
                || isNonEmpty(s.getHouseholdIncome()) || isNonEmpty(s.getEducationalLevel())
                || isNonEmpty(s.getTimeSpentOnMturk()) || isNonEmpty(s.getWeeklyIncomeFromMturk())
                || isNonEmpty(s.getLanguagesSpoken());
    }

    private boolean isNonEmpty(Map<String, Integer> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Build a rollup directly from BigQuery data for a date range, bypassing
     * the daily snapshot layer. Used when daily snapshots are missing.
     *
     * @param granularity "weekly" or "monthly"
     * @param dateKey     the rollup key date (yyyy-MM-dd)
     * @param fromDate    inclusive start date (yyyy-MM-dd)
     * @param toDate      exclusive end date (yyyy-MM-dd)
     */
    private DemographicsRollup buildRollupFromBigQuery(String granularity, String dateKey,
                                                        String fromDate, String toDate) {
        List<UserAnswer> answers = loadFromBigQueryRange(fromDate, toDate);
        if (answers.isEmpty()) {
            logger.info("No BigQuery data for " + granularity + " rollup " + dateKey);
            return null;
        }

        int totalResponses = 0;
        Map<String, Integer> countries = new HashMap<>();
        Map<String, Integer> yearOfBirth = new HashMap<>();
        Map<String, Integer> gender = new HashMap<>();
        Map<String, Integer> maritalStatus = new HashMap<>();
        Map<String, Integer> householdSize = new HashMap<>();
        Map<String, Integer> householdIncome = new HashMap<>();
        Map<String, Integer> educationalLevel = new HashMap<>();
        Map<String, Integer> timeSpentOnMturk = new HashMap<>();
        Map<String, Integer> weeklyIncomeFromMturk = new HashMap<>();
        Map<String, Integer> languagesSpoken = new HashMap<>();
        Map<String, Integer> countriesDetailed = new HashMap<>();
        Map<String, Integer> usStates = new HashMap<>();

        for (UserAnswer ua : answers) {
            if (!hasDemographicAnswers(ua)) continue;
            totalResponses++;
            incrementCountry(ua.getLocationCountry(), countries);
            incrementCountryDetailed(ua.getLocationCountry(), countriesDetailed);
            incrementUsState(ua.getLocationCountry(), ua.getLocationRegion(), usStates);
            incrementDemographic("yearOfBirth", ua.getAnswers(), yearOfBirth, true);
            incrementDemographic("gender", ua.getAnswers(), gender, false);
            incrementDemographic("maritalStatus", ua.getAnswers(), maritalStatus, false);
            incrementDemographic("householdSize", ua.getAnswers(), householdSize, false);
            incrementDemographic("householdIncome", ua.getAnswers(), householdIncome, false);
            incrementDemographic("educationalLevel", ua.getAnswers(), educationalLevel, false);
            incrementDemographic("timeSpentOnMturk", ua.getAnswers(), timeSpentOnMturk, false);
            incrementDemographic("weeklyIncomeFromMturk", ua.getAnswers(), weeklyIncomeFromMturk, false);
            incrementMultiValue("languagesSpoken", ua.getAnswers(), languagesSpoken);
        }

        if (totalResponses == 0) {
            logger.info("No valid demographics in BigQuery for " + granularity + " rollup " + dateKey
                    + " (" + answers.size() + " rows), skipping");
            return null;
        }

        DemographicsRollup rollup = new DemographicsRollup();
        rollup.setId(granularity + ":" + dateKey);
        rollup.setGranularity(granularity);
        rollup.setDate(dateKey);
        rollup.setTotalResponses(totalResponses);
        rollup.setCountries(countries);
        rollup.setYearOfBirth(yearOfBirth);
        rollup.setGender(gender);
        rollup.setMaritalStatus(maritalStatus);
        rollup.setHouseholdSize(householdSize);
        rollup.setHouseholdIncome(householdIncome);
        rollup.setEducationalLevel(educationalLevel);
        rollup.setTimeSpentOnMturk(timeSpentOnMturk);
        rollup.setWeeklyIncomeFromMturk(weeklyIncomeFromMturk);
        rollup.setLanguagesSpoken(languagesSpoken);
        rollup.setCountriesDetailed(countriesDetailed);
        rollup.setUsStates(usStates);

        logger.info("Built " + granularity + " rollup for " + dateKey + " from BigQuery: "
                + totalResponses + " valid of " + answers.size() + " rows");
        return rollup;
    }

    /**
     * Load UserAnswer-like objects from BigQuery for a date range.
     * Tries the Datastore backup table first, then falls back to the daily export table.
     *
     * @param fromDate inclusive start date (yyyy-MM-dd)
     * @param toDate   exclusive end date (yyyy-MM-dd)
     */
    private List<UserAnswer> loadFromBigQueryRange(String fromDate, String toDate) {
        List<UserAnswer> results = loadFromBackupTableRange(fromDate, toDate);
        if (!results.isEmpty()) {
            return results;
        }
        return loadFromResponsesTableRange(fromDate, toDate);
    }

    private List<UserAnswer> loadFromBackupTableRange(String fromDate, String toDate) {
        List<UserAnswer> results = new ArrayList<>();
        try {
            BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
            String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

            String sql = String.format(
                    "SELECT date, workerId, hitId, locationCountry, locationRegion, hitCreationDate, answers "
                    + "FROM `%s.%s.%s` WHERE DATE(date) >= '%s' AND DATE(date) < '%s'",
                    projectId, BQ_BACKUP_DATASET, BQ_BACKUP_TABLE, fromDate, toDate);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult tableResult = bigQuery.query(queryConfig);
            Schema tableSchema = tableResult.getSchema();

            for (FieldValueList row : tableResult.iterateAll()) {
                UserAnswer ua = new UserAnswer();
                ua.setDate(parseTimestamp(row, "date"));
                ua.setWorkerId(getStringOrNull(row, "workerId"));
                ua.setHitId(getStringOrNull(row, "hitId"));
                ua.setLocationCountry(getStringOrNull(row, "locationCountry"));
                ua.setLocationRegion(getStringOrNull(row, "locationRegion"));
                ua.setHitCreationDate(parseTimestampOrNull(row, "hitCreationDate"));

                Map<String, String> answers = parseAnswersRecord(row, tableSchema);
                if (!answers.isEmpty()) {
                    ua.setAnswers(answers);
                }
                results.add(ua);
            }

            logger.info("Loaded " + results.size() + " rows from backup " + BQ_BACKUP_DATASET
                    + "." + BQ_BACKUP_TABLE + " for range " + fromDate + " to " + toDate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted querying BigQuery backup range", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to query BigQuery backup range " + fromDate
                    + " to " + toDate + ": " + e.getMessage(), e);
        }
        return results;
    }

    private List<UserAnswer> loadFromResponsesTableRange(String fromDate, String toDate) {
        List<UserAnswer> results = new ArrayList<>();
        try {
            BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
            String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

            String sql = String.format(
                    "SELECT DISTINCT date, worker_id, hit_id, country, region, hit_creation_date, "
                    + "year_of_birth, gender, marital_status, "
                    + "household_size, household_income, educational_level, "
                    + "time_spent_on_mturk, weekly_income_from_mturk, languages_spoken "
                    + "FROM `%s.%s.%s` WHERE DATE(date) >= '%s' AND DATE(date) < '%s'",
                    projectId, BQ_DATASET, BQ_TABLE, fromDate, toDate);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult tableResult = bigQuery.query(queryConfig);

            for (FieldValueList row : tableResult.iterateAll()) {
                UserAnswer ua = new UserAnswer();
                ua.setDate(parseTimestamp(row, "date"));
                ua.setWorkerId(getStringOrNull(row, "worker_id"));
                ua.setHitId(getStringOrNull(row, "hit_id"));
                ua.setLocationCountry(getStringOrNull(row, "country"));
                ua.setLocationRegion(getStringOrNull(row, "region"));
                ua.setHitCreationDate(parseTimestampOrNull(row, "hit_creation_date"));

                Map<String, String> answers = new LinkedHashMap<>();
                putIfNotNull(answers, "yearOfBirth", getStringOrNull(row, "year_of_birth"));
                putIfNotNull(answers, "gender", getStringOrNull(row, "gender"));
                putIfNotNull(answers, "maritalStatus", getStringOrNull(row, "marital_status"));
                putIfNotNull(answers, "householdSize", getStringOrNull(row, "household_size"));
                putIfNotNull(answers, "householdIncome", getStringOrNull(row, "household_income"));
                putIfNotNull(answers, "educationalLevel", getStringOrNull(row, "educational_level"));
                putIfNotNull(answers, "timeSpentOnMturk", getStringOrNull(row, "time_spent_on_mturk"));
                putIfNotNull(answers, "weeklyIncomeFromMturk", getStringOrNull(row, "weekly_income_from_mturk"));
                putIfNotNull(answers, "languagesSpoken", getStringOrNull(row, "languages_spoken"));

                if (!answers.isEmpty()) {
                    ua.setAnswers(answers);
                }
                results.add(ua);
            }

            logger.info("Loaded " + results.size() + " rows from " + BQ_DATASET + "." + BQ_TABLE
                    + " for range " + fromDate + " to " + toDate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted querying BigQuery responses range", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to query BigQuery responses range " + fromDate
                    + " to " + toDate + ": " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Load UserAnswer-like objects from BigQuery for a given date.
     * Tries the Datastore backup table (authoritative source) first, then
     * falls back to the daily export table (demographics.responses).
     */
    private List<UserAnswer> loadFromBigQuery(String sortableDate) {
        // Try the Datastore backup table first (most complete source)
        List<UserAnswer> results = loadFromBackupTable(sortableDate);
        if (!results.isEmpty()) {
            return results;
        }

        // Fall back to the daily export table
        return loadFromResponsesTable(sortableDate);
    }

    /**
     * Load from the daily export table (demographics.responses) which has
     * flattened columns like year_of_birth, gender, etc.
     */
    private List<UserAnswer> loadFromResponsesTable(String sortableDate) {
        List<UserAnswer> results = new ArrayList<>();
        try {
            BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
            String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

            String sql = String.format(
                    "SELECT DISTINCT date, worker_id, hit_id, country, region, hit_creation_date, "
                    + "year_of_birth, gender, marital_status, "
                    + "household_size, household_income, educational_level, "
                    + "time_spent_on_mturk, weekly_income_from_mturk, languages_spoken "
                    + "FROM `%s.%s.%s` WHERE DATE(date) = '%s'",
                    projectId, BQ_DATASET, BQ_TABLE, sortableDate);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult tableResult = bigQuery.query(queryConfig);

            for (FieldValueList row : tableResult.iterateAll()) {
                UserAnswer ua = new UserAnswer();
                ua.setDate(parseTimestamp(row, "date"));
                ua.setWorkerId(getStringOrNull(row, "worker_id"));
                ua.setHitId(getStringOrNull(row, "hit_id"));
                ua.setLocationCountry(getStringOrNull(row, "country"));
                ua.setLocationRegion(getStringOrNull(row, "region"));
                ua.setHitCreationDate(parseTimestampOrNull(row, "hit_creation_date"));

                Map<String, String> answers = new LinkedHashMap<>();
                putIfNotNull(answers, "yearOfBirth", getStringOrNull(row, "year_of_birth"));
                putIfNotNull(answers, "gender", getStringOrNull(row, "gender"));
                putIfNotNull(answers, "maritalStatus", getStringOrNull(row, "marital_status"));
                putIfNotNull(answers, "householdSize", getStringOrNull(row, "household_size"));
                putIfNotNull(answers, "householdIncome", getStringOrNull(row, "household_income"));
                putIfNotNull(answers, "educationalLevel", getStringOrNull(row, "educational_level"));
                putIfNotNull(answers, "timeSpentOnMturk", getStringOrNull(row, "time_spent_on_mturk"));
                putIfNotNull(answers, "weeklyIncomeFromMturk", getStringOrNull(row, "weekly_income_from_mturk"));
                putIfNotNull(answers, "languagesSpoken", getStringOrNull(row, "languages_spoken"));

                if (!answers.isEmpty()) {
                    ua.setAnswers(answers);
                }
                results.add(ua);
            }

            logger.info("Loaded " + results.size() + " rows from " + BQ_DATASET + "." + BQ_TABLE + " for " + sortableDate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted querying BigQuery responses for " + sortableDate, e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to query BigQuery responses for " + sortableDate + ": " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Load from the Datastore backup table (test.UserAnswer_2025MAR20) which has
     * Datastore export format with nested answers RECORD and camelCase column names.
     */
    private List<UserAnswer> loadFromBackupTable(String sortableDate) {
        List<UserAnswer> results = new ArrayList<>();
        try {
            BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
            String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

            String sql = String.format(
                    "SELECT date, workerId, hitId, locationCountry, locationRegion, hitCreationDate, answers "
                    + "FROM `%s.%s.%s` WHERE DATE(date) = '%s'",
                    projectId, BQ_BACKUP_DATASET, BQ_BACKUP_TABLE, sortableDate);

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult tableResult = bigQuery.query(queryConfig);
            Schema tableSchema = tableResult.getSchema();

            for (FieldValueList row : tableResult.iterateAll()) {
                UserAnswer ua = new UserAnswer();
                ua.setDate(parseTimestamp(row, "date"));
                ua.setWorkerId(getStringOrNull(row, "workerId"));
                ua.setHitId(getStringOrNull(row, "hitId"));
                ua.setLocationCountry(getStringOrNull(row, "locationCountry"));
                ua.setLocationRegion(getStringOrNull(row, "locationRegion"));
                ua.setHitCreationDate(parseTimestampOrNull(row, "hitCreationDate"));

                Map<String, String> answers = parseAnswersRecord(row, tableSchema);
                if (!answers.isEmpty()) {
                    ua.setAnswers(answers);
                }
                results.add(ua);
            }

            logger.info("Loaded " + results.size() + " rows from backup " + BQ_BACKUP_DATASET + "." + BQ_BACKUP_TABLE + " for " + sortableDate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted querying BigQuery backup for " + sortableDate, e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to query BigQuery backup for " + sortableDate + ": " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Parse the 'answers' field from a Datastore export row.
     * The backup table stores answers as a flat RECORD with named sub-fields
     * (e.g., answers.gender, answers.householdSize) rather than a Datastore
     * map format with repeated {key, value} entries.
     */
    private Map<String, String> parseAnswersRecord(FieldValueList row, Schema tableSchema) {
        Map<String, String> answers = new LinkedHashMap<>();
        try {
            FieldValue answersField = row.get("answers");
            if (!answersField.isNull() && answersField.getAttribute() == FieldValue.Attribute.RECORD) {
                FieldValueList record = answersField.getRecordValue();

                // Get the sub-field names from the schema
                com.google.cloud.bigquery.Field answersSchemaField = tableSchema.getFields().get("answers");
                if (answersSchemaField != null && answersSchemaField.getSubFields() != null) {
                    FieldList subFields = answersSchemaField.getSubFields();
                    for (int i = 0; i < subFields.size() && i < record.size(); i++) {
                        String fieldName = subFields.get(i).getName();
                        if ("__key__".equals(fieldName)) continue;
                        try {
                            FieldValue val = record.get(i);
                            if (!val.isNull()) {
                                answers.put(fieldName, val.getStringValue());
                            }
                        } catch (Exception e) {
                            // Skip unparseable sub-field
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Field might not exist
        }
        return answers;
    }

    private Date parseTimestamp(FieldValueList row, String fieldName) {
        Date result = parseTimestampOrNull(row, fieldName);
        return result != null ? result : new Date();
    }

    private Date parseTimestampOrNull(FieldValueList row, String fieldName) {
        try {
            FieldValue val = row.get(fieldName);
            if (val.isNull()) return null;
            String dateVal = val.getStringValue();

            // Try epoch micros first (Datastore export format)
            try {
                long micros = Long.parseLong(dateVal);
                return new Date(micros / 1000);
            } catch (NumberFormatException e) {
                // Try floating-point epoch seconds
                try {
                    double epochSeconds = Double.parseDouble(dateVal);
                    return new Date((long) (epochSeconds * 1000));
                } catch (NumberFormatException e2) {
                    // Not numeric
                }
            }

            // Try ISO formats
            String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd HH:mm:ss z", "yyyy-MM-dd HH:mm:ss"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(dateVal);
                } catch (ParseException ignored) {
                }
            }
        } catch (Exception e) {
            // Field might not exist
        }
        return null;
    }

    private String getStringOrNull(FieldValueList row, String fieldName) {
        try {
            FieldValue val = row.get(fieldName);
            return val.isNull() ? null : val.getStringValue();
        } catch (Exception e) {
            return null;
        }
    }

    private void putIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static final Set<String> DEMOGRAPHIC_KEYS = Set.of(
            "gender", "yearOfBirth", "maritalStatus", "householdSize",
            "householdIncome", "educationalLevel", "timeSpentOnMturk",
            "weeklyIncomeFromMturk", "languagesSpoken");

    /**
     * Returns true if the UserAnswer has a populated answers map containing
     * at least one expected demographic key. This filters out non-demographics
     * entities and old-format entities that lack the answers map.
     */
    private boolean hasDemographicAnswers(UserAnswer ua) {
        Map<String, String> ans = ua.getAnswers();
        if (ans == null || ans.isEmpty()) {
            return false;
        }
        for (String key : DEMOGRAPHIC_KEYS) {
            if (ans.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private void increment(Map<String, Integer> map, String key) {
        map.merge(key, 1, Integer::sum);
    }

    private void collectResponseTime(UserAnswer ua, List<Long> responseTimesMinutes) {
        if (ua.getDate() != null && ua.getHitCreationDate() != null) {
            long diffMs = ua.getDate().getTime() - ua.getHitCreationDate().getTime();
            if (diffMs > 0 && diffMs < 7L * 24 * 60 * 60 * 1000) { // cap at 7 days to filter outliers
                responseTimesMinutes.add(diffMs / (60 * 1000));
            }
        }
    }

    private static long percentile(List<Long> sorted, int pct) {
        if (sorted.isEmpty()) return 0;
        double idx = (pct / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(idx);
        int upper = Math.min(lower + 1, sorted.size() - 1);
        double frac = idx - lower;
        return Math.round(sorted.get(lower) + frac * (sorted.get(upper) - sorted.get(lower)));
    }

    private void incrementCountry(String countryCode, Map<String, Integer> map) {
        String country = "US".equals(countryCode) ? "US" : "Others";
        map.merge(country, 1, Integer::sum);
    }

    private void incrementCountry(String countryCode, Map<String, Integer> map, String hour) {
        String country = "US".equals(countryCode) ? "US" : "Others";
        map.merge(hour + ":" + country, 1, Integer::sum);
    }

    private void incrementCountryDetailed(String countryCode, Map<String, Integer> map) {
        if (countryCode != null && !countryCode.isEmpty()) {
            map.merge(countryCode, 1, Integer::sum);
        }
    }

    private void incrementUsState(String countryCode, String regionCode, Map<String, Integer> map) {
        if ("US".equals(countryCode) && regionCode != null && !regionCode.isEmpty()) {
            map.merge(regionCode.toUpperCase(), 1, Integer::sum);
        }
    }

    private void incrementDemographic(String questionId, Map<String, String> answers,
                                       Map<String, Integer> dst, boolean isDecade) {
        if (answers == null) return;
        String answer = answers.get(questionId);
        if (answer == null) return;
        String key = isDecade ? getDecadeKey(answer) : answer;
        dst.merge(key, 1, Integer::sum);
    }

    private void incrementDemographic(String questionId, Map<String, String> answers,
                                       Map<String, Integer> dst, String hour, boolean isDecade) {
        if (answers == null) return;
        String answer = answers.get(questionId);
        if (answer == null) return;
        String key = isDecade ? getDecadeKey(answer) : answer;
        dst.merge(hour + ":" + key, 1, Integer::sum);
    }

    private void incrementMultiValue(String questionId, Map<String, String> answers,
                                       Map<String, Integer> dst) {
        if (answers == null) return;
        String answer = answers.get(questionId);
        if (answer == null || answer.isEmpty()) return;
        for (String part : answer.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                dst.merge(trimmed, 1, Integer::sum);
            }
        }
    }

    private void incrementMultiValue(String questionId, Map<String, String> answers,
                                       Map<String, Integer> dst, String hour) {
        if (answers == null) return;
        String answer = answers.get(questionId);
        if (answer == null || answer.isEmpty()) return;
        for (String part : answer.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                dst.merge(hour + ":" + trimmed, 1, Integer::sum);
            }
        }
    }

    private String getDecadeKey(String year) {
        int rounded = Math.round(Integer.parseInt(year) / 10) * 10;
        return String.format("%d-%d", rounded, rounded + 10);
    }
}
