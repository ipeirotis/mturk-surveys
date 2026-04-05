package com.ipeirotis.controller.tasks;

import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.ListTasksRequest;
import com.google.cloud.tasks.v2.QueueName;
import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.entity.UserAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Authenticated diagnostic endpoint reporting operational health:
 * pending task count, last snapshot date, last BigQuery export, and last HIT creation.
 * Protected by TaskAuthFilter (requires App Engine Cron, Cloud Tasks, or admin key).
 */
@RestController
public class TaskStatusController {

    private static final Logger logger = LoggerFactory.getLogger(TaskStatusController.class);

    @GetMapping("/tasks/status")
    public Map<String, Object> taskStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("timestamp", Instant.now().toString());

        // 1. Pending tasks in Cloud Tasks queue
        status.put("pendingTasks", getPendingTaskCount());

        // 2. Last successful snapshot date
        status.put("lastSnapshotDate", getLastSnapshotDate());

        // 3. Last UserAnswer received (proxy for last HIT activity)
        status.put("lastUserAnswerDate", getLastUserAnswerDate());

        return status;
    }

    private Object getPendingTaskCount() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        String queueName = System.getenv("QUEUE_ID");
        String location = System.getenv("LOCATION_ID");

        if (projectId == null || queueName == null || location == null) {
            return "unavailable (env vars not set)";
        }

        try (CloudTasksClient client = CloudTasksClient.create()) {
            String queuePath = QueueName.of(projectId, location, queueName).toString();
            ListTasksRequest request = ListTasksRequest.newBuilder()
                    .setParent(queuePath)
                    .build();
            int count = 0;
            for (var ignored : client.listTasks(request).iterateAll()) {
                count++;
                if (count >= 1000) {
                    return "1000+";
                }
            }
            return count;
        } catch (Exception e) {
            logger.warn("Failed to query Cloud Tasks queue", e);
            return "error: " + e.getMessage();
        }
    }

    private String getLastSnapshotDate() {
        try {
            DemographicsSnapshot latest = ofy().load().type(DemographicsSnapshot.class)
                    .order("-date")
                    .limit(1)
                    .first()
                    .now();
            return latest != null ? latest.getDate() : "none";
        } catch (Exception e) {
            logger.warn("Failed to query last snapshot", e);
            return "error: " + e.getMessage();
        }
    }

    private String getLastUserAnswerDate() {
        try {
            UserAnswer latest = ofy().load().type(UserAnswer.class)
                    .order("-date")
                    .limit(1)
                    .first()
                    .now();
            if (latest != null && latest.getDate() != null) {
                Date d = latest.getDate();
                return DateTimeFormatter.ISO_INSTANT.format(d.toInstant().atOffset(ZoneOffset.UTC));
            }
            return "none";
        } catch (Exception e) {
            logger.warn("Failed to query last user answer", e);
            return "error: " + e.getMessage();
        }
    }
}
