package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DemographicsSnapshotService;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class SnapshotController {

    private static final Logger logger = Logger.getLogger(SnapshotController.class.getName());

    @Autowired
    private DemographicsSnapshotService snapshotService;

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
     * Cron-triggered: snapshot yesterday's demographics data.
     */
    @GetMapping("/tasks/snapshotDemographics")
    public Map<String, Object> snapshotYesterday() throws ParseException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
        String dateStr = df.format(yesterday.getTime());
        snapshotService.buildSnapshot(dateStr);
        return Map.of("status", "ok", "date", dateStr);
    }

    /**
     * Snapshot a single date. Called by Cloud Tasks during backfill.
     * Example: /tasks/snapshotDate?date=01/15/2024
     */
    @GetMapping("/tasks/snapshotDate")
    public Map<String, Object> snapshotDate(@RequestParam String date) throws ParseException {
        snapshotService.buildSnapshot(date);
        return Map.of("status", "ok", "date", date);
    }

    private static final int MAX_CHUNKS = 30;

    /**
     * Backfill snapshots for a date range. If the range exceeds MAX_CHUNKS days,
     * it divides the range into ~MAX_CHUNKS equal sub-ranges and re-enqueues each
     * as a backfillSnapshots Cloud Task (recursive subdivision). Only when the range
     * is <= MAX_CHUNKS days does it enqueue individual snapshotDate tasks per day.
     * Example: /tasks/backfillSnapshots?from=01/01/2010&to=03/09/2026
     */
    @GetMapping("/tasks/backfillSnapshots")
    public Map<String, Object> backfill(@RequestParam String from, @RequestParam String to) throws ParseException {
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
}
