package com.ipeirotis.controller.tasks;

import com.ipeirotis.service.BigQueryExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BigQueryDedupController {

	@Autowired
	private BigQueryExportService bigQueryExportService;

	/**
	 * One-time deduplication of the BigQuery demographics.responses table.
	 * Keeps the earliest response per (worker_id, hit_id) pair.
	 */
	@GetMapping("/tasks/dedupBigQuery")
	public Map<String, Object> deduplicateBigQuery() {
		return bigQueryExportService.deduplicateTable();
	}
}
