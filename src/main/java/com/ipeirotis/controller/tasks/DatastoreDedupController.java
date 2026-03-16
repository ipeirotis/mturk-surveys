package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.DatastoreDedupService;
import com.ipeirotis.service.DatastoreDedupService.DedupResult;
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

@RestController
public class DatastoreDedupController {

	@Autowired
	private DatastoreDedupService datastoreDedupService;

	/**
	 * Count duplicates for a single date (read-only, no deletions).
	 * Example: /tasks/countDuplicatesDate?date=2024-01-15
	 */
	@GetMapping("/tasks/countDuplicatesDate")
	public Map<String, Object> countDuplicatesDate(@RequestParam String date) throws ParseException {
		DedupResult result = datastoreDedupService.countDuplicates(date);
		return result.toMap();
	}

	/**
	 * Audit duplicates across a date range. Returns per-day counts for days
	 * that have duplicates, plus totals. Read-only, no deletions.
	 * For large ranges, processes directly (no Cloud Tasks) since it's read-only.
	 * Example: /tasks/countDuplicates?from=2024-01-01&to=2024-12-31
	 */
	@GetMapping("/tasks/countDuplicates")
	public Map<String, Object> countDuplicatesRange(@RequestParam String from,
													@RequestParam String to) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");
		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(to));
		CalendarUtils.truncateToDay(end);

		List<Map<String, Object>> daysWithDuplicates = new ArrayList<>();
		int totalEntries = 0;
		int totalDuplicates = 0;
		int daysScanned = 0;

		Calendar current = (Calendar) start.clone();
		while (!current.after(end)) {
			String dateStr = df.format(current.getTime());
			DedupResult result = datastoreDedupService.countDuplicates(dateStr);
			daysScanned++;
			totalEntries += result.totalEntries;
			totalDuplicates += result.duplicateEntries;

			if (result.duplicateEntries > 0) {
				daysWithDuplicates.add(result.toMap());
			}

			current.add(Calendar.DAY_OF_MONTH, 1);
		}

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", "ok");
		response.put("from", from);
		response.put("to", to);
		response.put("daysScanned", daysScanned);
		response.put("totalEntries", totalEntries);
		response.put("totalDuplicates", totalDuplicates);
		response.put("daysWithDuplicates", daysWithDuplicates.size());
		response.put("details", daysWithDuplicates);
		return response;
	}

	/**
	 * Deduplicate Datastore UserAnswer entries for a single date.
	 * Use dryRun=true to preview without deleting.
	 * Example: /tasks/dedupDatastoreDate?date=2024-01-15
	 * Example: /tasks/dedupDatastoreDate?date=2024-01-15&dryRun=true
	 */
	@GetMapping("/tasks/dedupDatastoreDate")
	public Map<String, Object> deduplicateDate(@RequestParam String date,
											   @RequestParam(defaultValue = "false") boolean dryRun) throws ParseException {
		if (dryRun) {
			DedupResult result = datastoreDedupService.countDuplicates(date);
			Map<String, Object> response = result.toMap();
			response.put("status", "dry_run");
			return response;
		}
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
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");

		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(to));
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
				params.put("from", df.format(chunkStart.getTime()));
				params.put("to", df.format(chunkEnd.getTime()));
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
			String dateStr = df.format(current.getTime());
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
