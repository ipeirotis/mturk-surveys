package com.ipeirotis.controller.tasks;

import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.mturk.model.HIT;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/tasks")
public class CreateHITController {

	private static final Logger logger = Logger.getLogger(CreateHITController.class.getName());
	private static final int MAX_RETRIES = 5;

	@Autowired
	private MturkService mturkService;
	@Autowired
	private SurveyService surveyService;

	@RequestMapping(value = "/createHIT", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity createHIT(@RequestParam String surveyId, @RequestParam Boolean production,
			@RequestParam(required = false, defaultValue = "0") int retryCount,
			@RequestParam(required = false) String idempotencyToken) {
		// Generate a deterministic token from surveyId + time window so that
		// retries (including crash-recovery redeliveries) produce the same token.
		// Truncate to 15-minute intervals matching the cron schedule.
		if (idempotencyToken == null || idempotencyToken.isEmpty()) {
			long window = Instant.now().truncatedTo(ChronoUnit.SECONDS).getEpochSecond() / 900;
			String raw = surveyId + "|" + window;
			idempotencyToken = sha256Hex(raw);
		}
		try {
			Survey survey = surveyService.get(surveyId);
			if(survey == null) {
				String error = String.format("Error creating HIT: survey %s doesn't exist", surveyId);
				logger.log(Level.SEVERE, error);
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			} else {
				HIT hit = mturkService.createHIT(production, survey, idempotencyToken);
				String responseText = "created HIT with id: " + hit.hitId() +
						", preview: https://" + (production ? "www" : "workersandbox") + ".mturk.com/mturk/preview?groupId=" + hit.hitGroupId();
				logger.info(responseText);
				return new ResponseEntity<>(responseText, HttpStatus.OK);
			}
		} catch (AwsServiceException e) {
			// If MTurk says the HIT already exists (idempotent duplicate), treat as success.
			// Match on the structured error code, not message text.
			String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "";
			if ("AWS.MechanicalTurk.HitAlreadyExists".equals(errorCode)) {
				logger.info("HIT already exists for token " + idempotencyToken + " (idempotent success)");
				return new ResponseEntity<>("HIT already exists (idempotent success)", HttpStatus.OK);
			}
			return handleRetry(e, surveyId, production, retryCount, idempotencyToken);
		} catch (Exception e) {
			return handleRetry(e, surveyId, production, retryCount, idempotencyToken);
		}
	}

	private ResponseEntity<String> handleRetry(Exception e, String surveyId, Boolean production,
			int retryCount, String idempotencyToken) {
		if (retryCount >= MAX_RETRIES) {
			logger.log(Level.SEVERE, "Error creating HIT after " + MAX_RETRIES + " retries, giving up", e);
			return new ResponseEntity<>("Gave up after " + MAX_RETRIES + " retries: " + e.getMessage(), HttpStatus.OK);
		}
		logger.log(Level.WARNING, "Error creating HIT (retry " + (retryCount + 1) + "/" + MAX_RETRIES + "), re-enqueuing", e);
		queueTask(surveyId, production, retryCount + 1, idempotencyToken);
		return new ResponseEntity<>("Re-enqueued retry " + (retryCount + 1) + ": " + e.getMessage(), HttpStatus.OK);
	}

	private static String sha256Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(64);
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}
	}

	public void queueTask(String surveyId, boolean production, int retryCount, String idempotencyToken) {
		Map<String, String> params = new HashMap<>();
		params.put("surveyId", surveyId);
		params.put("production", String.valueOf(production));
		params.put("retryCount", String.valueOf(retryCount));
		params.put("idempotencyToken", idempotencyToken);
		// Exponential backoff: 2^retryCount seconds (2s, 4s, 8s, 16s, 32s)
		long delaySeconds = (long) Math.pow(2, retryCount);
		TaskUtils.queueTask("/tasks/createHIT", params, delaySeconds);
	}

}
