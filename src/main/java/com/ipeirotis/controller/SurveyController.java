package com.ipeirotis.controller;

import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.ResourceNotFoundException;
import com.ipeirotis.ofy.ListByCursorResult;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/survey")
public class SurveyController {

	@Autowired
	private SurveyService surveyService;
	@Autowired
	private UserAnswerService userAnswerService;

	@GetMapping({"/demographics/answers"})
	public ListByCursorResult<UserAnswer> getSurveyAnswers(@RequestParam String cursor, @RequestParam Integer limit) {
		return userAnswerService.list(cursor, limit);
	}

	@GetMapping({"/demographics/aggregatedAnswers"})
	public DemographicsSurveyAnswersByPeriod getSurveyAggregatedAnswers(@RequestParam String from, String to) throws ParseException {
		return surveyService.getDemographicsAnswers(from, to);
	}

	@GetMapping("/{surveyId}")
	public Survey get(@PathVariable String surveyId) {
		Survey survey = surveyService.get(surveyId);
		if(survey == null) {
			throw new ResourceNotFoundException(String.format("Survey with id=%s doesn't exist", surveyId));
		} else {
			return survey;
		}
	}

	@PostMapping
	public Survey create(@RequestBody Survey survey) {
		return surveyService.create(survey);
	}

}
