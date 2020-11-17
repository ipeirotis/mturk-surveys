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
import software.amazon.awssdk.services.mturk.model.Assignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

@RestController
@RequestMapping("/tasks")
public class ApproveAssignmentsController {

	@Autowired
	private MturkService mturkService;
	@Autowired
	private UserAnswerService userAnswerService;

	@GetMapping({"/approveAssignments"})
	public void approveAssignments(@RequestParam String cursor, @RequestParam String sched) {
		if(!"true".equals(sched)) {
			String nextPageToken = approve(cursor);
			if(nextPageToken != null) {
				queueTask("/tasks/approveAssignments", nextPageToken);
			}
		} else {
			queueTask("/tasks/approveAssignments", null);
		}
	}

	private String approve(String cursorString) {
		Query<UserAnswer> query = ofy().load().type(UserAnswer.class).limit(30);

		if (cursorString != null) {
			query = query.startAt(Cursor.fromUrlSafe(cursorString));
		}

		boolean cont = false;
		QueryResults<UserAnswer> iterator = query.iterator();

		while (iterator.hasNext()) {
			UserAnswer userAnswer = iterator.next();
			List<Assignment> assignments = mturkService.listAssignmentsForHit(true, userAnswer.getHitId());
			for(Assignment assignment: assignments) {
				mturkService.approveAssignment(true, assignment.assignmentId());
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
