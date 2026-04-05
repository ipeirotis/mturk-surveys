package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.BigQueryExportService;
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
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class BigQueryExportController {

	@Autowired
	private BigQueryExportService bigQueryExportService;

	/**
	 * Cron-triggered: export yesterday's demographics data to BigQuery.
	 */
	@GetMapping("/tasks/exportToBigQuery")
	public Map<String, Object> exportYesterday() throws ParseException {
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
		String dateStr = df.format(yesterday.getTime());
		int rows = bigQueryExportService.exportDate(dateStr);
		return Map.of("status", "ok", "date", dateStr, "rowsExported", rows);
	}

	/**
	 * Export a single date to BigQuery. Called by Cloud Tasks during backfill.
	 * Returns 200 even on BigQuery errors (to prevent Cloud Tasks retries that
	 * cause transaction conflict storms). Errors are logged for monitoring.
	 * Example: /tasks/exportDateToBigQuery?date=01/15/2024
	 */
	@PostMapping("/tasks/exportDateToBigQuery")
	public Map<String, Object> exportDate(@RequestParam String date) {
		DateValidation.requireValidDate(date, "date", "MM/dd/yyyy");
		try {
			int rows = bigQueryExportService.exportDate(date);
			return Map.of("status", "ok", "date", date, "rowsExported", rows);
		} catch (Exception e) {
			org.slf4j.LoggerFactory.getLogger(getClass())
					.warn("BigQuery export failed for " + date + ": " + e.getMessage(), e);
			return Map.of("status", "error", "date", date, "error", e.getMessage() != null ? e.getMessage() : e.getClass().getName());
		}
	}

	private static final int MAX_CHUNKS = 30;

	/**
	 * Backfill BigQuery export for a date range. If the range exceeds MAX_CHUNKS days,
	 * it divides the range into ~MAX_CHUNKS equal sub-ranges and re-enqueues each
	 * as a backfillBigQuery Cloud Task (recursive subdivision). Only when the range
	 * is <= MAX_CHUNKS days does it enqueue individual exportDateToBigQuery tasks per day.
	 * Example: /tasks/backfillBigQuery?from=01/01/2015&to=03/09/2026
	 */
	@RequestMapping(value = "/tasks/backfillBigQuery", method = {RequestMethod.GET, RequestMethod.POST})
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
				TaskUtils.queueTask("/tasks/backfillBigQuery", params);
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
			TaskUtils.queueTask("/tasks/exportDateToBigQuery", params);
			current.add(Calendar.DAY_OF_MONTH, 1);
			tasksEnqueued++;
		}

		return Map.of("status", "ok", "mode", "daily",
				"tasksEnqueued", tasksEnqueued, "from", from, "to", to);
	}
}
