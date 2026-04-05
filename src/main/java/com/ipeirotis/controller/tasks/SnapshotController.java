package com.ipeirotis.controller.tasks;

import com.ipeirotis.dao.DemographicsSnapshotDao;
import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.service.DemographicsSnapshotService;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.DateValidation;
import com.ipeirotis.util.SafeDateFormat;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Logger;

@RestController
public class SnapshotController {

    private static final Logger logger = Logger.getLogger(SnapshotController.class.getName());

    @Autowired
    private DemographicsSnapshotService snapshotService;

    @Autowired
    private DemographicsSnapshotDao snapshotDao;

    /**
     * Cron-triggered: pre-warms the chartData cache for the full date range.
     * Runs after the daily snapshot so the cache includes yesterday's data.
     */
    @GetMapping("/tasks/warmChartCache")
    public Map<String, Object> warmChartCache() {
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        String from = "03/26/2015";
        String to = df.format(new java.util.Date());
        long start = System.currentTimeMillis();
        snapshotService.getChartData(from, to);
        long elapsed = System.currentTimeMillis() - start;
        logger.info("Chart cache warmed for " + from + " to " + to + " in " + elapsed + "ms");
        return Map.of("status", "ok", "from", from, "to", to, "elapsedMs", elapsed);
    }

    /**
     * Cron-triggered: build weekly and monthly rollups for the most recently completed periods.
     * - Weekly: builds the rollup for the week that ended last Sunday
     * - Monthly: if today is the 1st-7th, builds the rollup for the previous month
     */
    @GetMapping("/tasks/buildRollups")
    public Map<String, Object> buildRollups() {
        LocalDate today = LocalDate.now();

        // Build weekly rollup for the most recently completed week (Mon-Sun)
        LocalDate lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        String weeklyDate = lastMonday.toString();
        snapshotService.buildWeeklyRollup(weeklyDate);
        logger.info("Built weekly rollup for " + weeklyDate);

        // Build monthly rollup if we're in the first week of the month
        String monthlyDate = null;
        if (today.getDayOfMonth() <= 7) {
            LocalDate firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
            monthlyDate = firstOfLastMonth.toString();
            snapshotService.buildMonthlyRollup(monthlyDate);
            logger.info("Built monthly rollup for " + monthlyDate);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("weeklyRollup", weeklyDate);
        result.put("monthlyRollup", monthlyDate);
        return result;
    }

    /**
     * Backfill all weekly and monthly rollups from the start of data (2015-03-23)
     * to now. Enqueues individual rollup tasks via Cloud Tasks.
     *
     * Note: during normal backfill via /tasks/backfillSnapshots, rollups are
     * automatically rebuilt after each snapshot completes (event-driven), so
     * calling this endpoint separately is only needed for standalone rollup repairs.
     */
    @GetMapping("/tasks/backfillRollups")
    public Map<String, Object> backfillRollups() {
        LocalDate dataStart = LocalDate.of(2015, 3, 23); // Monday before first data
        LocalDate today = LocalDate.now();

        int weeklyCount = 0;
        int monthlyCount = 0;

        // Enqueue weekly rollups
        LocalDate monday = dataStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (monday.isBefore(today)) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("date", monday.toString());
            params.put("granularity", "weekly");
            TaskUtils.queueTask("/tasks/buildRollup", params);
            weeklyCount++;
            monday = monday.plusWeeks(1);
        }

        // Enqueue monthly rollups
        LocalDate month = dataStart.withDayOfMonth(1);
        while (month.isBefore(today)) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("date", month.toString());
            params.put("granularity", "monthly");
            TaskUtils.queueTask("/tasks/buildRollup", params);
            monthlyCount++;
            month = month.plusMonths(1);
        }

        return Map.of("status", "ok", "weeklyTasksEnqueued", weeklyCount,
                "monthlyTasksEnqueued", monthlyCount);
    }

    /**
     * Build a single rollup. Called by Cloud Tasks during backfill.
     * Example: /tasks/buildRollup?date=2024-01-01&granularity=weekly
     */
    @PostMapping("/tasks/buildRollup")
    public Map<String, Object> buildSingleRollup(@RequestParam String date, @RequestParam String granularity) {
        DateValidation.requireValidDate(date, "date", "yyyy-MM-dd");
        if ("weekly".equals(granularity)) {
            snapshotService.buildWeeklyRollup(date);
        } else if ("monthly".equals(granularity)) {
            snapshotService.buildMonthlyRollup(date);
        } else {
            return Map.of("status", "error", "message", "Unknown granularity: " + granularity);
        }
        return Map.of("status", "ok", "date", date, "granularity", granularity);
    }

    /**
     * Cron-triggered: snapshot yesterday's demographics data, then rebuild
     * the affected weekly and monthly rollups.
     */
    @GetMapping("/tasks/snapshotDemographics")
    public Map<String, Object> snapshotYesterday() throws ParseException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        String dateStr = df.format(yesterday.getTime());
        snapshotService.buildSnapshot(dateStr);
        rebuildRollupsForDate(dateStr);
        return Map.of("status", "ok", "date", dateStr);
    }

    /**
     * Snapshot a single date, then rebuild the affected weekly and monthly rollups.
     * Called by Cloud Tasks during backfill.
     * Because rollups are idempotent, rebuilding them after each snapshot is safe:
     * intermediate rebuilds are partial, and the last snapshot to complete produces
     * the correct final result.
     * Example: /tasks/snapshotDate?date=01/15/2024
     */
    @PostMapping("/tasks/snapshotDate")
    public Map<String, Object> snapshotDate(@RequestParam String date) throws ParseException {
        DateValidation.requireValidDate(date, "date", "MM/dd/yyyy");
        snapshotService.buildSnapshot(date);
        rebuildRollupsForDate(date);
        return Map.of("status", "ok", "date", date);
    }

    /**
     * After a daily snapshot is saved, rebuild the weekly and monthly rollups
     * that contain that date. Since rollups are a deterministic sum of daily
     * snapshots, rebuilding them multiple times is harmless — the last snapshot
     * task to complete for a given week/month produces the correct final rollup.
     */
    private void rebuildRollupsForDate(String mmddyyyy) throws ParseException {
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        DateFormat sortable = SafeDateFormat.forPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(sortable.format(df.parse(mmddyyyy)));

        // Rebuild weekly rollup (week containing this date)
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        try {
            snapshotService.buildWeeklyRollup(monday.toString());
        } catch (Exception e) {
            logger.warning("Weekly rollup rebuild failed for " + monday + ": " + e.getMessage());
        }

        // Rebuild monthly rollup (month containing this date)
        LocalDate monthStart = date.withDayOfMonth(1);
        try {
            snapshotService.buildMonthlyRollup(monthStart.toString());
        } catch (Exception e) {
            logger.warning("Monthly rollup rebuild failed for " + monthStart + ": " + e.getMessage());
        }
    }

    private static final int MAX_CHUNKS = 30;

    /**
     * Backfill snapshots for a date range. If the range exceeds MAX_CHUNKS days,
     * it divides the range into ~MAX_CHUNKS equal sub-ranges and re-enqueues each
     * as a backfillSnapshots Cloud Task (recursive subdivision). Only when the range
     * is <= MAX_CHUNKS days does it enqueue individual snapshotDate tasks per day.
     * Example: /tasks/backfillSnapshots?from=01/01/2010&to=03/09/2026
     */
    @RequestMapping(value = "/tasks/backfillSnapshots", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, Object> backfill(@RequestParam String from, @RequestParam String to) throws ParseException {
        DateValidation.requireValidRange(from, to, "MM/dd/yyyy");
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

        Calendar start = Calendar.getInstance();
        start.setTime(df.parse(from));
        CalendarUtils.truncateToDay(start);

        Calendar end = Calendar.getInstance();
        end.setTime(df.parse(to));
        CalendarUtils.truncateToDay(end);

        long totalDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                end.getTimeInMillis() - start.getTimeInMillis()) + 1;

        if (totalDays > MAX_CHUNKS) {
            // Divide range into ~MAX_CHUNKS equal sub-ranges and re-enqueue
            int daysPerChunk = (int) Math.ceil((double) totalDays / MAX_CHUNKS);
            int chunksEnqueued = 0;
            Calendar chunkStart = (Calendar) start.clone();
            while (!chunkStart.after(end)) {
                Calendar chunkEnd = (Calendar) chunkStart.clone();
                chunkEnd.add(Calendar.DAY_OF_MONTH, daysPerChunk - 1);
                if (chunkEnd.after(end)) {
                    chunkEnd = (Calendar) end.clone();
                }

                Map<String, String> params = new LinkedHashMap<>();
                params.put("from", df.format(chunkStart.getTime()));
                params.put("to", df.format(chunkEnd.getTime()));
                TaskUtils.queueTask("/tasks/backfillSnapshots", params);
                chunksEnqueued++;

                chunkStart = (Calendar) chunkEnd.clone();
                chunkStart.add(Calendar.DAY_OF_MONTH, 1);
            }

            return Map.of("status", "ok", "mode", "subdivided",
                    "chunksEnqueued", chunksEnqueued, "daysPerChunk", daysPerChunk,
                    "totalDays", totalDays, "from", from, "to", to);
        }

        // Range is small enough — enqueue individual per-day tasks
        int tasksEnqueued = 0;
        Calendar current = start;
        while (!current.after(end)) {
            String dateStr = df.format(current.getTime());
            Map<String, String> params = new LinkedHashMap<>();
            params.put("date", dateStr);
            TaskUtils.queueTask("/tasks/snapshotDate", params);
            current.add(Calendar.DAY_OF_MONTH, 1);
            tasksEnqueued++;
        }

        return Map.of("status", "ok", "mode", "daily",
                "tasksEnqueued", tasksEnqueued, "from", from, "to", to);
    }

    /**
     * Coverage report: checks which dates have DemographicsSnapshot entities
     * and which are missing. Returns summary stats and lists of missing dates,
     * grouped by year-month for readability.
     *
     * Optional backfill=true parameter will enqueue snapshot tasks for all missing dates.
     *
     * Example: /tasks/snapshotCoverage
     * Example: /tasks/snapshotCoverage?from=2020-01-01&to=2024-12-31
     * Example: /tasks/snapshotCoverage?backfill=true
     */
    @GetMapping("/tasks/snapshotCoverage")
    public Map<String, Object> snapshotCoverage(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "false") boolean backfill) {

        LocalDate startDate = (from != null) ? LocalDate.parse(from) : LocalDate.of(2015, 3, 26);
        LocalDate endDate = (to != null) ? LocalDate.parse(to) : LocalDate.now().minusDays(1);

        // Load all existing snapshots in the range
        DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String displayFrom = startDate.format(dtf);
        String displayTo = endDate.format(dtf);

        List<DemographicsSnapshot> snapshots = snapshotDao.listByDateRange(displayFrom, displayTo);

        // Build set of dates that have snapshots
        Set<String> existingDates = new HashSet<>();
        int withData = 0;
        int withoutData = 0;
        for (DemographicsSnapshot s : snapshots) {
            existingDates.add(s.getDate()); // yyyy-MM-dd format
            if (s.getTotalResponses() > 0) {
                withData++;
            } else {
                withoutData++;
            }
        }

        // Walk every date and find missing ones
        List<String> missingDates = new ArrayList<>();
        Map<String, List<String>> missingByMonth = new LinkedHashMap<>();
        LocalDate current = startDate;
        long totalDays = 0;
        while (!current.isAfter(endDate)) {
            totalDays++;
            String dateStr = current.toString(); // yyyy-MM-dd
            if (!existingDates.contains(dateStr)) {
                missingDates.add(dateStr);
                String yearMonth = dateStr.substring(0, 7); // yyyy-MM
                missingByMonth.computeIfAbsent(yearMonth, k -> new ArrayList<>()).add(dateStr);
            }
            current = current.plusDays(1);
        }

        // Build per-month summary: total days, present, missing
        Map<String, Map<String, Object>> monthlySummary = new LinkedHashMap<>();
        current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            String yearMonth = current.toString().substring(0, 7);
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(endDate)) monthEnd = endDate;
            if (current.isBefore(startDate)) current = startDate;

            long daysInRange = ChronoUnit.DAYS.between(current, monthEnd) + 1;
            List<String> missing = missingByMonth.getOrDefault(yearMonth, List.of());
            long present = daysInRange - missing.size();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("days", daysInRange);
            m.put("present", present);
            m.put("missing", missing.size());
            if (!missing.isEmpty()) {
                m.put("missingDates", missing);
            }
            monthlySummary.put(yearMonth, m);

            current = current.plusMonths(1).withDayOfMonth(1);
        }

        // Optionally trigger backfill for missing dates
        int backfillTasks = 0;
        if (backfill && !missingDates.isEmpty()) {
            DateFormat mmddyyyy = SafeDateFormat.forPattern("MM/dd/yyyy");
            for (String md : missingDates) {
                try {
                    DateFormat iso = SafeDateFormat.forPattern("yyyy-MM-dd");
                    String display = mmddyyyy.format(iso.parse(md));
                    Map<String, String> params = new LinkedHashMap<>();
                    params.put("date", display);
                    TaskUtils.queueTask("/tasks/snapshotDate", params);
                    backfillTasks++;
                } catch (ParseException e) {
                    logger.warning("Failed to parse date for backfill: " + md);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("from", startDate.toString());
        result.put("to", endDate.toString());
        result.put("totalDays", totalDays);
        result.put("snapshotsFound", snapshots.size());
        result.put("snapshotsWithData", withData);
        result.put("snapshotsEmpty", withoutData);
        result.put("missingDays", missingDates.size());
        result.put("coveragePercent", totalDays > 0
                ? Math.round((double) snapshots.size() / totalDays * 10000.0) / 100.0 : 0);
        if (backfill) {
            result.put("backfillTasksEnqueued", backfillTasks);
        }
        result.put("monthlySummary", monthlySummary);
        return result;
    }
}
