package com.ipeirotis.service;

import com.google.cloud.bigquery.*;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to compare Datastore vs BigQuery backup counts and restore
 * missing entries from the BigQuery backup table (test.UserAnswer_2025MAR20).
 */
@Service
public class DatastoreRestoreService {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreRestoreService.class);

	private static final String BQ_BACKUP_DATASET = "test";
	private static final String BQ_BACKUP_TABLE = "UserAnswer_2025MAR20";
	private static final String DEFAULT_QUALIFIED_TABLE = BQ_BACKUP_DATASET + "." + BQ_BACKUP_TABLE;

	@Autowired
	private UserAnswerService userAnswerService;

	/**
	 * Compare daily counts between Datastore and BigQuery backup for a date range.
	 * Returns a list of maps with {date, datastoreCount, bigqueryCount, delta}.
	 * Only includes days where delta != 0 (i.e., counts differ).
	 */
	public List<Map<String, Object>> compareCounts(String fromStr, String toStr) throws ParseException {
		return compareCounts(fromStr, toStr, null);
	}

	public List<Map<String, Object>> compareCounts(String fromStr, String toStr, String table) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");
		String qualifiedTable = resolveTable(table);

		// 1. Get BigQuery daily counts in one query
		Map<String, Long> bqCounts = getBigQueryDailyCounts(fromStr, toStr, qualifiedTable);

		// 2. Get Datastore daily counts day by day
		Calendar start = Calendar.getInstance();
		start.setTime(df.parse(fromStr));
		CalendarUtils.truncateToDay(start);

		Calendar end = Calendar.getInstance();
		end.setTime(df.parse(toStr));
		CalendarUtils.truncateToDay(end);

		List<Map<String, Object>> results = new ArrayList<>();

		Calendar current = (Calendar) start.clone();
		while (!current.after(end)) {
			String dateKey = df.format(current.getTime());

			Calendar dayEnd = (Calendar) current.clone();
			dayEnd.add(Calendar.DAY_OF_MONTH, 1);

			// Count Datastore entries for this day (no surveyId filter to include legacy entries)
			int dsCount = countDatastoreEntries(current.getTime(), dayEnd.getTime());
			long bqCount = bqCounts.getOrDefault(dateKey, 0L);

			long delta = bqCount - dsCount;
			if (delta != 0) {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("date", dateKey);
				row.put("datastoreCount", dsCount);
				row.put("bigqueryCount", bqCount);
				row.put("delta", delta);
				results.add(row);
			}

			current.add(Calendar.DAY_OF_MONTH, 1);
		}

		return results;
	}

	/**
	 * Restore all entries from BigQuery backup for a given date into Datastore.
	 * Uses the original Datastore entity IDs from the __key__.id field.
	 * Existing entities with the same ID will be overwritten.
	 *
	 * @param dateStr date in yyyy-MM-dd format
	 * @return number of entities restored
	 */
	public int restoreDate(String dateStr) {
		return restoreDate(dateStr, null);
	}

	public int restoreDate(String dateStr, String table) {
		String qualifiedTable = resolveTable(table);
		List<UserAnswer> entities = loadFullEntitiesFromBackup(dateStr, qualifiedTable);
		if (entities.isEmpty()) {
			logger.info("No entries in BigQuery backup for " + dateStr + ", nothing to restore");
			return 0;
		}

		// Build set of existing (workerId|hitId) keys in Datastore for this day
		// to avoid creating duplicates when restoring with auto-generated IDs
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");
		Set<String> existingKeys = new HashSet<>();
		try {
			Calendar dayStart = Calendar.getInstance();
			dayStart.setTime(df.parse(dateStr));
			CalendarUtils.truncateToDay(dayStart);
			Calendar dayEnd = (Calendar) dayStart.clone();
			dayEnd.add(Calendar.DAY_OF_MONTH, 1);

			List<UserAnswer> existing = ofy().load().type(UserAnswer.class)
					.filter("surveyId", "demographics")
					.filter("date >=", dayStart.getTime())
					.filter("date <", dayEnd.getTime())
					.list();
			for (UserAnswer ua : existing) {
				String key = (ua.getWorkerId() != null ? ua.getWorkerId() : "")
						+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
				existingKeys.add(key);
			}
			logger.info("Found " + existingKeys.size() + " existing entries in Datastore for " + dateStr);
		} catch (ParseException e) {
			logger.warn("Failed to parse date for existing-check: " + dateStr, e);
		}

		// Filter out entries that already exist in Datastore
		List<UserAnswer> toRestore = new ArrayList<>();
		for (UserAnswer ua : entities) {
			String key = (ua.getWorkerId() != null ? ua.getWorkerId() : "")
					+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
			if (!existingKeys.contains(key)) {
				toRestore.add(ua);
			}
		}
		int skipped = entities.size() - toRestore.size();
		if (skipped > 0) {
			logger.info("Skipped " + skipped + " entries already in Datastore for " + dateStr);
		}
		if (toRestore.isEmpty()) {
			logger.info("All " + entities.size() + " entries already exist in Datastore for " + dateStr);
			return 0;
		}

		// Save in batches of 250 (Datastore limit is 500 per batch, but use smaller batches for safety)
		int batchSize = 250;
		int totalSaved = 0;
		for (int i = 0; i < toRestore.size(); i += batchSize) {
			int end = Math.min(i + batchSize, toRestore.size());
			List<UserAnswer> batch = toRestore.subList(i, end);
			ofy().save().entities(batch).now();
			totalSaved += batch.size();
		}

		logger.info("Restored " + totalSaved + " entries from BigQuery backup for " + dateStr
				+ " (skipped " + skipped + " existing)");
		return totalSaved;
	}

	/**
	 * Parse one row from BigQuery backup and return diagnostic info (does NOT write to Datastore).
	 */
	public Map<String, Object> testParseOneRow(String dateStr, String table) {
		String qualifiedTable = resolveTable(table);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("date", dateStr);
		result.put("table", qualifiedTable);
		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();
			String sql = String.format(
					"SELECT * FROM `%s.%s` WHERE DATE(date) = '%s' AND surveyId = 'demographics' LIMIT 1",
					projectId, qualifiedTable, dateStr);
			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(queryConfig);
			Schema tableSchema = tableResult.getSchema();

			// Schema info for the answers field
			try {
				com.google.cloud.bigquery.Field answersSchemaField = tableSchema.getFields().get("answers");
				result.put("answersSchemaField_type", answersSchemaField.getType().name());
				result.put("answersSchemaField_mode", answersSchemaField.getMode() != null ? answersSchemaField.getMode().name() : "null");
				if (answersSchemaField.getSubFields() != null) {
					List<String> subFieldNames = new ArrayList<>();
					for (com.google.cloud.bigquery.Field sf : answersSchemaField.getSubFields()) {
						subFieldNames.add(sf.getName() + ":" + sf.getType().name());
					}
					result.put("answersSchemaField_subFields", subFieldNames);
				} else {
					result.put("answersSchemaField_subFields", "NULL");
				}
			} catch (Exception e) {
				result.put("answersSchemaField_error", e.getClass().getName() + ": " + e.getMessage());
			}

			for (FieldValueList row : tableResult.iterateAll()) {
				// Test parseAnswersRecord
				Map<String, String> parsedAnswers = parseAnswersRecord(row, tableSchema);
				result.put("parsedAnswers", parsedAnswers);
				result.put("parsedAnswersSize", parsedAnswers.size());

				// Also show raw answers field info
				try {
					FieldValue af = row.get("answers");
					result.put("rawAnswers_isNull", af.isNull());
					result.put("rawAnswers_attribute", af.getAttribute().name());
					if (!af.isNull() && af.getAttribute() == FieldValue.Attribute.RECORD) {
						FieldValueList rec = af.getRecordValue();
						result.put("rawAnswers_recordSize", rec.size());

						// Test named access
						Map<String, String> namedAccess = new LinkedHashMap<>();
						for (String fn : ANSWER_FIELDS) {
							try {
								FieldValue v = rec.get(fn);
								namedAccess.put(fn, v.isNull() ? "NULL" : v.getStringValue());
							} catch (Exception e) {
								namedAccess.put(fn, "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
							}
						}
						result.put("namedFieldAccess", namedAccess);
					}
				} catch (Exception e) {
					result.put("rawAnswers_error", e.getClass().getName() + ": " + e.getMessage());
				}
				break;
			}
		} catch (Exception e) {
			result.put("error", e.getClass().getName() + ": " + e.getMessage());
		}
		return result;
	}

	/**
	 * Get BigQuery daily counts for the backup table in one query.
	 */
	private String resolveTable(String table) {
		if (table != null && !table.isBlank()) {
			// Allow "dataset.table" or just "table" (defaults to test dataset)
			return table.contains(".") ? table : BQ_BACKUP_DATASET + "." + table;
		}
		return DEFAULT_QUALIFIED_TABLE;
	}

	private Map<String, Long> getBigQueryDailyCounts(String fromDate, String toDate, String qualifiedTable) {
		Map<String, Long> counts = new LinkedHashMap<>();
		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

			String sql = String.format(
					"SELECT FORMAT_DATE('%%Y-%%m-%%d', DATE(date)) AS day, COUNT(*) AS cnt "
					+ "FROM `%s.%s` "
					+ "WHERE DATE(date) >= '%s' AND DATE(date) <= '%s' "
					+ "AND surveyId = 'demographics' "
					+ "GROUP BY day ORDER BY day",
					projectId, qualifiedTable, fromDate, toDate);

			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult result = bigQuery.query(queryConfig);

			for (FieldValueList row : result.iterateAll()) {
				String day = row.get("day").getStringValue();
				long cnt = row.get("cnt").getLongValue();
				counts.put(day, cnt);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted querying BigQuery counts", e);
		} catch (Exception e) {
			logger.warn("Failed to query BigQuery counts: " + e.getMessage(), e);
		}
		return counts;
	}

	/**
	 * Count Datastore UserAnswer entries for a day.
	 * Uses the composite index (surveyId, date) which is reliable,
	 * rather than the built-in single-property date index which undercounts.
	 */
	private int countDatastoreEntries(Date from, Date to) {
		return ofy().load().type(UserAnswer.class)
				.filter("surveyId", "demographics")
				.filter("date >=", from)
				.filter("date <", to)
				.count();
	}

	/**
	 * Load full UserAnswer entities from the BigQuery backup table for a given date.
	 * Uses SELECT * to avoid failures from unknown column names, then extracts
	 * fields defensively. Entities get new Datastore IDs on save.
	 */
	private List<UserAnswer> loadFullEntitiesFromBackup(String sortableDate, String qualifiedTable) {
		List<UserAnswer> results = new ArrayList<>();
		try {
			BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
			String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

			// Deduplicate by (workerId, hitId), keeping the earliest entry per pair.
			// Filter to demographics surveyId only.
			String sql = String.format(
					"SELECT * EXCEPT(row_num) FROM ("
					+ "  SELECT *, ROW_NUMBER() OVER ("
					+ "    PARTITION BY workerId, hitId ORDER BY date ASC"
					+ "  ) AS row_num"
					+ "  FROM `%s.%s`"
					+ "  WHERE DATE(date) = '%s' AND surveyId = 'demographics'"
					+ ") WHERE row_num = 1",
					projectId, qualifiedTable, sortableDate);

			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
			TableResult tableResult = bigQuery.query(queryConfig);

			// Build set of available column names from schema
			Schema tableSchema = tableResult.getSchema();
			Set<String> columns = new HashSet<>();
			for (com.google.cloud.bigquery.Field field : tableSchema.getFields()) {
				columns.add(field.getName());
			}
			logger.info("Backup table columns for " + sortableDate + ": " + columns);

			for (FieldValueList row : tableResult.iterateAll()) {
				UserAnswer ua = new UserAnswer();

				// Do NOT restore original Datastore entity IDs — auto-generated IDs get
				// reused over time, so restoring old IDs would overwrite newer entries.
				// Let Objectify auto-allocate fresh IDs instead.

				ua.setDate(parseTimestamp(row, "date"));
				ua.setHitId(getStringOrNull(row, "hitId"));
				ua.setHitCreationDate(parseTimestampOrNull(row, "hitCreationDate"));
				ua.setSurveyId(getStringOrNull(row, "surveyId"));
				ua.setWorkerId(getStringOrNull(row, "workerId"));
				ua.setIp(getStringOrNull(row, "ip"));
				ua.setLocationCountry(getStringOrNull(row, "locationCountry"));
				ua.setLocationRegion(getStringOrNull(row, "locationRegion"));
				ua.setLocationCity(getStringOrNull(row, "locationCity"));

				// Parse the old 'answer' field (deprecated string format)
				ua.setAnswer(getStringOrNull(row, "answer"));

				// Parse the 'answers' map (new format)
				Map<String, String> answers = parseAnswersRecord(row, tableSchema);
				if (!answers.isEmpty()) {
					ua.setAnswers(answers);
				}

				results.add(ua);
			}

			long withAnswers = results.stream().filter(ua -> ua.getAnswers() != null && !ua.getAnswers().isEmpty()).count();
		logger.info("Loaded " + results.size() + " full entities from backup for " + sortableDate
				+ " (" + withAnswers + " with answers)");
		if (withAnswers == 0 && !results.isEmpty()) {
			logger.error("WARNING: Loaded " + results.size() + " entities but NONE have answers — parsing may be broken!");
		}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted querying BigQuery backup for " + sortableDate, e);
		} catch (Exception e) {
			logger.warn("Failed to query BigQuery backup for " + sortableDate + ": " + e.getMessage(), e);
		}
		return results;
	}

	/** Known answer field names in the Datastore export RECORD. */
	private static final String[] ANSWER_FIELDS = {
		"gender", "householdSize", "languagesSpoken", "timeSpentOnMturk",
		"weeklyIncomeFromMturk", "householdIncome", "maritalStatus",
		"yearOfBirth", "educationalLevel"
	};

	/**
	 * Parse the 'answers' field from a Datastore export row.
	 * The backup table stores answers as a flat RECORD with named sub-fields
	 * (e.g., answers.gender, answers.householdSize).
	 * Uses named field access for robustness against column ordering differences.
	 */
	private Map<String, String> parseAnswersRecord(FieldValueList row, Schema tableSchema) {
		Map<String, String> answers = new LinkedHashMap<>();
		try {
			FieldValue answersField = row.get("answers");
			if (answersField.isNull()) {
				return answers;
			}
			if (answersField.getAttribute() != FieldValue.Attribute.RECORD) {
				logger.warn("answers field is not RECORD, attribute=" + answersField.getAttribute());
				return answers;
			}
			FieldValueList record = answersField.getRecordValue();

			// Use named access for each known answer field
			for (String fieldName : ANSWER_FIELDS) {
				try {
					FieldValue val = record.get(fieldName);
					if (val != null && !val.isNull()) {
						answers.put(fieldName, val.getStringValue());
					}
				} catch (IllegalArgumentException e) {
					// Field doesn't exist in this record — skip
				} catch (Exception e) {
					logger.warn("Failed to parse answer field '" + fieldName + "': " + e.getMessage());
				}
			}
		} catch (IllegalArgumentException e) {
			// 'answers' column doesn't exist in this table
			logger.info("No 'answers' column in table: " + e.getMessage());
		} catch (Exception e) {
			logger.warn("Failed to parse answers record: " + e.getClass().getName() + ": " + e.getMessage());
		}
		return answers;
	}

	private Date parseTimestamp(FieldValueList row, String fieldName) {
		try {
			FieldValue val = row.get(fieldName);
			if (val.isNull()) return new Date();
			String dateVal = val.getStringValue();

			// Try epoch micros first (Datastore export format)
			try {
				long micros = Long.parseLong(dateVal);
				return new Date(micros / 1000);
			} catch (NumberFormatException e) {
				try {
					double epochSeconds = Double.parseDouble(dateVal);
					return new Date((long) (epochSeconds * 1000));
				} catch (NumberFormatException e2) {
					// Not numeric
				}
			}

			// Try ISO formats
			String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
					"yyyy-MM-dd HH:mm:ss z", "yyyy-MM-dd HH:mm:ss"};
			for (String pattern : patterns) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(pattern);
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					return sdf.parse(dateVal);
				} catch (ParseException ignored) {
				}
			}
		} catch (Exception e) {
			// Field might not exist
		}
		return new Date();
	}

	private Date parseTimestampOrNull(FieldValueList row, String fieldName) {
		try {
			FieldValue val = row.get(fieldName);
			if (val.isNull()) return null;
			return parseTimestamp(row, fieldName);
		} catch (Exception e) {
			return null;
		}
	}

	private String getStringOrNull(FieldValueList row, String fieldName) {
		try {
			FieldValue val = row.get(fieldName);
			return val.isNull() ? null : val.getStringValue();
		} catch (Exception e) {
			return null;
		}
	}
}
