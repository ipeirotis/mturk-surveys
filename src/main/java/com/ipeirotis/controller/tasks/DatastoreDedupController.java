package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DatastoreDedupService;
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

@RestController
public class DatastoreDedupController {

	@Autowired
	private DatastoreDedupService datastoreDedupService;

	/**
	 * Deduplicate Datastore UserAnswer entries for a single date.
	 * Example: /tasks/dedupDatastoreDate?date=2024-01-15
	 */
	@GetMapping("/tasks/dedupDatastoreDate")
	public Map<String, Object> deduplicateDate(@RequestParam String date) throws ParseException {
		int deleted = datastoreDedupService.deduplicateDate(date);
		return Map.of("status", "ok", "date", date, "duplicatesDeleted", deleted);
	}

	private static final int MAX_CHUNKS = 30;

	/**
	 * Backfill Datastore dedup for a date range. Uses Cloud Tasks subdivision
	 * for large ranges (same pattern as BigQueryExportController.backfill).
	 * Example: /tasks/dedupDatastore?from=2015-03-26&to=2026-03-16
	 */
	@GetMapping("/tasks/dedupDatastore")
	public Map<String, Object> deduplicateRange(@RequestParam String from, @RequestParam String to) throws ParseException {
		DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
		DateFormat taskDf = SafeDateFormat.forPattern("yyyy-MM-dd");

		Calendar start = Calendar.getInstance();
		start.setTime(sortableDf.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(sortableDf.parse(to));
		CalendarUtils.truncateToDay(end);

		long totalDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
				end.getTimeInMillis() - start.getTimeInMillis()) + 1;

		if (totalDays > MAX_CHUNKS) {
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
				params.put("from", taskDf.format(chunkStart.getTime()));
				params.put("to", taskDf.format(chunkEnd.getTime()));
				TaskUtils.queueTask("/tasks/dedupDatastore", params);
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
		Calendar current = (Calendar) start.clone();
		while (!current.after(end)) {
			String dateStr = taskDf.format(current.getTime());
			Map<String, String> params = new LinkedHashMap<>();
			params.put("date", dateStr);
			TaskUtils.queueTask("/tasks/dedupDatastoreDate", params);
			current.add(Calendar.DAY_OF_MONTH, 1);
			tasksEnqueued++;
		}

		return Map.of("status", "ok", "mode", "daily",
				"tasksEnqueued", tasksEnqueued, "from", from, "to", to);
	}
}
