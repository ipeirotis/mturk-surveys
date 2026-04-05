package com.ipeirotis.controller;

import com.ipeirotis.service.MturkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.mturk.model.Assignment;
import software.amazon.awssdk.services.mturk.model.HIT;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MturkController {

	@Autowired
	private MturkService mturkService;

	@GetMapping("/getHIT/{hitId}")
	public Map<String, Object> getHIT(@PathVariable String hitId, @RequestParam Boolean production) {
		return hitToMap(mturkService.getHIT(production, hitId));
	}

	@GetMapping({"/listHITs"})
	public List<Map<String, Object>> listHITs(@RequestParam Boolean production) {
		return mturkService.listHits(production).stream().map(this::hitToMap).toList();
	}

	@GetMapping({"/getAssignmentsForHIT/{hitId}"})
	public List<Map<String, Object>> getAssignmentsForHIT(@PathVariable String hitId, @RequestParam Boolean production) {
		return mturkService.listAssignmentsForHit(production, hitId).stream().map(this::assignmentToMap).toList();
	}

	private Map<String, Object> hitToMap(HIT hit) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("hitId", hit.hitId());
		map.put("hitTypeId", hit.hitTypeId());
		map.put("hitGroupId", hit.hitGroupId());
		map.put("title", hit.title());
		map.put("description", hit.description());
		map.put("hitStatusAsString", hit.hitStatusAsString());
		map.put("maxAssignments", hit.maxAssignments());
		map.put("reward", hit.reward());
		map.put("creationTime", hit.creationTime() != null ? hit.creationTime().toString() : null);
		map.put("expiration", hit.expiration() != null ? hit.expiration().toString() : null);
		map.put("autoApprovalDelayInSeconds", hit.autoApprovalDelayInSeconds());
		map.put("assignmentDurationInSeconds", hit.assignmentDurationInSeconds());
		map.put("numberOfAssignmentsPending", hit.numberOfAssignmentsPending());
		map.put("numberOfAssignmentsAvailable", hit.numberOfAssignmentsAvailable());
		map.put("numberOfAssignmentsCompleted", hit.numberOfAssignmentsCompleted());
		map.put("hitReviewStatusAsString", hit.hitReviewStatusAsString());
		return map;
	}

	private Map<String, Object> assignmentToMap(Assignment a) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("assignmentId", a.assignmentId());
		map.put("workerId", a.workerId());
		map.put("hitId", a.hitId());
		map.put("assignmentStatusAsString", a.assignmentStatusAsString());
		map.put("acceptTime", a.acceptTime() != null ? a.acceptTime().toString() : null);
		map.put("submitTime", a.submitTime() != null ? a.submitTime().toString() : null);
		map.put("approvalTime", a.approvalTime() != null ? a.approvalTime().toString() : null);
		map.put("answer", a.answer());
		return map;
	}

}
