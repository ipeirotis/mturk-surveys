package com.ipeirotis.service;

import com.google.cloud.bigquery.*;
import com.ipeirotis.entity.BigQueryExportLock;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.ExportInProgressException;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Service
public class BigQueryExportService {

	private static final Logger logger = LoggerFactory.getLogger(BigQueryExportService.class);

	private static final String DATASET_ID = "demographics";
	private static final String TABLE_ID = "responses";
	/** Longer than any single-day export takes; an abandoned lease frees itself after this. */
	private static final long LOCK_TTL_MS = 10 * 60 * 1000L;

	@Autowired
	private BigQuery bigQuery;

	@Autowired
	private SurveyService surveyService;

	/**
	 * Append a single day's Datastore answers to BigQuery. Append-only: rows already in
	 * demographics.responses (matched by hashed worker ID + HIT ID) are left untouched,
	 * and nothing is ever deleted, so Datastore cannot shrink the canonical table.
	 * @param dateStr date in MM/dd/yyyy format
	 * @return number of rows appended
	 */
	@Timed(value = "bigquery.export", description = "BigQuery export duration")
	public int exportDate(String dateStr) throws ParseException {
		String sortableDate = SafeDateFormat.forPattern("yyyy-MM-dd")
				.format(SafeDateFormat.forPattern("MM/dd/yyyy").parse(dateStr));
		String owner = acquireLock(sortableDate);
		try {
			return exportDateLocked(dateStr);
		} finally {
			releaseLock(sortableDate, owner);
		}
	}

	/**
	 * Take the leases for the export date and both neighbouring dates in one
	 * transaction. loadExistingPairs checks a window of date +/- 1 day, so exports of
	 * adjacent dates read overlapping windows; holding all three leases makes any two
	 * exports whose windows overlap run one after the other, while exports further
	 * apart still run in parallel.
	 * @return owner token to pass to releaseLock
	 * @throws ExportInProgressException if an export with an overlapping window holds a lease
	 */
	private String acquireLock(String sortableDate) {
		String owner = UUID.randomUUID().toString();
		List<String> dates = lockDates(sortableDate);
		boolean acquired = ofy().transact(() -> {
			Date now = new Date();
			Map<String, BigQueryExportLock> locks = ofy().load().type(BigQueryExportLock.class).ids(dates);
			for (BigQueryExportLock lock : locks.values()) {
				if (lock.isHeldAt(now)) {
					return false;
				}
			}
			Date expiresAt = new Date(now.getTime() + LOCK_TTL_MS);
			List<BigQueryExportLock> mine = new ArrayList<>();
			for (String d : dates) {
				mine.add(new BigQueryExportLock(d, owner, expiresAt));
			}
			ofy().save().entities(mine).now();
			return true;
		});
		if (!acquired) {
			throw new ExportInProgressException("A BigQuery export overlapping " + sortableDate
					+ " is already running; retry later");
		}
		return owner;
	}

	private void releaseLock(String sortableDate, String owner) {
		List<String> dates = lockDates(sortableDate);
		try {
			ofy().transact(() -> {
				List<BigQueryExportLock> mine = new ArrayList<>();
				for (BigQueryExportLock lock : ofy().load().type(BigQueryExportLock.class).ids(dates).values()) {
					if (owner.equals(lock.getOwner())) {
						mine.add(lock);
					}
				}
				ofy().delete().entities(mine).now();
			});
		} catch (RuntimeException e) {
			// The leases expire on their own after LOCK_TTL_MS.
			logger.warn("Failed to release BigQuery export locks around " + sortableDate + ": " + e.getMessage(), e);
		}
	}

	/** The export date and its neighbours, matching the window loadExistingPairs reads. */
	static List<String> lockDates(String sortableDate) {
		java.time.LocalDate day = java.time.LocalDate.parse(sortableDate);
		return List.of(day.minusDays(1).toString(), sortableDate, day.plusDays(1).toString());
	}

	private int exportDateLocked(String dateStr) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
		Calendar dateFrom = Calendar.getInstance();
		dateFrom.setTime(df.parse(dateStr));
		CalendarUtils.truncateToDay(dateFrom);

		Calendar dateTo = Calendar.getInstance();
		dateTo.setTime(dateFrom.getTime());
		dateTo.add(Calendar.DAY_OF_MONTH, 1);

		// Filter by surveyId to only export demographics entries
		List<UserAnswer> answers = surveyService.listAnswers("demographics", dateFrom.getTime(), dateTo.getTime());

		// Deduplicate: keep earliest response per (workerId, hitId)
		int originalCount = answers.size();
		Map<String, UserAnswer> seen = new LinkedHashMap<>();
		for (UserAnswer ua : answers) {
			String key = (ua.getWorkerId() != null ? ua.getWorkerId() : "")
					+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
			seen.putIfAbsent(key, ua);
		}
		answers = new ArrayList<>(seen.values());
		if (answers.size() < originalCount) {
			logger.info("Deduplicated " + (originalCount - answers.size())
					+ " duplicate entries for " + dateStr);
		}

		logger.info("BigQuery export for " + dateStr + ": " + answers.size() + " demographics entries");

		if (answers.isEmpty()) {
			logger.info("No responses found for " + dateStr + ", skipping BigQuery export");
			return 0;
		}

		String projectId = bigQuery.getOptions().getProjectId();

		ensureTableExists(bigQuery, projectId);

		String sortableDate = SafeDateFormat.forPattern("yyyy-MM-dd").format(dateFrom.getTime());
		String fullTable = String.format("`%s.%s.%s`", projectId, DATASET_ID, TABLE_ID);

		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Step 1: demographics.responses is the canonical archive, so never delete from it.
		// Only append (worker, HIT) pairs that are not already in the table for this date.
		Set<String> existing = loadExistingPairs(fullTable, sortableDate);
		int candidates = answers.size();
		List<UserAnswer> missing = new ArrayList<>();
		for (UserAnswer ua : answers) {
			if (!existing.contains(pairKey(ua))) {
				missing.add(ua);
			}
		}
		answers = missing;
		if (answers.isEmpty()) {
			logger.info("All " + candidates + " entries for " + dateStr + " already in BigQuery");
			return 0;
		}

		// Step 2: Insert rows in batches (non-transactional)
		int batchSize = 200;
		for (int i = 0; i < answers.size(); i += batchSize) {
			int end = Math.min(i + batchSize, answers.size());
			List<UserAnswer> batch = answers.subList(i, end);

			StringBuilder insertSql = new StringBuilder();
			insertSql.append(String.format("INSERT INTO %s "
					+ "(date, worker_id, survey_id, country, region, city, hit_id, "
					+ "hit_creation_date, ip_address, year_of_birth, gender, marital_status, "
					+ "household_size, household_income, educational_level, "
					+ "time_spent_on_mturk, weekly_income_from_mturk, languages_spoken) VALUES ",
					fullTable));

			for (int j = 0; j < batch.size(); j++) {
				UserAnswer ua = batch.get(j);
				if (j > 0) insertSql.append(", ");
				insertSql.append("(");

				// date
				insertSql.append(ua.getDate() != null
						? "TIMESTAMP('" + isoFormat.format(ua.getDate()) + "')" : "NULL");
				insertSql.append(", ");

				// worker_id (SHA256-hashed)
				String workerId = ua.getWorkerId();
				insertSql.append(workerId != null ? sqlString(sha256Hex(workerId)) : "NULL");
				insertSql.append(", ");

				// survey_id
				insertSql.append(sqlString(ua.getSurveyId()));
				insertSql.append(", ");

				// country, region, city
				insertSql.append(sqlString(ua.getLocationCountry())).append(", ");
				insertSql.append(sqlString(ua.getLocationRegion())).append(", ");
				insertSql.append(sqlString(ua.getLocationCity())).append(", ");

				// hit_id
				insertSql.append(sqlString(ua.getHitId())).append(", ");

				// hit_creation_date
				insertSql.append(ua.getHitCreationDate() != null
						? "TIMESTAMP('" + isoFormat.format(ua.getHitCreationDate()) + "')" : "NULL");
				insertSql.append(", ");

				// ip_address (SHA256-hashed)
				String ip = ua.getIp();
				insertSql.append(ip != null ? sqlString(sha256Hex(ip)) : "NULL");

				// answer fields
				Map<String, String> a = ua.getAnswers();
				String[] keys = {"yearOfBirth", "gender", "maritalStatus", "householdSize",
						"householdIncome", "educationalLevel", "timeSpentOnMturk",
						"weeklyIncomeFromMturk", "languagesSpoken"};
				for (String key : keys) {
					insertSql.append(", ");
					insertSql.append(a != null ? sqlString(a.get(key)) : "NULL");
				}

				insertSql.append(")");
			}

			try {
				bigQuery.query(QueryJobConfiguration.newBuilder(insertSql.toString())
						.setJobTimeoutMs(120_000L).build());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Interrupted during BigQuery insert", e);
			} catch (BigQueryException e) {
				if (e.getMessage() != null && e.getMessage().contains("concurrent")) {
					logger.warn("Concurrent update conflict during insert for " + dateStr + ", will retry later");
					throw new RuntimeException("BigQuery concurrent update conflict for " + dateStr, e);
				}
				throw e;
			}
		}

		int totalExported = answers.size();
		logger.info("Appended " + totalExported + " missing rows to BigQuery for " + dateStr
				+ " (" + (candidates - totalExported) + " already present)");
		return totalExported;
	}

	/**
	 * (hashed worker_id, hit_id) pairs already in the table around one UTC day. The
	 * neighbouring days are included because a row's timestamp in the table can differ
	 * slightly from Datastore's and land on the other side of midnight.
	 */
	private Set<String> loadExistingPairs(String fullTable, String sortableDate) {
		String sql = String.format(
				"SELECT worker_id, hit_id FROM %s WHERE DATE(date) BETWEEN "
				+ "DATE_SUB(DATE '%s', INTERVAL 1 DAY) AND DATE_ADD(DATE '%s', INTERVAL 1 DAY)",
				fullTable, sortableDate, sortableDate);
		Set<String> pairs = new HashSet<>();
		try {
			TableResult result = bigQuery.query(QueryJobConfiguration.newBuilder(sql)
					.setJobTimeoutMs(60_000L).build());
			for (FieldValueList row : result.iterateAll()) {
				FieldValue w = row.get("worker_id");
				FieldValue h = row.get("hit_id");
				pairs.add((w.isNull() ? "" : w.getStringValue()) + "|" + (h.isNull() ? "" : h.getStringValue()));
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted reading existing BigQuery rows", e);
		}
		return pairs;
	}

	static String pairKey(UserAnswer ua) {
		return (ua.getWorkerId() != null ? sha256Hex(ua.getWorkerId()) : "")
				+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
	}

	/**
	 * Remove duplicate rows from the BigQuery demographics.responses table.
	 * Keeps the earliest response per (worker_id, hit_id) pair.
	 * @return map with beforeCount, afterCount, duplicatesRemoved
	 */
	public Map<String, Object> deduplicateTable() {
		String projectId = bigQuery.getOptions().getProjectId();
		String fullTable = String.format("`%s.%s.%s`", projectId, DATASET_ID, TABLE_ID);

		Map<String, Object> result = new LinkedHashMap<>();

		try {
			// Count rows before dedup
			String countSql = "SELECT COUNT(*) AS cnt FROM " + fullTable;
			QueryJobConfiguration countConfig = QueryJobConfiguration.newBuilder(countSql)
					.setJobTimeoutMs(60_000L).build();
			TableResult countResult = bigQuery.query(countConfig);
			long beforeCount = countResult.iterateAll().iterator().next().get("cnt").getLongValue();
			result.put("beforeCount", beforeCount);

			// Deduplicate: keep earliest response per (worker_id, hit_id)
			String dedupSql = String.format(
					"CREATE OR REPLACE TABLE %s AS "
					+ "SELECT * EXCEPT(row_num) FROM ("
					+ "  SELECT *, ROW_NUMBER() OVER ("
					+ "    PARTITION BY worker_id, hit_id"
					+ "    ORDER BY date ASC"
					+ "  ) AS row_num"
					+ "  FROM %s"
					+ ") WHERE row_num = 1",
					fullTable, fullTable);

			QueryJobConfiguration dedupConfig = QueryJobConfiguration.newBuilder(dedupSql)
					.setJobTimeoutMs(120_000L).build();
			bigQuery.query(dedupConfig);

			// Count rows after dedup
			countResult = bigQuery.query(countConfig);
			long afterCount = countResult.iterateAll().iterator().next().get("cnt").getLongValue();
			result.put("afterCount", afterCount);
			result.put("duplicatesRemoved", beforeCount - afterCount);

			logger.info("BigQuery dedup complete: " + beforeCount + " -> " + afterCount
					+ " (" + (beforeCount - afterCount) + " duplicates removed)");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted during BigQuery dedup", e);
		}

		return result;
	}

	private static String sqlString(String value) {
		if (value == null) return "NULL";
		return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	private static String sha256Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}
	}

	/**
	 * Ensure the BigQuery dataset and table exist, creating them if needed.
	 */
	public void ensureTableExists(BigQuery bigQuery, String projectId) {
		// Create dataset if it doesn't exist
		DatasetId datasetId = DatasetId.of(projectId, DATASET_ID);
		Dataset dataset = bigQuery.getDataset(datasetId);
		if (dataset == null) {
			DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetId)
					.setDescription("MTurk Demographics Survey public dataset")
					.build();
			dataset = bigQuery.create(datasetInfo);
			logger.info("Created BigQuery dataset: " + DATASET_ID);
		}

		// Create table if it doesn't exist
		TableId tableId = TableId.of(DATASET_ID, TABLE_ID);
		if (bigQuery.getTable(tableId) == null) {
			Schema schema = Schema.of(
					Field.newBuilder("date", StandardSQLTypeName.TIMESTAMP)
							.setDescription("When the response was submitted").build(),
					Field.newBuilder("worker_id", StandardSQLTypeName.STRING)
							.setDescription("SHA256-hashed worker ID (privacy)").build(),
					Field.newBuilder("survey_id", StandardSQLTypeName.STRING)
							.setDescription("Survey identifier").build(),
					Field.newBuilder("country", StandardSQLTypeName.STRING)
							.setDescription("Country code from App Engine geolocation").build(),
					Field.newBuilder("region", StandardSQLTypeName.STRING)
							.setDescription("Region from App Engine geolocation").build(),
					Field.newBuilder("city", StandardSQLTypeName.STRING)
							.setDescription("City from App Engine geolocation").build(),
					Field.newBuilder("hit_id", StandardSQLTypeName.STRING)
							.setDescription("MTurk HIT ID").build(),
					Field.newBuilder("hit_creation_date", StandardSQLTypeName.TIMESTAMP)
							.setDescription("When the HIT was created on MTurk").build(),
					Field.newBuilder("ip_address", StandardSQLTypeName.STRING)
							.setDescription("SHA256-hashed IP address (privacy)").build(),
					Field.newBuilder("year_of_birth", StandardSQLTypeName.STRING)
							.setDescription("Worker's year of birth").build(),
					Field.newBuilder("gender", StandardSQLTypeName.STRING)
							.setDescription("Worker's gender").build(),
					Field.newBuilder("marital_status", StandardSQLTypeName.STRING)
							.setDescription("Worker's marital status").build(),
					Field.newBuilder("household_size", StandardSQLTypeName.STRING)
							.setDescription("Worker's household size").build(),
					Field.newBuilder("household_income", StandardSQLTypeName.STRING)
							.setDescription("Worker's household income bracket").build(),
					Field.newBuilder("educational_level", StandardSQLTypeName.STRING)
							.setDescription("Worker's educational level").build(),
					Field.newBuilder("time_spent_on_mturk", StandardSQLTypeName.STRING)
							.setDescription("Time spent on MTurk per week").build(),
					Field.newBuilder("weekly_income_from_mturk", StandardSQLTypeName.STRING)
							.setDescription("Weekly income from MTurk").build(),
					Field.newBuilder("languages_spoken", StandardSQLTypeName.STRING)
							.setDescription("Comma-separated language codes").build()
			);

			TableDefinition tableDefinition = StandardTableDefinition.of(schema);
			TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition)
					.setDescription("Individual demographics survey responses from MTurk workers (since 2015)")
					.build();
			bigQuery.create(tableInfo);
			logger.info("Created BigQuery table: " + TABLE_ID);
		}
	}
}
