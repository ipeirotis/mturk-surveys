package com.ipeirotis.controller.tasks;

import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.ResourceNotFoundException;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.UserAnswerService;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.mturk.model.HIT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/tasks")
public class AddHitCreationTimeToUserAnswerController {

	private static final Logger logger = LoggerFactory.getLogger(AddHitCreationTimeToUserAnswerController.class);

	@Autowired
	private MturkService mturkService;
	@Autowired
	private UserAnswerService userAnswerService;

	@PostMapping("/addHitCreationTime")
	public void addHitCreationTime(@RequestParam String hitId) {
		if (mturkService.isClosed()) {
			// Return normally so Cloud Tasks drops the task instead of retrying.
			logger.info("MTurk has closed; skipping HIT creation time lookup for " + hitId);
			return;
		}
		HIT hit = mturkService.getHIT(true, hitId);

		if(hit == null) {
			throw new ResourceNotFoundException(String.format("HIT %s doesn't exist", hitId));
		}

		UserAnswer userAnswer = userAnswerService.get(hitId);
		if(userAnswer == null) {
			throw new ResourceNotFoundException(String.format("User answer for HIT %s doesn't exist", hitId));
		} else {
			userAnswer.setHitCreationDate(Date.from(hit.creationTime()));
			userAnswerService.save(userAnswer);
		}
	}

	public static void queueTask(String url, String hitId) {
		Map<String, String> params = new HashMap<>();
		if(hitId != null) {
			params.put("hitId", hitId);
		}
		TaskUtils.queueTask(url, params);
	}

}
