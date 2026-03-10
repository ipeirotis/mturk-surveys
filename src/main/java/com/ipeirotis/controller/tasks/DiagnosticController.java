package com.ipeirotis.controller.tasks;

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
	 */
	@GetMapping("/tasks/diagnoseVolume")
	public Map<String, Object> diagnoseVolume(@RequestParam String date) throws ParseException {
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

		// Strategy 1: date-only filter (single-property index on date)
		{
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("date >=", from)
					.filter("date <", to);
			List<UserAnswer> answers = q.list();
			int total = answers.size();
			int withSurveyId = 0;
			int withDemographicsSurveyId = 0;
			int nullSurveyId = 0;
			Set<String> surveyIds = new TreeSet<>();
			for (UserAnswer ua : answers) {
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
			s1.put("description", "date-only filter (single-property date index)");
			s1.put("totalCount", total);
			s1.put("withDemographicsSurveyId", withDemographicsSurveyId);
			s1.put("nullSurveyId", nullSurveyId);
			s1.put("distinctSurveyIds", surveyIds);
			result.put("strategy1_dateOnly", s1);
		}

		// Strategy 2: surveyId=demographics + date filter (composite index)
		{
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("surveyId", "demographics")
					.filter("date >=", from)
					.filter("date <", to)
					.order("date");
			List<UserAnswer> answers = q.list();
			Map<String, Object> s2 = new LinkedHashMap<>();
			s2.put("description", "surveyId=demographics + date filter (composite index)");
			s2.put("totalCount", answers.size());
			result.put("strategy2_compositeIndex", s2);
		}

		// Strategy 3: surveyId=demographics only, filter dates in Java (bypasses date index)
		{
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("surveyId", "demographics");
			int totalForSurvey = 0;
			int inDateRange = 0;
			for (UserAnswer ua : q) {
				totalForSurvey++;
				if (ua.getDate() != null && !ua.getDate().before(from) && ua.getDate().before(to)) {
					inDateRange++;
				}
				// Limit to avoid timeout - if we've already found more than enough
				// evidence, we can stop
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
		}

		// Strategy 4: keys-only count with date filter
		{
			Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
					.filter("date >=", from)
					.filter("date <", to);
			int keysCount = q.keys().list().size();
			Map<String, Object> s4 = new LinkedHashMap<>();
			s4.put("description", "keys-only query with date filter");
			s4.put("keysCount", keysCount);
			result.put("strategy4_keysOnlyDate", s4);
		}

		// Strategy 5: keys-only count with composite filter
		{
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

		// Query all answers in the range using the composite index
		Query<UserAnswer> q = ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.filter("date >=", start.getTime())
				.filter("date <", end.getTime())
				.order("date");

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

		// Also run without surveyId filter for comparison
		Query<UserAnswer> qNoSurvey = ofy().load().type(UserAnswer.class)
				.filter("date >=", start.getTime())
				.filter("date <", end.getTime());

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
}
