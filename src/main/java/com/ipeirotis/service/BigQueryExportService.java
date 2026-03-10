package com.ipeirotis.service;

import com.google.cloud.bigquery.*;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.MD5;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

		List<UserAnswer> answers = surveyService.listAnswersByDateRange(dateFrom.getTime(), dateTo.getTime());

		if (answers.isEmpty()) {
			logger.info("No responses found for " + dateStr + ", skipping BigQuery export");
			return 0;
		}

		BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
		String projectId = BigQueryOptions.getDefaultInstance().getProjectId();

		ensureTableExists(bigQuery, projectId);

		// Delete existing rows for this date to make export idempotent
		String sortableDate = SafeDateFormat.forPattern("yyyy-MM-dd").format(dateFrom.getTime());
		String deleteSql = String.format(
				"DELETE FROM `%s.%s.%s` WHERE DATE(date) = '%s'",
				projectId, DATASET_ID, TABLE_ID, sortableDate);
		try {
			QueryJobConfiguration deleteConfig = QueryJobConfiguration.newBuilder(deleteSql).build();
			bigQuery.query(deleteConfig);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while deleting existing rows", e);
		} catch (BigQueryException e) {
			// Table may not have data for this date yet, that's fine
			logger.info("Delete for date " + sortableDate + " returned: " + e.getMessage());
		}

		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		TableId tableId = TableId.of(DATASET_ID, TABLE_ID);

		// Batch inserts in groups of 500 (BigQuery streaming insert limit is 10,000 per request)
		int batchSize = 500;
		int totalExported = 0;

		for (int i = 0; i < answers.size(); i += batchSize) {
			int end = Math.min(i + batchSize, answers.size());
			List<UserAnswer> batch = answers.subList(i, end);

			InsertAllRequest.Builder requestBuilder = InsertAllRequest.newBuilder(tableId);

			for (UserAnswer ua : batch) {
				Map<String, Object> row = new HashMap<>();

				if (ua.getDate() != null) {
					row.put("date", isoFormat.format(ua.getDate()));
				}

				String workerId = ua.getWorkerId();
				row.put("worker_id", workerId != null ? MD5.crypt(workerId) : null);
				row.put("country", ua.getLocationCountry());
				row.put("region", ua.getLocationRegion());
				row.put("city", ua.getLocationCity());
				row.put("hit_id", ua.getHitId());

				Map<String, String> a = ua.getAnswers();
				if (a != null) {
					row.put("year_of_birth", a.get("yearOfBirth"));
					row.put("gender", a.get("gender"));
					row.put("marital_status", a.get("maritalStatus"));
					row.put("household_size", a.get("householdSize"));
					row.put("household_income", a.get("householdIncome"));
					row.put("educational_level", a.get("educationalLevel"));
					row.put("time_spent_on_mturk", a.get("timeSpentOnMturk"));
					row.put("weekly_income_from_mturk", a.get("weeklyIncomeFromMturk"));
					row.put("languages_spoken", a.get("languagesSpoken"));
				}

				requestBuilder.addRow(row);
			}

			InsertAllResponse response = bigQuery.insertAll(requestBuilder.build());
			if (response.hasErrors()) {
				for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
					logger.log(Level.WARNING, "Row " + entry.getKey() + " errors: " + entry.getValue());
				}
			}
			totalExported += batch.size();
		}

		logger.info("Exported " + totalExported + " rows to BigQuery for " + dateStr);
		return totalExported;
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
							.setDescription("MD5-hashed worker ID (privacy)").build(),
					Field.newBuilder("country", StandardSQLTypeName.STRING)
							.setDescription("Country code from App Engine geolocation").build(),
					Field.newBuilder("region", StandardSQLTypeName.STRING)
							.setDescription("Region from App Engine geolocation").build(),
					Field.newBuilder("city", StandardSQLTypeName.STRING)
							.setDescription("City from App Engine geolocation").build(),
					Field.newBuilder("hit_id", StandardSQLTypeName.STRING)
							.setDescription("MTurk HIT ID").build(),
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
