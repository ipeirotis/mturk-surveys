package com.ipeirotis.service;

import com.google.cloud.bigquery.*;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BigQueryExportService {

	private static final Logger logger = Logger.getLogger(BigQueryExportService.class.getName());

	private static final String DATASET_ID = "demographics";
	private static final String TABLE_ID = "responses";

	@Autowired
	private SurveyService surveyService;

	/**
	 * Export a single day's data to BigQuery.
	 * @param dateStr date in MM/dd/yyyy format
	 * @return number of rows exported
	 */
	public int exportDate(String dateStr) throws ParseException {
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

		BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
		String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

		ensureTableExists(bigQuery, projectId);

		String sortableDate = SafeDateFormat.forPattern("yyyy-MM-dd").format(dateFrom.getTime());
		String fullTable = String.format("`%s.%s.%s`", projectId, DATASET_ID, TABLE_ID);

		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Build a single transactional script: DELETE + INSERT(s)
		// This prevents duplicates from concurrent exports for the same date
		StringBuilder script = new StringBuilder();
		script.append("BEGIN TRANSACTION;\n");

		// Delete existing rows for this date
		script.append(String.format("DELETE FROM %s WHERE DATE(date) = '%s';\n",
				fullTable, sortableDate));

		// Build INSERT statements in batches
		int batchSize = 200;
		for (int i = 0; i < answers.size(); i += batchSize) {
			int end = Math.min(i + batchSize, answers.size());
			List<UserAnswer> batch = answers.subList(i, end);

			script.append(String.format("INSERT INTO %s "
					+ "(date, worker_id, survey_id, country, region, city, hit_id, "
					+ "hit_creation_date, ip_address, year_of_birth, gender, marital_status, "
					+ "household_size, household_income, educational_level, "
					+ "time_spent_on_mturk, weekly_income_from_mturk, languages_spoken) VALUES ",
					fullTable));

			for (int j = 0; j < batch.size(); j++) {
				UserAnswer ua = batch.get(j);
				if (j > 0) script.append(", ");
				script.append("(");

				// date
				script.append(ua.getDate() != null
						? "TIMESTAMP('" + isoFormat.format(ua.getDate()) + "')" : "NULL");
				script.append(", ");

				// worker_id (SHA256-hashed)
				String workerId = ua.getWorkerId();
				script.append(workerId != null ? sqlString(sha256Hex(workerId)) : "NULL");
				script.append(", ");

				// survey_id
				script.append(sqlString(ua.getSurveyId()));
				script.append(", ");

				// country, region, city
				script.append(sqlString(ua.getLocationCountry())).append(", ");
				script.append(sqlString(ua.getLocationRegion())).append(", ");
				script.append(sqlString(ua.getLocationCity())).append(", ");

				// hit_id
				script.append(sqlString(ua.getHitId())).append(", ");

				// hit_creation_date
				script.append(ua.getHitCreationDate() != null
						? "TIMESTAMP('" + isoFormat.format(ua.getHitCreationDate()) + "')" : "NULL");
				script.append(", ");

				// ip_address (SHA256-hashed)
				String ip = ua.getIp();
				script.append(ip != null ? sqlString(sha256Hex(ip)) : "NULL");

				// answer fields
				Map<String, String> a = ua.getAnswers();
				String[] keys = {"yearOfBirth", "gender", "maritalStatus", "householdSize",
						"householdIncome", "educationalLevel", "timeSpentOnMturk",
						"weeklyIncomeFromMturk", "languagesSpoken"};
				for (String key : keys) {
					script.append(", ");
					script.append(a != null ? sqlString(a.get(key)) : "NULL");
				}

				script.append(")");
			}
			script.append(";\n");
		}

		script.append("COMMIT TRANSACTION;\n");

		try {
			QueryJobConfiguration scriptConfig = QueryJobConfiguration.newBuilder(script.toString())
					.build();
			bigQuery.query(scriptConfig);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted during BigQuery export transaction", e);
		}

		int totalExported = answers.size();
		logger.info("Exported " + totalExported + " rows to BigQuery for " + dateStr);
		return totalExported;
	}

	/**
	 * Remove duplicate rows from the BigQuery demographics.responses table.
	 * Keeps the earliest response per (worker_id, hit_id) pair.
	 * @return map with beforeCount, afterCount, duplicatesRemoved
	 */
	public Map<String, Object> deduplicateTable() {
		BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
		String projectId = BigQueryOptions.getDefaultInstance().getProjectId();
		String fullTable = String.format("`%s.%s.%s`", projectId, DATASET_ID, TABLE_ID);

		Map<String, Object> result = new LinkedHashMap<>();

		try {
			// Count rows before dedup
			String countSql = "SELECT COUNT(*) AS cnt FROM " + fullTable;
			QueryJobConfiguration countConfig = QueryJobConfiguration.newBuilder(countSql).build();
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

			QueryJobConfiguration dedupConfig = QueryJobConfiguration.newBuilder(dedupSql).build();
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
	 * The dataset is made publicly readable.
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

		// Make dataset publicly readable
		try {
			Acl publicAccess = Acl.of(new Acl.Group("allUsers"), Acl.Role.READER);
			List<Acl> acls = new ArrayList<>(dataset.getAcl());
			boolean hasPublicAccess = acls.stream().anyMatch(
					acl -> acl.getEntity() instanceof Acl.Group
							&& "allUsers".equals(((Acl.Group) acl.getEntity()).getIdentifier()));
			if (!hasPublicAccess) {
				acls.add(publicAccess);
				bigQuery.update(dataset.toBuilder().setAcl(acls).build());
				logger.info("Set public access on dataset: " + DATASET_ID);
			}
		} catch (BigQueryException e) {
			logger.log(Level.WARNING, "Could not set public access on dataset: " + e.getMessage(), e);
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
