package com.ipeirotis.controller.tasks;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled full Datastore export to Google Cloud Storage.
 * Creates timestamped backups that can be used for disaster recovery
 * or imported into BigQuery for analysis.
 *
 * Requires the App Engine default service account to have the
 * "Cloud Datastore Import Export Admin" role (roles/datastore.importExportAdmin)
 * and write access to the GCS bucket.
 */
@RestController
public class DatastoreBackupController {

	private static final Logger logger = Logger.getLogger(DatastoreBackupController.class.getName());

	private static final String GCS_BUCKET = "demographics_data_export";
	private static final String PROJECT_ID = "mturk-demographics";

	/**
	 * Cron-triggered: export all Datastore entities to GCS.
	 * Creates a timestamped folder in gs://demographics_data_export/.
	 *
	 * Example: /tasks/backupDatastore
	 * Example: /tasks/backupDatastore?kinds=UserAnswer,DemographicsSnapshot
	 *
	 * @param kinds optional comma-separated list of entity kinds to export.
	 *              If omitted, exports all kinds.
	 */
	@GetMapping("/tasks/backupDatastore")
	public Map<String, Object> backupDatastore(
			@RequestParam(required = false) String kinds) {

		String timestamp = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String outputUrl = "gs://" + GCS_BUCKET + "/" + timestamp;

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("bucket", GCS_BUCKET);
		result.put("outputUrl", outputUrl);

		try {
			// Build the export request body
			StringBuilder json = new StringBuilder();
			json.append("{\"outputUrlPrefix\":\"").append(outputUrl).append("\"");

			if (kinds != null && !kinds.isBlank()) {
				json.append(",\"entityFilter\":{\"kinds\":[");
				String[] kindList = kinds.split(",");
				for (int i = 0; i < kindList.length; i++) {
					if (i > 0) json.append(",");
					json.append("\"").append(kindList[i].trim()).append("\"");
				}
				json.append("]}");
				result.put("kinds", kinds);
			}
			json.append("}");

			// Get access token from default credentials
			GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
					.createScoped("https://www.googleapis.com/auth/datastore");
			credentials.refreshIfExpired();
			String accessToken = credentials.getAccessToken().getTokenValue();

			// Call the Datastore Admin export API
			String apiUrl = "https://datastore.googleapis.com/v1/projects/" + PROJECT_ID + ":export";
			URL url = new URL(apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				os.write(json.toString().getBytes(StandardCharsets.UTF_8));
			}

			int responseCode = conn.getResponseCode();
			String responseBody;
			try (var is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
				responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			}

			result.put("httpStatus", responseCode);

			if (responseCode >= 200 && responseCode < 300) {
				result.put("status", "ok");
				result.put("response", responseBody);
				logger.info("Datastore export started: " + outputUrl);
			} else {
				result.put("status", "error");
				result.put("error", responseBody);
				logger.log(Level.WARNING, "Datastore export failed (" + responseCode + "): " + responseBody);
			}
		} catch (IOException e) {
			result.put("status", "error");
			result.put("error", e.getMessage());
			logger.log(Level.SEVERE, "Datastore export failed", e);
		}

		return result;
	}
}
