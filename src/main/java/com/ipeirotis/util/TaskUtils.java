package com.ipeirotis.util;

import com.google.cloud.tasks.v2.AppEngineHttpRequest;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskUtils {

	public static String queueTask(String url, Map<String, String> params) {
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
					paramsList.add(entry.getKey() + "=" + entry.getValue());
				}
				url = url + "?" + String.join("&", paramsList);
			}
	  
			// Construct the task body.
			Task.Builder taskBuilder =
				Task.newBuilder()
					.setAppEngineHttpRequest(
						AppEngineHttpRequest.newBuilder()
							.setRelativeUri(url)
							.setHttpMethod(HttpMethod.GET)
							.build());
	  
			// Send create task request.
			Task task = client.createTask(queuePath, taskBuilder.build());
			return "Task created: " + task.getName();
		} catch(Exception e) {
			return null;
		}
	}
}


