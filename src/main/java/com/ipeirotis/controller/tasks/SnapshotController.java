package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DemographicsSnapshotService;
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

@RestController
public class SnapshotController {

    private final DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

    @Autowired
    private DemographicsSnapshotService snapshotService;

    /**
     * Cron-triggered: snapshot yesterday's demographics data.
     */
    @GetMapping("/tasks/snapshotDemographics")
    public Map<String, Object> snapshotYesterday() throws ParseException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
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

    /**
     * Backfill snapshots for a date range by enqueueing one Cloud Task per day.
     * Each task calls /tasks/snapshotDate for a single date, avoiding timeouts.
     * Example: /tasks/backfillSnapshots?from=01/01/2010&to=03/09/2026
     */
    @GetMapping("/tasks/backfillSnapshots")
    public Map<String, Object> backfill(@RequestParam String from, @RequestParam String to) throws ParseException {
        Calendar current = Calendar.getInstance();
        current.setTime(df.parse(from));
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTime(df.parse(to));
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        int tasksEnqueued = 0;
        while (!current.after(end)) {
            String dateStr = df.format(current.getTime());
            Map<String, String> params = new LinkedHashMap<>();
            params.put("date", dateStr);
            TaskUtils.queueTask("/tasks/snapshotDate", params);
            current.add(Calendar.DAY_OF_MONTH, 1);
            tasksEnqueued++;
        }

        return Map.of("status", "ok", "tasksEnqueued", tasksEnqueued, "from", from, "to", to);
    }
}
