package com.ipeirotis.controller.answer;

import com.google.gson.Gson;
import com.ipeirotis.controller.tasks.AddHitCreationTimeToUserAnswerController;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.UserAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/")
public class SaveUserAnswerController {

	@Autowired
	private UserAnswerService userAnswerService;

	@RequestMapping(value = "/saveAnswer", method = RequestMethod.GET, produces = "application/javascript")
	public ResponseEntity saveAnswer(@RequestParam("userAnswer") String userAnswerJson,
									 @RequestParam String callback,
									 @RequestHeader("X-AppEngine-Country") String country,
									 @RequestHeader("X-AppEngine-Region") String region,
									 @RequestHeader("X-AppEngine-City") String city) {
		Gson gson = new Gson();
		UserAnswer userAnswer = gson.fromJson(userAnswerJson, UserAnswer.class);
		if(userAnswer == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		String ip = getIp(RequestContextHolder.getRequestAttributes());

		userAnswer.setDate(new Date());
		userAnswer.setIp(ip);
		userAnswer.setLocationCity(city);
		userAnswer.setLocationCountry(country);
		userAnswer.setLocationRegion(region);
		userAnswerService.save(userAnswer);

		AddHitCreationTimeToUserAnswerController.queueTask("/tasks/addHitCreationTime", userAnswer.getHitId());

		if(callback != null) {
			return new ResponseEntity<>(callback + "(" + gson.toJson(userAnswer) +");", HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	private String getIp(RequestAttributes requestAttributes) {
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null) {
			ip = request.getHeader("X_FORWARDED_FOR");
			if (ip == null) {
				ip = request.getRemoteAddr();
			}
		}
		return ip;
	}

}
