package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DatastoreRestoreService;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Controller for comparing Datastore vs BigQuery backup counts and
 * restoring missing entries from the BigQuery backup table
 * (test.UserAnswer_2025MAR20) into Datastore.
 */
@RestController
public class DatastoreRestoreController {

	private static final Logger logger = Logger.getLogger(DatastoreRestoreController.class.getName());
	private static final int MAX_CHUNKS = 30;

	@Autowired
	private DatastoreRestoreService restoreService;

	/**
	 * Compare daily counts between Datastore and BigQuery backup for a date range.
	 * Returns only days where the counts differ.
	 *
	 * Example: /tasks/compareDatastoreBigQuery?from=2020-11-03&to=2025-03-20
	 *
	 * @param from start date in yyyy-MM-dd format
	 * @param to   end date in yyyy-MM-dd format
	 */
	@GetMapping("/tasks/compareDatastoreBigQuery")
	public Map<String, Object> compareCounts(
			@RequestParam String from, @RequestParam String to,
			@RequestParam(required = false) String table) throws ParseException {
		List<Map<String, Object>> mismatches = restoreService.compareCounts(from, to, table);

		long totalDelta = 0;
		for (Map<String, Object> row : mismatches) {
			totalDelta += ((Number) row.get("delta")).longValue();
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("status", "ok");
		result.put("from", from);
		result.put("to", to);
		result.put("daysWithDifferences", mismatches.size());
		result.put("totalDelta", totalDelta);
		result.put("mismatches", mismatches);
		return result;
	}

	/**
	 * Restore a single day's entries from BigQuery backup into Datastore.
	 * Uses original Datastore entity IDs so existing entries are overwritten.
	 *
	 * Example: /tasks/restoreDateFromBigQuery?date=2024-01-15
	 *
	 * @param date date in yyyy-MM-dd format
	 */
	@GetMapping("/tasks/restoreDateFromBigQuery")
	public Map<String, Object> restoreDate(@RequestParam String date,
			@RequestParam(required = false) String table) {
		int restored = restoreService.restoreDate(date, table);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("status", "ok");
		result.put("date", date);
		result.put("entitiesRestored", restored);
		if (table != null) result.put("table", table);
		return result;
	}

	/**
	 * Backfill restore for a date range. If the range exceeds MAX_CHUNKS days,
	 * it subdivides into sub-ranges and enqueues them as Cloud Tasks.
	 * When the range is small enough, enqueues individual per-day restore tasks.
	 *
	 * Example: /tasks/backfillRestoreFromBigQuery?from=2020-11-03&to=2025-03-20
	 *
	 * @param from start date in yyyy-MM-dd format
	 * @param to   end date in yyyy-MM-dd format
	 */
	@GetMapping("/tasks/backfillRestoreFromBigQuery")
	public Map<String, Object> backfillRestore(
			@RequestParam String from, @RequestParam String to,
			@RequestParam(required = false) String table) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");

		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(to));
		CalendarUtils.truncateToDay(end);

		long totalDays = TimeUnit.MILLISECONDS.toDays(
				end.getTimeInMillis() - start.getTimeInMillis()) + 1;

		if (totalDays > MAX_CHUNKS) {
			// Subdivide into chunks and re-enqueue
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
				if (table != null) params.put("table", table);
				TaskUtils.queueTask("/tasks/backfillRestoreFromBigQuery", params);
				chunksEnqueued++;

				chunkStart = (Calendar) chunkEnd.clone();
				chunkStart.add(Calendar.DAY_OF_MONTH, 1);
			}

			return Map.of("status", "ok", "mode", "subdivided",
					"chunksEnqueued", chunksEnqueued, "daysPerChunk", daysPerChunk,
					"totalDays", totalDays, "from", from, "to", to);
		}

		// Range is small enough — enqueue individual per-day restore tasks
		int tasksEnqueued = 0;
		Calendar current = (Calendar) start.clone();
		while (!current.after(end)) {
			String dateStr = df.format(current.getTime());
			Map<String, String> params = new LinkedHashMap<>();
			params.put("date", dateStr);
			if (table != null) params.put("table", table);
			TaskUtils.queueTask("/tasks/restoreDateFromBigQuery", params);
			current.add(Calendar.DAY_OF_MONTH, 1);
			tasksEnqueued++;
		}

		return Map.of("status", "ok", "mode", "daily",
				"tasksEnqueued", tasksEnqueued, "from", from, "to", to);
	}

	/**
	 * Smart restore: compare counts and only restore days where
	 * Datastore has fewer entries than BigQuery backup.
	 * Enqueues restore tasks only for days with missing data.
	 *
	 * Example: /tasks/smartRestoreFromBigQuery?from=2020-11-03&to=2025-03-20
	 *
	 * @param from start date in yyyy-MM-dd format
	 * @param to   end date in yyyy-MM-dd format
	 */
	@GetMapping("/tasks/smartRestoreFromBigQuery")
	public Map<String, Object> smartRestore(
			@RequestParam String from, @RequestParam String to,
			@RequestParam(required = false) String table) throws ParseException {
		List<Map<String, Object>> mismatches = restoreService.compareCounts(from, to, table);

		int tasksEnqueued = 0;
		for (Map<String, Object> mismatch : mismatches) {
			long delta = ((Number) mismatch.get("delta")).longValue();
			if (delta > 0) {
				// BigQuery has more entries — restore this day
				String date = (String) mismatch.get("date");
				Map<String, String> params = new LinkedHashMap<>();
				params.put("date", date);
				if (table != null) params.put("table", table);
				TaskUtils.queueTask("/tasks/restoreDateFromBigQuery", params);
				tasksEnqueued++;
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("status", "ok");
		result.put("from", from);
		result.put("to", to);
		result.put("daysChecked", mismatches.size());
		result.put("restoreTasksEnqueued", tasksEnqueued);
		result.put("mismatches", mismatches);
		return result;
	}
}
