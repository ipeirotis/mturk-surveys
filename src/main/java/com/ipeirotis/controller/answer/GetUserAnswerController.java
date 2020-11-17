package com.ipeirotis.controller.answer;

import com.google.gson.Gson;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class GetUserAnswerController {

	@Autowired
	private UserAnswerService userAnswerService;

	@RequestMapping(value = "/getAnswer", method = RequestMethod.GET, produces = "application/javascript")
	public ResponseEntity getAnswer(@RequestParam String callback, @RequestParam String workerId, @RequestParam String surveyId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("workerId", workerId);
		params.put("surveyId", surveyId);
		List<UserAnswer> existingList = userAnswerService.query(params);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);

		for(UserAnswer userAnswer : existingList) {
			if(userAnswer.getDate().after(cal.getTime()) && callback != null) {
				return new ResponseEntity<>(callback + "(" + new Gson().toJson(userAnswer) +");", HttpStatus.OK);
			}
		}

		if(callback != null) {
			return new ResponseEntity<>(callback + "();", HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
