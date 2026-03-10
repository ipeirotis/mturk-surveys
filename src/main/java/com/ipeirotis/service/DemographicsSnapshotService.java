package com.ipeirotis.service;

import com.ipeirotis.dao.DemographicsSnapshotDao;
import com.ipeirotis.dto.DemographicsSurveyAnswers;
import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class DemographicsSnapshotService {

    private static final Logger logger = Logger.getLogger(DemographicsSnapshotService.class.getName());

    private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final Set<String> INCOME_LABELS = new LinkedHashSet<>();

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

    private final DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

    @Autowired
    private DemographicsSnapshotDao snapshotDao;

    @Autowired
    private SurveyService surveyService;

    /**
     * Build and save a snapshot for the given date from raw UserAnswer data.
     */
    public DemographicsSnapshot buildSnapshot(String dateStr) throws ParseException {
        Calendar dateFrom = Calendar.getInstance();
        dateFrom.setTime(df.parse(dateStr));
        dateFrom.set(Calendar.HOUR_OF_DAY, 0);
        dateFrom.set(Calendar.MINUTE, 0);
        dateFrom.set(Calendar.SECOND, 0);
        dateFrom.set(Calendar.MILLISECOND, 0);

        Calendar dateTo = Calendar.getInstance();
        dateTo.setTime(dateFrom.getTime());
        dateTo.add(Calendar.DAY_OF_MONTH, 1);

        List<UserAnswer> answers = surveyService.listAnswers("demographics", dateFrom.getTime(), dateTo.getTime());

        DemographicsSnapshot snapshot = new DemographicsSnapshot();
        snapshot.setId(dateStr);
        snapshot.setDate(dateStr);
        snapshot.setDayOfWeek(DAYS[dateFrom.get(Calendar.DAY_OF_WEEK) - 1]);
        snapshot.setTotalResponses(answers.size());

        Map<String, Integer> countries = new HashMap<>();
        Map<String, Integer> yearOfBirth = new HashMap<>();
        Map<String, Integer> gender = new HashMap<>();
        Map<String, Integer> maritalStatus = new HashMap<>();
        Map<String, Integer> householdSize = new HashMap<>();
        Map<String, Integer> householdIncome = new HashMap<>();

        Map<String, Integer> hourlyTotals = new HashMap<>();
        Map<String, Integer> hourlyCountries = new HashMap<>();
        Map<String, Integer> hourlyYearOfBirth = new HashMap<>();
        Map<String, Integer> hourlyGender = new HashMap<>();
        Map<String, Integer> hourlyMaritalStatus = new HashMap<>();
        Map<String, Integer> hourlyHouseholdSize = new HashMap<>();
        Map<String, Integer> hourlyHouseholdIncome = new HashMap<>();

        for (UserAnswer ua : answers) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(ua.getDate());
            String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

            // Daily totals
            incrementCountry(ua.getLocationCountry(), countries);
            incrementDemographic("yearOfBirth", ua.getAnswers(), yearOfBirth, true);
            incrementDemographic("gender", ua.getAnswers(), gender, false);
            incrementDemographic("maritalStatus", ua.getAnswers(), maritalStatus, false);
            incrementDemographic("householdSize", ua.getAnswers(), householdSize, false);
            incrementDemographic("householdIncome", ua.getAnswers(), householdIncome, false);

            // Hourly totals
            increment(hourlyTotals, hour);
            incrementCountry(ua.getLocationCountry(), hourlyCountries, hour);
            incrementDemographic("yearOfBirth", ua.getAnswers(), hourlyYearOfBirth, hour, true);
            incrementDemographic("gender", ua.getAnswers(), hourlyGender, hour, false);
            incrementDemographic("maritalStatus", ua.getAnswers(), hourlyMaritalStatus, hour, false);
            incrementDemographic("householdSize", ua.getAnswers(), hourlyHouseholdSize, hour, false);
            incrementDemographic("householdIncome", ua.getAnswers(), hourlyHouseholdIncome, hour, false);
        }

        snapshot.setCountries(countries);
        snapshot.setYearOfBirth(yearOfBirth);
        snapshot.setGender(gender);
        snapshot.setMaritalStatus(maritalStatus);
        snapshot.setHouseholdSize(householdSize);
        snapshot.setHouseholdIncome(householdIncome);
        snapshot.setHourlyTotals(hourlyTotals);
        snapshot.setHourlyCountries(hourlyCountries);
        snapshot.setHourlyYearOfBirth(hourlyYearOfBirth);
        snapshot.setHourlyGender(hourlyGender);
        snapshot.setHourlyMaritalStatus(hourlyMaritalStatus);
        snapshot.setHourlyHouseholdSize(hourlyHouseholdSize);
        snapshot.setHourlyHouseholdIncome(hourlyHouseholdIncome);

        snapshotDao.save(snapshot);
        logger.info("Saved snapshot for " + dateStr + " with " + answers.size() + " responses");
        return snapshot;
    }

    /**
     * Get aggregated answers from pre-computed snapshots.
     */
    public DemographicsSurveyAnswersByPeriod getAggregatedAnswers(String from, String to) {
        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(from, to);

        DemographicsSurveyAnswersByPeriod result = new DemographicsSurveyAnswersByPeriod();
        result.setDaily(buildDailyAggregation(snapshots));
        result.setHourly(buildHourlyAggregation(snapshots));
        result.setWeekly(buildWeeklyAggregation(snapshots));
        return result;
    }

    private DemographicsSurveyAnswers buildDailyAggregation(List<DemographicsSnapshot> snapshots) {
        Map<String, Map<String, Float>> countries = new LinkedHashMap<>();
        Map<String, Map<String, Float>> yearOfBirth = new LinkedHashMap<>();
        Map<String, Map<String, Float>> gender = new LinkedHashMap<>();
        Map<String, Map<String, Float>> maritalStatus = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdSize = new LinkedHashMap<>();
        Map<String, Map<String, Float>> householdIncome = new LinkedHashMap<>();
        Map<String, Set<String>> labels = new HashMap<>();

        for (DemographicsSnapshot snap : snapshots) {
            if (snap.getTotalResponses() == 0) continue;

            // Convert the date string to a Date.toString() key to match frontend expectations
            String key;
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(df.parse(snap.getId()));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
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
        }

        filterIncomeLabels(labels);

        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countries);
        result.setYearOfBirth(yearOfBirth);
        result.setGender(gender);
        result.setMaritalStatus(maritalStatus);
        result.setHouseholdSize(householdSize);
        result.setHouseholdIncome(householdIncome);
        result.setLabels(labels);
        return result;
    }

    private DemographicsSurveyAnswers buildHourlyAggregation(List<DemographicsSnapshot> snapshots) {
        // Merge hourly counts across all snapshots
        Map<String, Map<String, Integer>> countryCounts = new HashMap<>();
        Map<String, Map<String, Integer>> yearOfBirthCounts = new HashMap<>();
        Map<String, Map<String, Integer>> genderCounts = new HashMap<>();
        Map<String, Map<String, Integer>> maritalStatusCounts = new HashMap<>();
        Map<String, Map<String, Integer>> householdSizeCounts = new HashMap<>();
        Map<String, Map<String, Integer>> householdIncomeCounts = new HashMap<>();

        for (DemographicsSnapshot snap : snapshots) {
            mergeHourlyCounts(snap.getHourlyCountries(), countryCounts);
            mergeHourlyCounts(snap.getHourlyYearOfBirth(), yearOfBirthCounts);
            mergeHourlyCounts(snap.getHourlyGender(), genderCounts);
            mergeHourlyCounts(snap.getHourlyMaritalStatus(), maritalStatusCounts);
            mergeHourlyCounts(snap.getHourlyHouseholdSize(), householdSizeCounts);
            mergeHourlyCounts(snap.getHourlyHouseholdIncome(), householdIncomeCounts);
        }

        Map<String, Set<String>> labels = new HashMap<>();
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countsToPercentages(countryCounts, labels, "countries"));
        result.setYearOfBirth(countsToPercentages(yearOfBirthCounts, labels, "yearOfBirth"));
        result.setGender(countsToPercentages(genderCounts, labels, "gender"));
        result.setMaritalStatus(countsToPercentages(maritalStatusCounts, labels, "maritalStatus"));
        result.setHouseholdSize(countsToPercentages(householdSizeCounts, labels, "householdSize"));
        result.setHouseholdIncome(countsToPercentages(householdIncomeCounts, labels, "householdIncome"));
        filterIncomeLabels(labels);
        result.setLabels(labels);
        return result;
    }

    private DemographicsSurveyAnswers buildWeeklyAggregation(List<DemographicsSnapshot> snapshots) {
        // Group snapshots by day of week, merge counts
        Map<String, Map<String, Integer>> countryCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> yearOfBirthCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> genderCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> maritalStatusCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdSizeCounts = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> householdIncomeCounts = new LinkedHashMap<>();

        // Initialize day-of-week keys in order
        for (String day : DAYS) {
            countryCounts.put(day, new HashMap<>());
            yearOfBirthCounts.put(day, new HashMap<>());
            genderCounts.put(day, new HashMap<>());
            maritalStatusCounts.put(day, new HashMap<>());
            householdSizeCounts.put(day, new HashMap<>());
            householdIncomeCounts.put(day, new HashMap<>());
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
        }

        Map<String, Set<String>> labels = new HashMap<>();
        DemographicsSurveyAnswers result = new DemographicsSurveyAnswers();
        result.setCountries(countsToPercentages(countryCounts, labels, "countries"));
        result.setYearOfBirth(countsToPercentages(yearOfBirthCounts, labels, "yearOfBirth"));
        result.setGender(countsToPercentages(genderCounts, labels, "gender"));
        result.setMaritalStatus(countsToPercentages(maritalStatusCounts, labels, "maritalStatus"));
        result.setHouseholdSize(countsToPercentages(householdSizeCounts, labels, "householdSize"));
        result.setHouseholdIncome(countsToPercentages(householdIncomeCounts, labels, "householdIncome"));
        filterIncomeLabels(labels);
        result.setLabels(labels);
        return result;
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

    private void increment(Map<String, Integer> map, String key) {
        map.merge(key, 1, Integer::sum);
    }

    private void incrementCountry(String countryCode, Map<String, Integer> map) {
        String country = "US".equals(countryCode) ? "US" : "Others";
        map.merge(country, 1, Integer::sum);
    }

    private void incrementCountry(String countryCode, Map<String, Integer> map, String hour) {
        String country = "US".equals(countryCode) ? "US" : "Others";
        map.merge(hour + ":" + country, 1, Integer::sum);
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

    private String getDecadeKey(String year) {
        int rounded = Math.round(Integer.parseInt(year) / 10) * 10;
        return String.format("%d-%d", rounded, rounded + 10);
    }
}
