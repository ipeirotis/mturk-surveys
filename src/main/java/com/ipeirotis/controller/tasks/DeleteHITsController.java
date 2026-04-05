package com.ipeirotis.controller.tasks;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.QueryResults;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ipeirotis.util.CalendarUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.googlecode.objectify.ObjectifyService.ofy;

@RestController
@RequestMapping("/tasks")
public class DeleteHITsController {

	private static final Logger logger = LoggerFactory.getLogger(DeleteHITsController.class);
	private static final int MAX_PAGES = 200; // Safety limit: 200 pages * 30 items = 6000 HITs max

	@Autowired
	private MturkService mturkService;

	@RequestMapping(value = "/deleteHITs", method = {RequestMethod.GET, RequestMethod.POST})
	public void deleteHITs(@RequestParam(required = false) String cursor,
			@RequestParam(required = false) String sched,
			@RequestParam(required = false, defaultValue = "0") int page) {
		if(!"true".equals(sched)) {
			String nextPageToken = delete(cursor);
			if(nextPageToken != null) {
				if (page >= MAX_PAGES) {
					logger.error("deleteHITs reached max page limit (" + MAX_PAGES + "), stopping");
					return;
				}
				queueTask("/tasks/deleteHITs", nextPageToken, page + 1);
			}
		} else {
			queueTask("/tasks/deleteHITs", null, 0);
		}
	}

	private String delete(String cursorString) {
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(new Date());
		CalendarUtils.truncateToDay(endCal);

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
				logger.info(String.format("Deleted HIT %s", userAnswer.getHitId()));
			} catch(Exception e) {
				logger.error(String.format("Error deleting HIT %s", userAnswer.getHitId()), e);
			}
			cont = true;
		}

		if(cont) {
			Cursor cursorObj = iterator.getCursorAfter();
			return cursorObj.toUrlSafe();
		} else {
			return null;
		}
	}

	public void queueTask(String url, String cursorStr, int page) {
		Map<String, String> params = new HashMap<>();
		if(cursorStr != null) {
			params.put("cursor", cursorStr);
		}
		params.put("page", String.valueOf(page));
		TaskUtils.queueTask(url, params);
	}

}
