package com.ipeirotis.controller.tasks.debug;

import com.google.cloud.bigquery.*;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.DatastoreRestoreService;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Debug endpoints for diagnosing Datastore and BigQuery data issues.
 * These are NOT called by cron — they exist for manual debugging only.
 * Disabled by default; set DEBUG_TASKS_ENABLED=true to enable.
 */
@RestController
@ConditionalOnProperty(name = "debug.tasks.enabled", havingValue = "true", matchIfMissing = false)
public class DebugDatastoreController {

	@Autowired
	private DatastoreRestoreService restoreService;

	/**
	 * Diagnostic endpoint to debug Datastore counting discrepancies.
	 * Tests multiple counting approaches for a single date.
	 *
	 * Example: /tasks/debug/count?date=2016-04-15
	 */
	@GetMapping("/tasks/debug/count")
	public Map<String, Object> debugCount(@RequestParam String date) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");

		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse(date));
		CalendarUtils.truncateToDay(cal);
		Date from = cal.getTime();

		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date to = cal.getTime();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("date", date);
		result.put("fromEpochMs", from.getTime());
		result.put("toEpochMs", to.getTime());
		result.put("fromUtc", from.toString());
		result.put("toUtc", to.toString());
		result.put("jvmTimezone", TimeZone.getDefault().getID());

		// Method 1: .count() without surveyId (what compare uses)
		int countNoSurvey = ofy().load().type(UserAnswer.class)
				.filter("date >=", from)
				.filter("date <", to)
				.count();
		result.put("countNoSurveyId", countNoSurvey);

		// Method 2: .count() with surveyId = "demographics"
		int countWithSurvey = ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.filter("date >=", from)
				.filter("date <", to)
				.count();
		result.put("countWithSurveyId", countWithSurvey);

		// Method 3: keys-only query, iterate and count (no surveyId)
		int keysCount = 0;
		for (@SuppressWarnings("unused") com.googlecode.objectify.Key<UserAnswer> key :
				ofy().load().type(UserAnswer.class)
						.filter("date >=", from)
						.filter("date <", to)
						.keys()) {
			keysCount++;
		}
		result.put("keysIterateNoSurveyId", keysCount);

		// Method 4: keys-only query with surveyId
		int keysCountWithSurvey = 0;
		for (@SuppressWarnings("unused") com.googlecode.objectify.Key<UserAnswer> key :
				ofy().load().type(UserAnswer.class)
						.filter("surveyId", "demographics")
						.filter("date >=", from)
						.filter("date <", to)
						.keys()) {
			keysCountWithSurvey++;
		}
		result.put("keysIterateWithSurveyId", keysCountWithSurvey);

		// Method 5: full entity load with surveyId, count
		List<UserAnswer> entities = ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.filter("date >=", from)
				.filter("date <", to)
				.list();
		result.put("fullLoadWithSurveyId", entities.size());

		return result;
	}

	/**
	 * Diagnostic: dump raw BigQuery schema and sample answers field for a date.
	 * Example: /tasks/debug/bigQueryAnswers?date=2021-06-15
	 */
	@GetMapping("/tasks/debug/bigQueryAnswers")
	public Map<String, Object> debugBigQueryAnswers(
			@RequestParam String date,
			@RequestParam(required = false) String table) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("date", date);
		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();
			String qualifiedTable = (table != null && !table.isBlank())
					? (table.contains(".") ? table : "test." + table)
					: "test.UserAnswer_2025MAR20";
			result.put("table", qualifiedTable);

			// Query one row
			String sql = String.format(
					"SELECT * FROM `%s.%s` WHERE DATE(date) = '%s' LIMIT 1",
					projectId, qualifiedTable, date);
			QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(config);

			// Schema
			List<Map<String, String>> schema = new ArrayList<>();
			for (com.google.cloud.bigquery.Field field : tableResult.getSchema().getFields()) {
				Map<String, String> f = new LinkedHashMap<>();
				f.put("name", field.getName());
				f.put("type", field.getType().name());
				f.put("mode", field.getMode() != null ? field.getMode().name() : "NULLABLE");
				if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
					StringBuilder sub = new StringBuilder();
					for (com.google.cloud.bigquery.Field sf : field.getSubFields()) {
						sub.append(sf.getName()).append("(").append(sf.getType().name());
						if (sf.getMode() != null) sub.append(",").append(sf.getMode().name());
						if (sf.getSubFields() != null && !sf.getSubFields().isEmpty()) {
							sub.append(",[");
							for (com.google.cloud.bigquery.Field ssf : sf.getSubFields()) {
								sub.append(ssf.getName()).append(":").append(ssf.getType().name()).append(",");
							}
							sub.append("]");
						}
						sub.append(") ");
					}
					f.put("subFields", sub.toString().trim());
				}
				schema.add(f);
			}
			result.put("schema", schema);

			// Inspect the answers field from the first row
			for (FieldValueList row : tableResult.iterateAll()) {
				try {
					FieldValue answersField = row.get("answers");
					result.put("answersIsNull", answersField.isNull());
					if (!answersField.isNull()) {
						result.put("answersAttribute", answersField.getAttribute().name());

						if (answersField.getAttribute() == FieldValue.Attribute.RECORD) {
							FieldValueList record = answersField.getRecordValue();
							result.put("answersRecordSize", record.size());
							// Dump first 3 entries
							List<Map<String, Object>> entries = new ArrayList<>();
							for (int i = 0; i < Math.min(3, record.size()); i++) {
								FieldValue entry = record.get(i);
								Map<String, Object> e = new LinkedHashMap<>();
								e.put("attribute", entry.getAttribute().name());
								if (entry.getAttribute() == FieldValue.Attribute.RECORD) {
									FieldValueList kv = entry.getRecordValue();
									e.put("recordSize", kv.size());
									for (int j = 0; j < kv.size(); j++) {
										FieldValue fv = kv.get(j);
										e.put("field_" + j + "_attr", fv.getAttribute().name());
										e.put("field_" + j + "_val", fv.isNull() ? "NULL" : fv.getStringValue());
									}
								} else {
									e.put("value", entry.isNull() ? "NULL" : entry.toString());
								}
								entries.add(e);
							}
							result.put("answersSample", entries);
						} else if (answersField.getAttribute() == FieldValue.Attribute.REPEATED) {
							List<FieldValue> entries = answersField.getRepeatedValue();
							result.put("answersRepeatedSize", entries.size());
							List<Map<String, Object>> sample = new ArrayList<>();
							for (int i = 0; i < Math.min(3, entries.size()); i++) {
								FieldValue entry = entries.get(i);
								Map<String, Object> e = new LinkedHashMap<>();
								e.put("attribute", entry.getAttribute().name());
								if (entry.getAttribute() == FieldValue.Attribute.RECORD) {
									FieldValueList kv = entry.getRecordValue();
									e.put("recordSize", kv.size());
									for (int j = 0; j < kv.size(); j++) {
										FieldValue fv = kv.get(j);
										e.put("field_" + j + "_attr", fv.getAttribute().name());
										e.put("field_" + j + "_val", fv.isNull() ? "NULL" : fv.getStringValue());
									}
								}
								sample.add(e);
							}
							result.put("answersSample", sample);
						} else {
							result.put("answersRawValue", answersField.toString());
						}
					}
				} catch (Exception e) {
					result.put("answersError", e.getClass().getName() + ": " + e.getMessage());
				}

				// Also check the deprecated 'answer' field
				try {
					FieldValue answerField = row.get("answer");
					result.put("answerIsNull", answerField.isNull());
					if (!answerField.isNull()) {
						result.put("answerValue", answerField.getStringValue());
					}
				} catch (Exception e) {
					result.put("answerError", e.getClass().getName() + ": " + e.getMessage());
				}

				// Check surveyId
				try {
					FieldValue surveyIdField = row.get("surveyId");
					result.put("surveyIdIsNull", surveyIdField.isNull());
					if (!surveyIdField.isNull()) {
						result.put("surveyIdValue", surveyIdField.getStringValue());
					}
				} catch (Exception e) {
					result.put("surveyIdError", e.getClass().getName() + ": " + e.getMessage());
				}
				break; // Only inspect first row
			}
		} catch (Exception e) {
			result.put("error", e.getClass().getName() + ": " + e.getMessage());
		}
		return result;
	}

	/**
	 * Test: parse one BigQuery row and return diagnostic details (does NOT write to Datastore).
	 * Example: /tasks/debug/parseRow?date=2021-01-15&table=test.UserAnswer_2025MAR20
	 */
	@GetMapping("/tasks/debug/parseRow")
	public Map<String, Object> testParseRow(
			@RequestParam String date,
			@RequestParam(required = false) String table) {
		return restoreService.testParseOneRow(date, table);
	}
}
