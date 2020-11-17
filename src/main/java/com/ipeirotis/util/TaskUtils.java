package com.ipeirotis.util;

import com.google.appengine.api.taskqueue.*;

import java.util.Map;

public class TaskUtils {

	public static String queueTask(String url) {
		return queueTask(url, null);
	}

	public static String queueTask(String url, Map<String, String> params) {
		return queueTask(url, null, params);
	}

	public static String queueTask(String url, String queueName, Map<String, String> params) {
		TaskOptions taskOptions = TaskOptions.Builder
				.withMethod(TaskOptions.Method.GET)
				.url(url)
				.etaMillis(System.currentTimeMillis())
				.retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));

		if(params != null) {
			for(Map.Entry<String, String> entry : params.entrySet()) {
				taskOptions.param(entry.getKey(), entry.getValue());
			}
		}

		Queue queue;
		if(queueName == null) {
			queue = QueueFactory.getDefaultQueue();
		} else {
			queue = QueueFactory.getQueue(queueName);
		}
		TaskHandle taskHandle = queue.add(taskOptions);
		return String.format("Queued task %s", taskHandle.getName());
	}

}
