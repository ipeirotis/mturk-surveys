package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DemographicsSnapshotService;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
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
     * Backfill snapshots for a date range (inclusive).
     * Example: /tasks/backfillSnapshots?from=01/01/2024&to=03/09/2026
     */
    @GetMapping("/tasks/backfillSnapshots")
    public Map<String, Object> backfill(@RequestParam String from, @RequestParam String to) throws ParseException {
        int count = snapshotService.backfill(from, to);
        return Map.of("status", "ok", "snapshotsCreated", count, "from", from, "to", to);
    }
}
