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
			@RequestParam(required = false, defaultValue = "0") int retryCount) {
		try {
			Survey survey = surveyService.get(surveyId);
			if(survey == null) {
				String error = String.format("Error creating HIT: survey %s doesn't exist", surveyId);
				logger.log(Level.SEVERE, error);
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			} else {
				HIT hit = mturkService.createHIT(production, survey);
				String responseText = "created HIT with id: " + hit.hitId() +
						", preview: https://" + (production ? "www" : "workersandbox") + ".mturk.com/mturk/preview?groupId=" + hit.hitGroupId();
				logger.info(responseText);
				return new ResponseEntity<>(responseText, HttpStatus.OK);
			}
		} catch (Exception e) {
			if (retryCount >= MAX_RETRIES) {
				logger.log(Level.SEVERE, "Error creating HIT after " + MAX_RETRIES + " retries, giving up", e);
				// Return 200 so Cloud Tasks considers the task complete and stops retrying
				return new ResponseEntity<>("Gave up after " + MAX_RETRIES + " retries: " + e.getMessage(), HttpStatus.OK);
			}
			logger.log(Level.WARNING, "Error creating HIT (retry " + (retryCount + 1) + "/" + MAX_RETRIES + "), re-enqueuing", e);
			queueTask(surveyId, production, retryCount + 1);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void queueTask(String surveyId, boolean production, int retryCount) {
		Map<String, String> params = new HashMap<>();
		params.put("surveyId", surveyId);
		params.put("production", String.valueOf(production));
		params.put("retryCount", String.valueOf(retryCount));
		// Exponential backoff: 2^retryCount seconds (2s, 4s, 8s, 16s, 32s)
		long delaySeconds = (long) Math.pow(2, retryCount);
		TaskUtils.queueTask("/tasks/createHIT", params, delaySeconds);
	}

}
