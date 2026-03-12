package com.ipeirotis.util;

import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.ipeirotis.exception.TaskEnqueueException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskUtils {

	private static final Logger logger = Logger.getLogger(TaskUtils.class.getName());

	public static String queueTask(String url, Map<String, String> params) {
		String fullUrl = url;
		try (CloudTasksClient client = CloudTasksClient.create()) {
			// Variables provided by system variables.
			String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
			String queueName = System.getenv("QUEUE_ID");
			String location = System.getenv("LOCATION_ID");

			// Construct the fully qualified queue name.
			String queuePath = QueueName.of(projectId, location, queueName).toString();

			if(params != null) {
				List<String> paramsList = new ArrayList<>();
				for(Map.Entry<String, String> entry : params.entrySet()) {
					paramsList.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
						+ "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
				}
				fullUrl = fullUrl + "?" + String.join("&", paramsList);
			}

			// Construct the task body.
			Task.Builder taskBuilder =
				Task.newBuilder()
					.setAppEngineHttpRequest(
						AppEngineHttpRequest.newBuilder()
							.setRelativeUri(fullUrl)
							.setHttpMethod(HttpMethod.GET)
							.build());

			// Send create task request.
			Task task = client.createTask(queuePath, taskBuilder.build());
			return "Task created: " + task.getName();
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to create Cloud Task for URL: " + fullUrl, e);
			throw new TaskEnqueueException("Failed to enqueue task for URL: " + fullUrl, e);
		}
	}
}


