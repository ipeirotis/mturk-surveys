package com.ipeirotis.controller.tasks;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.QueryResults;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.UserAnswerService;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.mturk.model.DeleteHitResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

@RestController
@RequestMapping("/tasks")
public class DeleteHITsController {

	private static final Logger logger = Logger.getLogger(DeleteHITsController.class.getName());

	@Autowired
	private MturkService mturkService;
	@Autowired
	private UserAnswerService userAnswerService;

	@GetMapping({"/deleteHITs"})
	public void deleteHITs(@RequestParam String cursor, @RequestParam String sched) {
		if(!"true".equals(sched)) {
			String nextPageToken = delete(cursor);
			if(nextPageToken != null) {
				queueTask("/tasks/deleteHITs", nextPageToken);
			}
		} else {
			queueTask("/tasks/deleteHITs", null);
		}
	}

	private String delete(String cursorString) {
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(new Date());
		endCal.set(Calendar.HOUR_OF_DAY, 0);
		endCal.set(Calendar.MINUTE, 0);
		endCal.set(Calendar.SECOND, 0);

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(endCal.getTime());
		startCal.add(Calendar.DAY_OF_MONTH, -1);

		Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
				.filter("date >=", startCal.getTime()).filter("date <", endCal.getTime()).limit(30);

		if (cursorString != null) {
			query = query.startAt(Cursor.fromUrlSafe(cursorString));
		}

		boolean cont = false;
		QueryResults<UserAnswer> iterator = query.iterator();

		while (iterator.hasNext()) {
			UserAnswer userAnswer = iterator.next();
			try {
				mturkService.deleteHIT(true, userAnswer.getHitId());
				logger.log(Level.INFO, String.format("Deleted HIT %s", userAnswer.getHitId()));
			} catch(Exception e) {
				logger.log(Level.SEVERE, String.format("Error deleting HIT %s", userAnswer.getHitId()), e);
			}
			cont = true;
		}

		if(cont) {
			Cursor cursor = iterator.getCursorAfter();
			return cursor.toUrlSafe();
		} else {
			return null;
		}
	}

	public void queueTask(String url, String cursorStr) {
		Map<String, String> params = new HashMap<>();
		if(cursorStr != null) {
			params.put("cursor", cursorStr);
		}
		TaskUtils.queueTask(url, params);
	}

}
