package com.ipeirotis.util;

import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.ipeirotis.exception.TaskEnqueueException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TaskUtils {

	private static final Logger logger = LoggerFactory.getLogger(TaskUtils.class);

	public static String queueTask(String url, Map<String, String> params) {
		return queueTask(url, params, 0);
	}

	/**
	 * Enqueue a Cloud Task with an optional delay.
	 *
	 * @param url        relative URI for the App Engine request
	 * @param params     query parameters (may be null)
	 * @param delaySeconds seconds to wait before the task executes (0 = immediate)
	 */
	public static String queueTask(String url, Map<String, String> params, long delaySeconds) {
		String fullUrl = url;
		try (CloudTasksClient client = CloudTasksClient.create()) {
			// Variables provided by system variables.
			String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
			String queueName = System.getenv("QUEUE_ID");
			String location = System.getenv("LOCATION_ID");

			// Construct the fully qualified queue name.
			String queuePath = QueueName.of(projectId, location, queueName).toString();

			// Build form-encoded body from params
			String body = "";
			if(params != null) {
				List<String> paramsList = new ArrayList<>();
				for(Map.Entry<String, String> entry : params.entrySet()) {
					paramsList.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
						+ "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
				}
				body = String.join("&", paramsList);
			}

			// Construct the task body using POST with form-encoded params.
			AppEngineHttpRequest.Builder requestBuilder = AppEngineHttpRequest.newBuilder()
					.setRelativeUri(fullUrl)
					.setHttpMethod(HttpMethod.POST);

			if (!body.isEmpty()) {
				requestBuilder
						.putHeaders("Content-Type", "application/x-www-form-urlencoded")
						.setBody(ByteString.copyFromUtf8(body));
			}

			// Propagate correlation ID for end-to-end tracing
			String parentRequestId = MDC.get("requestId");
			if (parentRequestId != null) {
				requestBuilder.putHeaders("X-Parent-Request-Id", parentRequestId);
			}

			Task.Builder taskBuilder =
				Task.newBuilder()
					.setAppEngineHttpRequest(requestBuilder.build());

			if (delaySeconds > 0) {
				Instant executeTime = Instant.now().plusSeconds(delaySeconds);
				taskBuilder.setScheduleTime(Timestamp.newBuilder()
						.setSeconds(executeTime.getEpochSecond())
						.setNanos(executeTime.getNano())
						.build());
			}

			// Send create task request.
			Task task = client.createTask(queuePath, taskBuilder.build());
			return "Task created: " + task.getName();
		} catch(Exception e) {
			logger.error("Failed to create Cloud Task for URL: " + fullUrl, e);
			throw new TaskEnqueueException("Failed to enqueue task for URL: " + fullUrl, e);
		}
	}
}


