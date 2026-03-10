package com.ipeirotis.controller.tasks;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.QueryResults;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.TaskUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class CleanupUserAnswersController {

    private static final Logger logger = Logger.getLogger(CleanupUserAnswersController.class.getName());
    private static final int BATCH_SIZE = 200;

    @GetMapping("/cleanupUserAnswers")
    public Map<String, Object> cleanupUserAnswers(
            @RequestParam String surveyId,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(required = false) String cursor) {

        boolean isNull = "null".equals(surveyId);

        Query<UserAnswer> query = ofy().load().type(UserAnswer.class);
        if (isNull) {
            query = query.filter("surveyId", null);
        } else {
            query = query.filter("surveyId", surveyId);
        }
        query = query.limit(BATCH_SIZE);

        if (cursor != null) {
            query = query.startAt(Cursor.fromUrlSafe(cursor));
        }

        List<Key<UserAnswer>> keysToDelete = new ArrayList<>();
        QueryResults<UserAnswer> iterator = query.iterator();
        int count = 0;
        String minDate = null;
        String maxDate = null;

        while (iterator.hasNext()) {
            UserAnswer ua = iterator.next();
            keysToDelete.add(Key.create(UserAnswer.class, ua.getId()));
            count++;
            String dateStr = ua.getDate() != null ? ua.getDate().toString() : "null";
            if (minDate == null || (ua.getDate() != null && dateStr.compareTo(minDate) < 0)) {
                minDate = dateStr;
            }
            if (maxDate == null || (ua.getDate() != null && dateStr.compareTo(maxDate) > 0)) {
                maxDate = dateStr;
            }
        }

        if (!dryRun && !keysToDelete.isEmpty()) {
            ofy().delete().keys(keysToDelete).now();
            logger.log(Level.INFO, String.format("Deleted %d UserAnswers with surveyId=%s", count, surveyId));
        } else if (dryRun) {
            logger.log(Level.INFO, String.format("Dry run: found %d UserAnswers with surveyId=%s", count, surveyId));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("surveyId", surveyId);
        result.put("dryRun", dryRun);
        result.put("processed", count);
        result.put("minDate", minDate);
        result.put("maxDate", maxDate);

        if (count == BATCH_SIZE) {
            String nextCursor = iterator.getCursorAfter().toUrlSafe();
            result.put("nextCursor", nextCursor);
            result.put("status", "more_remaining");

            // Auto-queue next batch
            if (!dryRun) {
                Map<String, String> params = new HashMap<>();
                params.put("surveyId", surveyId);
                params.put("dryRun", String.valueOf(dryRun));
                params.put("cursor", nextCursor);
                TaskUtils.queueTask("/tasks/cleanupUserAnswers", params);
                result.put("queued", true);
            }
        } else {
            result.put("status", "complete");
        }

        return result;
    }
}
