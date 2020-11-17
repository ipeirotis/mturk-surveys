package com.ipeirotis.controller;

import com.ipeirotis.service.MturkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.mturk.model.Assignment;
import software.amazon.awssdk.services.mturk.model.HIT;

import java.util.List;

@RestController
@RequestMapping("/")
public class MturkController {

	@Autowired
	private MturkService mturkService;

	@GetMapping("/getHIT/{hitId}")
	public HIT getHIT(@PathVariable String hitId, @RequestParam Boolean production) {
		return mturkService.getHIT(production, hitId);
	}

	@GetMapping({"/listHITs"})
	public List<HIT> listHITs (@RequestParam Boolean production) {
		return mturkService.listHits(production);
	}

	@GetMapping({"/getAssignmentsForHIT/{hitId}"})
	public List<Assignment> getAssignmentsForHIT (@PathVariable String hitId, @RequestParam Boolean production) {
		return mturkService.listAssignmentsForHit(production, hitId);
	}

}
