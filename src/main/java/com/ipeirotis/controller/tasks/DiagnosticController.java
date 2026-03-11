package com.ipeirotis.controller.tasks;

import com.google.cloud.bigquery.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import com.ipeirotis.util.TaskUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Diagnostic endpoint to debug missing UserAnswer data.
 * Compares different query strategies to identify index vs data issues.
 */
@RestController
public class DiagnosticController {

	private static final Logger logger = Logger.getLogger(DiagnosticController.class.getName());

	/**
	 * Diagnose data volume for a single date by running multiple query strategies.
	 *
	 * Strategy 1: date-only filter (uses single-property date index)
	 * Strategy 2: surveyId + date filter (uses composite index)
	 * Strategy 3: surveyId equality only, then filter dates in-memory (bypasses date index)
	 * Strategy 4: keys-only count with date filter
	 * Strategy 5: keys-only count with composite filter
	 *
	 * Example: /tasks/diagnoseVolume?date=01/15/2021
	 * Add full=true to include Strategy 3 (slow full-scan): /tasks/diagnoseVolume?date=01/15/2021&full=true
	 */
	@GetMapping("/tasks/diagnoseVolume")
	public Map<String, Object> diagnoseVolume(
			@RequestParam String date,
			@RequestParam(required = false, defaultValue = "false") boolean full) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
		Calendar dateFrom = Calendar.getInstance();
		dateFrom.setTime(df.parse(date));
		CalendarUtils.truncateToDay(dateFrom);

		Calendar dateTo = Calendar.getInstance();
		dateTo.setTime(dateFrom.getTime());
		dateTo.add(Calendar.DAY_OF_MONTH, 1);

		Date from = dateFrom.getTime();
		Date to = dateTo.getTime();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("date", date);
		result.put("queryFrom", from.toString());
		result.put("queryTo", to.toString());

		// Strategy 1: date-only filter — stream entities in small chunks to avoid OOM
		try {
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("date >=", from)
					.filter("date <", to)
					.chunk(100);
			int total = 0;
			int withSurveyId = 0;
			int withDemographicsSurveyId = 0;
			int nullSurveyId = 0;
			Set<String> surveyIds = new TreeSet<>();
			for (UserAnswer ua : q) {
				total++;
				if (ua.getSurveyId() == null) {
					nullSurveyId++;
				} else {
					withSurveyId++;
					surveyIds.add(ua.getSurveyId());
					if ("demographics".equals(ua.getSurveyId())) {
						withDemographicsSurveyId++;
					}
				}
			}
			Map<String, Object> s1 = new LinkedHashMap<>();
			s1.put("description", "date-only filter (single-property date index, streamed)");
			s1.put("totalCount", total);
			s1.put("withDemographicsSurveyId", withDemographicsSurveyId);
			s1.put("nullSurveyId", nullSurveyId);
			s1.put("distinctSurveyIds", surveyIds);
			result.put("strategy1_dateOnly", s1);
		} catch (Exception e) {
			result.put("strategy1_dateOnly", Map.of("error", e.getMessage()));
			logger.warning("Strategy 1 failed: " + e.getMessage());
		}

		// Strategy 2: surveyId=demographics + date filter — keys-only count
		try {
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("surveyId", "demographics")
					.filter("date >=", from)
					.filter("date <", to)
					.order("date");
			int keysCount = q.keys().list().size();
			Map<String, Object> s2 = new LinkedHashMap<>();
			s2.put("description", "surveyId=demographics + date filter (composite index, keys-only)");
			s2.put("totalCount", keysCount);
			result.put("strategy2_compositeIndex", s2);
		} catch (Exception e) {
			result.put("strategy2_compositeIndex", Map.of("error", e.getMessage()));
			logger.warning("Strategy 2 failed: " + e.getMessage());
		}

		// Strategy 3: surveyId=demographics only, filter dates in Java (bypasses date index)
		// This is SLOW — scans all demographics entities. Only runs with full=true.
		if (full) {
			try {
				Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
						.filter("surveyId", "demographics")
						.chunk(100);
				int totalForSurvey = 0;
				int inDateRange = 0;
				for (UserAnswer ua : q) {
					totalForSurvey++;
					if (ua.getDate() != null && !ua.getDate().before(from) && ua.getDate().before(to)) {
						inDateRange++;
					}
					if (totalForSurvey > 200000) {
						Map<String, Object> s3 = new LinkedHashMap<>();
						s3.put("description", "surveyId=demographics, dates filtered in-memory (TRUNCATED at 200k)");
						s3.put("totalScanned", totalForSurvey);
						s3.put("inDateRangeCount", inDateRange);
						s3.put("truncated", true);
						result.put("strategy3_inMemoryDateFilter", s3);
						break;
					}
				}
				if (totalForSurvey <= 200000) {
					Map<String, Object> s3 = new LinkedHashMap<>();
					s3.put("description", "surveyId=demographics, dates filtered in-memory");
					s3.put("totalScanned", totalForSurvey);
					s3.put("inDateRangeCount", inDateRange);
					s3.put("truncated", false);
					result.put("strategy3_inMemoryDateFilter", s3);
				}
			} catch (Exception e) {
				result.put("strategy3_inMemoryDateFilter", Map.of("error", e.getMessage()));
				logger.warning("Strategy 3 failed: " + e.getMessage());
			}
		} else {
			result.put("strategy3_inMemoryDateFilter", "SKIPPED (add full=true to enable — slow full-scan)");
		}

		// Strategy 4: keys-only count with date filter
		try {
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("date >=", from)
					.filter("date <", to);
			int keysCount = q.keys().list().size();
			Map<String, Object> s4 = new LinkedHashMap<>();
			s4.put("description", "keys-only query with date filter");
			s4.put("keysCount", keysCount);
			result.put("strategy4_keysOnlyDate", s4);
		} catch (Exception e) {
			result.put("strategy4_keysOnlyDate", Map.of("error", e.getMessage()));
			logger.warning("Strategy 4 failed: " + e.getMessage());
		}

		// Strategy 5: keys-only count with composite filter
		try {
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("surveyId", "demographics")
					.filter("date >=", from)
					.filter("date <", to)
					.order("date");
			int keysCount = q.keys().list().size();
			Map<String, Object> s5 = new LinkedHashMap<>();
			s5.put("description", "keys-only query with surveyId + date filter (composite index)");
			s5.put("keysCount", keysCount);
			result.put("strategy5_keysOnlyComposite", s5);
		} catch (Exception e) {
			result.put("strategy5_keysOnlyComposite", Map.of("error", e.getMessage()));
			logger.warning("Strategy 5 failed: " + e.getMessage());
		}

		logger.info("Diagnostic results for " + date + ": " + result);
		return result;
	}

	/**
	 * Diagnose data volume for a date range, returning daily counts.
	 * Uses the composite index query (surveyId=demographics + date) which is
	 * what the snapshot builder and BigQuery export should be using.
	 *
	 * Example: /tasks/diagnoseVolumeRange?from=10/01/2020&to=12/31/2022
	 */
	@GetMapping("/tasks/diagnoseVolumeRange")
	public Map<String, Object> diagnoseVolumeRange(
			@RequestParam String from, @RequestParam String to) throws ParseException {

		DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
		DateFormat sortable = SafeDateFormat.forPattern("yyyy-MM-dd");

		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(to));
		CalendarUtils.truncateToDay(end);
		end.add(Calendar.DAY_OF_MONTH, 1);

		// Query all answers in the range using the composite index — stream in chunks
		Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.filter("date >=", start.getTime())
				.filter("date <", end.getTime())
				.order("date")
				.chunk(100);

		Map<String, Integer> dailyCounts = new TreeMap<>();
		int total = 0;
		int nullDate = 0;
		int nullSurveyId = 0;

		for (UserAnswer ua : q) {
			total++;
			if (ua.getDate() == null) {
				nullDate++;
				continue;
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(ua.getDate());
			CalendarUtils.truncateToDay(cal);
			String day = sortable.format(cal.getTime());
			dailyCounts.merge(day, 1, Integer::sum);
		}

		// Also run without surveyId filter for comparison — stream in chunks
		Query<UserAnswer> qNoSurvey = ofy().load().type(UserAnswer.class)
				.filter("date >=", start.getTime())
				.filter("date <", end.getTime())
				.chunk(100);

		Map<String, Integer> dailyCountsNoSurveyFilter = new TreeMap<>();
		int totalNoSurveyFilter = 0;

		for (UserAnswer ua : qNoSurvey) {
			totalNoSurveyFilter++;
			if (ua.getDate() == null) continue;
			Calendar cal = Calendar.getInstance();
			cal.setTime(ua.getDate());
			CalendarUtils.truncateToDay(cal);
			String day = sortable.format(cal.getTime());
			dailyCountsNoSurveyFilter.merge(day, 1, Integer::sum);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("from", from);
		result.put("to", to);
		result.put("totalWithSurveyFilter", total);
		result.put("totalWithoutSurveyFilter", totalNoSurveyFilter);
		result.put("nullDateCount", nullDate);
		result.put("dailyCountsWithSurveyFilter", dailyCounts);
		result.put("dailyCountsWithoutSurveyFilter", dailyCountsNoSurveyFilter);

		// Find days where the two counts differ
		Map<String, Map<String, Integer>> discrepancies = new TreeMap<>();
		Set<String> allDays = new TreeSet<>();
		allDays.addAll(dailyCounts.keySet());
		allDays.addAll(dailyCountsNoSurveyFilter.keySet());
		for (String day : allDays) {
			int withFilter = dailyCounts.getOrDefault(day, 0);
			int withoutFilter = dailyCountsNoSurveyFilter.getOrDefault(day, 0);
			if (withFilter != withoutFilter) {
				Map<String, Integer> diff = new LinkedHashMap<>();
				diff.put("withSurveyFilter", withFilter);
				diff.put("withoutSurveyFilter", withoutFilter);
				discrepancies.put(day, diff);
			}
		}
		result.put("daysWithDiscrepancies", discrepancies);

		logger.info("Diagnostic range results: total(with)=" + total
				+ " total(without)=" + totalNoSurveyFilter
				+ " discrepancies=" + discrepancies.size());
		return result;
	}

	/**
	 * Look up specific UserAnswer entities by their numeric IDs.
	 * Use this to check whether entities from BigQuery still exist in Datastore.
	 *
	 * Example: /tasks/lookupEntities?ids=12345,67890,11111
	 */
	@GetMapping("/tasks/lookupEntities")
	public Map<String, Object> lookupEntities(@RequestParam String ids) {
		String[] idParts = ids.split(",");
		List<Long> idList = new ArrayList<>();
		for (String part : idParts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				idList.add(Long.parseLong(trimmed));
			}
		}

		List<Key<UserAnswer>> keys = new ArrayList<>();
		for (Long id : idList) {
			keys.add(Key.create(UserAnswer.class, id));
		}

		Map<Key<UserAnswer>, UserAnswer> found = ofy().load().keys(keys);

		List<Map<String, Object>> results = new ArrayList<>();
		int existCount = 0;
		int missingCount = 0;
		for (Long id : idList) {
			Key<UserAnswer> key = Key.create(UserAnswer.class, id);
			UserAnswer ua = found.get(key);
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("id", id);
			if (ua != null) {
				existCount++;
				entry.put("exists", true);
				entry.put("date", ua.getDate() != null ? ua.getDate().toString() : null);
				entry.put("surveyId", ua.getSurveyId());
				entry.put("hitId", ua.getHitId());
			} else {
				missingCount++;
				entry.put("exists", false);
			}
			results.add(entry);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("requestedCount", idList.size());
		result.put("existCount", existCount);
		result.put("missingCount", missingCount);
		result.put("entities", results);
		return result;
	}

	/**
	 * Count total UserAnswer entities in Datastore using keys-only scan.
	 * This gives us the true entity count regardless of indexes.
	 * Warning: this scans ALL entities and may be slow/expensive.
	 *
	 * Example: /tasks/countAllEntities
	 * With surveyId filter: /tasks/countAllEntities?surveyId=demographics
	 */
	@GetMapping("/tasks/countAllEntities")
	public Map<String, Object> countAllEntities(
			@RequestParam(required = false) String surveyId) {
		Query<UserAnswer> q = ofy().load().type(UserAnswer.class);
		if (surveyId != null) {
			q = q.filter("surveyId", surveyId);
		}

		int count = 0;
		for (Key<UserAnswer> key : q.keys()) {
			count++;
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("surveyIdFilter", surveyId);
		result.put("totalEntities", count);
		return result;
	}

	/**
	 * Re-index UserAnswer entities by loading via the surveyId single-property
	 * index (bypassing the date index) and re-saving them. Re-saving forces
	 * Datastore to rebuild all property indexes including date.
	 *
	 * Uses cursor-based pagination with continuation via Cloud Tasks to handle
	 * large datasets without timing out.
	 *
	 * Example: /tasks/reindexUserAnswers?from=10/01/2020&to=12/31/2022
	 * With cursor: /tasks/reindexUserAnswers?from=10/01/2020&to=12/31/2022&cursor=...
	 */
	@GetMapping("/tasks/reindexUserAnswers")
	public Map<String, Object> reindexUserAnswers(
			@RequestParam String from, @RequestParam String to,
			@RequestParam(required = false) String cursor) throws ParseException {

		DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");

		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(from));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(to));
		CalendarUtils.truncateToDay(end);
		end.add(Calendar.DAY_OF_MONTH, 1); // exclusive end

		Date fromDate = start.getTime();
		Date toDate = end.getTime();

		// Query by surveyId only (single-property index) — deliberately
		// avoids the date index which may be corrupt.
		// Process BATCH_LIMIT entities per invocation, then continue via Cloud Task.
		int BATCH_LIMIT = 5000;

		Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.limit(BATCH_LIMIT);

		if (cursor != null) {
			q = q.startAt(com.google.cloud.datastore.Cursor.fromUrlSafe(cursor));
		}

		int resaved = 0;
		int scanned = 0;
		int skippedOutOfRange = 0;
		List<UserAnswer> batch = new ArrayList<>();
		String nextCursor = null;

		com.google.cloud.datastore.QueryResults<UserAnswer> iterator = q.iterator();
		while (iterator.hasNext()) {
			UserAnswer ua = iterator.next();
			scanned++;

			if (ua.getDate() == null
					|| ua.getDate().before(fromDate)
					|| !ua.getDate().before(toDate)) {
				skippedOutOfRange++;
				continue;
			}

			batch.add(ua);
			if (batch.size() >= 25) {
				ofy().save().entities(batch).now();
				resaved += batch.size();
				batch.clear();
			}
		}

		if (!batch.isEmpty()) {
			ofy().save().entities(batch).now();
			resaved += batch.size();
		}

		// If we processed the full batch limit, there may be more entities
		if (scanned >= BATCH_LIMIT) {
			nextCursor = iterator.getCursorAfter().toUrlSafe();

			// Enqueue continuation task
			Map<String, String> params = new LinkedHashMap<>();
			params.put("from", from);
			params.put("to", to);
			params.put("cursor", nextCursor);
			TaskUtils.queueTask("/tasks/reindexUserAnswers", params);
		}

		logger.info("Re-index batch: scanned=" + scanned + " resaved=" + resaved
				+ " skippedOutOfRange=" + skippedOutOfRange
				+ " hasMore=" + (nextCursor != null));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("status", "ok");
		result.put("scanned", scanned);
		result.put("resaved", resaved);
		result.put("skippedOutOfRange", skippedOutOfRange);
		result.put("continuationEnqueued", nextCursor != null);
		result.put("from", from);
		result.put("to", to);
		return result;
	}

	/**
	 * Preview what a restore from BigQuery would do, without writing anything.
	 * Shows the BigQuery table schema and a sample of rows that would be restored.
	 *
	 * Example: /tasks/previewRestore?dataset=test&table=UserAnswer_2025MAR20&date=2021-01-15
	 */
	@GetMapping("/tasks/previewRestore")
	public Map<String, Object> previewRestore(
			@RequestParam String dataset,
			@RequestParam String table,
			@RequestParam String date) {

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("dataset", dataset);
		result.put("table", table);
		result.put("date", date);

		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

			// Get table schema
			TableId tableId = TableId.of(dataset, table);
			Table bqTable = bigQuery.getTable(tableId);
			if (bqTable == null) {
				result.put("error", "Table not found: " + dataset + "." + table);
				return result;
			}

			Schema schema = bqTable.getDefinition().getSchema();
			List<String> columns = new ArrayList<>();
			for (Field field : schema.getFields()) {
				columns.add(field.getName() + " (" + field.getType() + ")");
			}
			result.put("schema", columns);

			// Query BigQuery for the given date
			String sql = String.format(
					"SELECT * FROM `%s.%s.%s` WHERE DATE(date) = '%s' LIMIT 5",
					projectId, dataset, table, date);

			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(queryConfig);

			result.put("totalRowsForDate", tableResult.getTotalRows());

			List<Map<String, String>> sampleRows = new ArrayList<>();
			for (FieldValueList row : tableResult.iterateAll()) {
				Map<String, String> rowMap = new LinkedHashMap<>();
				for (Field field : schema.getFields()) {
					FieldValue val = row.get(field.getName());
					rowMap.put(field.getName(), val.isNull() ? null : val.getStringValue());
				}
				sampleRows.add(rowMap);
			}
			result.put("sampleRows", sampleRows);

			// Count how many already exist in Datastore
			String countSql = String.format(
					"SELECT __key__.id as entity_id FROM `%s.%s.%s` WHERE DATE(date) = '%s'",
					projectId, dataset, table, date);
			QueryJobConfiguration countConfig = QueryJobConfiguration.newBuilder(countSql).build();
			TableResult countResult = bigQuery.query(countConfig);

			List<Long> bqIds = new ArrayList<>();
			for (FieldValueList row : countResult.iterateAll()) {
				bqIds.add(row.get("entity_id").getLongValue());
			}

			// Check which ones exist in Datastore
			List<Key<UserAnswer>> keys = new ArrayList<>();
			for (Long id : bqIds) {
				keys.add(Key.create(UserAnswer.class, id));
			}
			Map<Key<UserAnswer>, UserAnswer> existing = ofy().load().keys(keys);

			int alreadyExist = existing.size();
			int wouldRestore = bqIds.size() - alreadyExist;

			result.put("bigQueryCount", bqIds.size());
			result.put("alreadyInDatastore", alreadyExist);
			result.put("wouldRestore", wouldRestore);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.put("error", "Interrupted: " + e.getMessage());
		} catch (Exception e) {
			result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		return result;
	}

	/**
	 * Restore UserAnswer entities from a BigQuery table back to Datastore.
	 * Only restores entities that are missing from Datastore (skip existing).
	 * Uses the original entity IDs from the __key__.id column.
	 *
	 * The BigQuery table should be a raw Datastore export with columns matching
	 * the UserAnswer entity fields.
	 *
	 * Params:
	 *   dataset  - BigQuery dataset (e.g., "test")
	 *   table    - BigQuery table (e.g., "UserAnswer_2025MAR20")
	 *   date     - date to restore in yyyy-MM-dd format (e.g., "2021-01-15")
	 *   dryRun   - if true, only count what would be restored (default: true)
	 *
	 * Example: /tasks/restoreFromBigQuery?dataset=test&table=UserAnswer_2025MAR20&date=2021-01-15&dryRun=false
	 */
	@GetMapping("/tasks/restoreFromBigQuery")
	public Map<String, Object> restoreFromBigQuery(
			@RequestParam String dataset,
			@RequestParam String table,
			@RequestParam String date,
			@RequestParam(required = false, defaultValue = "true") boolean dryRun) {

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("dataset", dataset);
		result.put("table", table);
		result.put("date", date);
		result.put("dryRun", dryRun);

		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

			// Query all rows for the given date
			String sql = String.format(
					"SELECT __key__.id as entity_id, * FROM `%s.%s.%s` WHERE DATE(date) = '%s'",
					projectId, dataset, table, date);

			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(queryConfig);

			int totalInBigQuery = 0;
			int alreadyExist = 0;
			int restored = 0;
			int errors = 0;
			List<UserAnswer> batch = new ArrayList<>();

			for (FieldValueList row : tableResult.iterateAll()) {
				totalInBigQuery++;

				long entityId = row.get("entity_id").getLongValue();

				// Check if entity already exists in Datastore
				UserAnswer existing = ofy().load().type(UserAnswer.class).id(entityId).now();
				if (existing != null) {
					alreadyExist++;
					continue;
				}

				if (dryRun) {
					restored++; // count what would be restored
					continue;
				}

				// Build UserAnswer from BigQuery row
				try {
					UserAnswer ua = new UserAnswer();
					ua.setId(entityId);

					// Parse date
					if (!row.get("date").isNull()) {
						String dateStr = row.get("date").getStringValue();
						// Datastore export timestamps can be in various formats
						ua.setDate(parseTimestamp(dateStr));
					}

					ua.setSurveyId(getStringOrNull(row, "surveyId"));
					ua.setHitId(getStringOrNull(row, "hitId"));
					ua.setWorkerId(getStringOrNull(row, "workerId"));
					ua.setIp(getStringOrNull(row, "ip"));
					ua.setLocationCountry(getStringOrNull(row, "locationCountry"));
					ua.setLocationRegion(getStringOrNull(row, "locationRegion"));
					ua.setLocationCity(getStringOrNull(row, "locationCity"));

					if (!row.get("hitCreationDate").isNull()) {
						ua.setHitCreationDate(parseTimestamp(row.get("hitCreationDate").getStringValue()));
					}

					// Parse answers map — Datastore exports nested maps as repeated RECORD
					Map<String, String> answers = parseAnswersFromRow(row);
					if (answers != null && !answers.isEmpty()) {
						ua.setAnswers(answers);
					}

					batch.add(ua);

					if (batch.size() >= 25) {
						ofy().save().entities(batch).now();
						restored += batch.size();
						batch.clear();
					}
				} catch (Exception e) {
					errors++;
					logger.log(Level.WARNING, "Error restoring entity " + entityId + ": " + e.getMessage(), e);
				}
			}

			// Save remaining batch
			if (!batch.isEmpty()) {
				ofy().save().entities(batch).now();
				restored += batch.size();
			}

			result.put("totalInBigQuery", totalInBigQuery);
			result.put("alreadyInDatastore", alreadyExist);
			result.put(dryRun ? "wouldRestore" : "restored", restored);
			if (errors > 0) {
				result.put("errors", errors);
			}
			result.put("status", "ok");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.put("error", "Interrupted: " + e.getMessage());
		} catch (Exception e) {
			result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
			logger.log(Level.SEVERE, "Restore failed", e);
		}

		return result;
	}

	/**
	 * Restore UserAnswer entities from BigQuery for a range of dates.
	 * Iterates day by day, restoring missing entities for each date.
	 *
	 * Example: /tasks/restoreRange?dataset=test&table=UserAnswer_2025MAR20&from=2021-01-01&to=2021-01-31&dryRun=false
	 */
	@GetMapping("/tasks/restoreRange")
	public Map<String, Object> restoreRange(
			@RequestParam String dataset,
			@RequestParam String table,
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam(required = false, defaultValue = "true") boolean dryRun) {

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("dataset", dataset);
		result.put("table", table);
		result.put("from", from);
		result.put("to", to);
		result.put("dryRun", dryRun);

		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

			// Query all rows for the date range
			String sql = String.format(
					"SELECT __key__.id as entity_id, * FROM `%s.%s.%s` WHERE DATE(date) >= '%s' AND DATE(date) <= '%s' ORDER BY date",
					projectId, dataset, table, from, to);

			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(queryConfig);

			int totalInBigQuery = 0;
			int alreadyExist = 0;
			int restored = 0;
			int errors = 0;

			// Collect all entity IDs first to batch-check existence
			List<FieldValueList> allRows = new ArrayList<>();
			for (FieldValueList row : tableResult.iterateAll()) {
				allRows.add(row);
			}
			totalInBigQuery = allRows.size();

			// Batch-check existence in groups of 100
			List<UserAnswer> saveBatch = new ArrayList<>();
			for (int i = 0; i < allRows.size(); i += 100) {
				int end = Math.min(i + 100, allRows.size());
				List<FieldValueList> chunk = allRows.subList(i, end);

				// Batch-load existing entities
				List<Key<UserAnswer>> keys = new ArrayList<>();
				for (FieldValueList row : chunk) {
					long entityId = row.get("entity_id").getLongValue();
					keys.add(Key.create(UserAnswer.class, entityId));
				}
				Map<Key<UserAnswer>, UserAnswer> existingMap = ofy().load().keys(keys);

				for (FieldValueList row : chunk) {
					long entityId = row.get("entity_id").getLongValue();
					Key<UserAnswer> key = Key.create(UserAnswer.class, entityId);

					if (existingMap.containsKey(key)) {
						alreadyExist++;
						continue;
					}

					if (dryRun) {
						restored++;
						continue;
					}

					try {
						UserAnswer ua = buildUserAnswerFromRow(row, entityId);
						saveBatch.add(ua);

						if (saveBatch.size() >= 25) {
							ofy().save().entities(saveBatch).now();
							restored += saveBatch.size();
							saveBatch.clear();
						}
					} catch (Exception e) {
						errors++;
						logger.log(Level.WARNING, "Error restoring entity " + entityId + ": " + e.getMessage(), e);
					}
				}
			}

			// Save remaining batch
			if (!saveBatch.isEmpty()) {
				ofy().save().entities(saveBatch).now();
				restored += saveBatch.size();
			}

			result.put("totalInBigQuery", totalInBigQuery);
			result.put("alreadyInDatastore", alreadyExist);
			result.put(dryRun ? "wouldRestore" : "restored", restored);
			if (errors > 0) {
				result.put("errors", errors);
			}
			result.put("status", "ok");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.put("error", "Interrupted: " + e.getMessage());
		} catch (Exception e) {
			result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
			logger.log(Level.SEVERE, "Restore range failed", e);
		}

		return result;
	}

	private UserAnswer buildUserAnswerFromRow(FieldValueList row, long entityId) {
		UserAnswer ua = new UserAnswer();
		ua.setId(entityId);

		if (!row.get("date").isNull()) {
			ua.setDate(parseTimestamp(row.get("date").getStringValue()));
		}

		ua.setSurveyId(getStringOrNull(row, "surveyId"));
		ua.setHitId(getStringOrNull(row, "hitId"));
		ua.setWorkerId(getStringOrNull(row, "workerId"));
		ua.setIp(getStringOrNull(row, "ip"));
		ua.setLocationCountry(getStringOrNull(row, "locationCountry"));
		ua.setLocationRegion(getStringOrNull(row, "locationRegion"));
		ua.setLocationCity(getStringOrNull(row, "locationCity"));

		try {
			if (!row.get("hitCreationDate").isNull()) {
				ua.setHitCreationDate(parseTimestamp(row.get("hitCreationDate").getStringValue()));
			}
		} catch (Exception e) {
			// hitCreationDate may not exist in all exports
		}

		Map<String, String> answers = parseAnswersFromRow(row);
		if (answers != null && !answers.isEmpty()) {
			ua.setAnswers(answers);
		}

		return ua;
	}

	private String getStringOrNull(FieldValueList row, String fieldName) {
		try {
			FieldValue val = row.get(fieldName);
			return val.isNull() ? null : val.getStringValue();
		} catch (Exception e) {
			return null;
		}
	}

	private Date parseTimestamp(String timestamp) {
		// Handle various timestamp formats from BigQuery/Datastore exports
		// Try epoch micros first (Datastore export format)
		try {
			long micros = Long.parseLong(timestamp);
			return new Date(micros / 1000);
		} catch (NumberFormatException e) {
			// Not numeric, try ISO format
		}

		// Try ISO format: "2021-01-15T12:34:56Z" or "2021-01-15 12:34:56 UTC"
		String[] patterns = {
				"yyyy-MM-dd'T'HH:mm:ss'Z'",
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				"yyyy-MM-dd HH:mm:ss z",
				"yyyy-MM-dd HH:mm:ss.SSS z",
				"yyyy-MM-dd HH:mm:ss",
				"E MMM dd HH:mm:ss z yyyy"
		};
		for (String pattern : patterns) {
			try {
				DateFormat df = SafeDateFormat.forPattern(pattern);
				return df.parse(timestamp);
			} catch (ParseException e) {
				// try next
			}
		}

		throw new RuntimeException("Cannot parse timestamp: " + timestamp);
	}

	private Map<String, String> parseAnswersFromRow(FieldValueList row) {
		Map<String, String> answers = new LinkedHashMap<>();

		// Try to read the "answers" field directly (Datastore export as nested RECORD)
		try {
			FieldValue answersField = row.get("answers");
			if (!answersField.isNull()) {
				if (answersField.getAttribute() == FieldValue.Attribute.RECORD) {
					// Nested record — iterate sub-fields
					FieldValueList record = answersField.getRecordValue();
					for (int i = 0; i < record.size(); i++) {
						// Datastore map exports as repeated {key, value} records
						FieldValue entry = record.get(i);
						if (entry.getAttribute() == FieldValue.Attribute.RECORD) {
							FieldValueList kv = entry.getRecordValue();
							String key = kv.get("key").isNull() ? null : kv.get("key").getStringValue();
							String value = kv.get("value").isNull() ? null : kv.get("value").getStringValue();
							if (key != null) {
								answers.put(key, value);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// Field might not exist or have different structure
		}

		// If answers map is still empty, try reading individual answer columns
		// (some exports flatten the map into top-level columns)
		if (answers.isEmpty()) {
			String[] answerKeys = {"yearOfBirth", "gender", "maritalStatus", "householdSize",
					"householdIncome", "educationalLevel", "timeSpentOnMturk",
					"weeklyIncomeFromMturk", "languagesSpoken"};
			for (String key : answerKeys) {
				String value = getStringOrNull(row, key);
				if (value != null) {
					answers.put(key, value);
				}
			}
		}

		return answers;
	}
}
