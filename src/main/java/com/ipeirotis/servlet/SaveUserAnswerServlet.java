package com.ipeirotis.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.UserAnswerService;

@Singleton
public class SaveUserAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private UserAnswerService userAnswerService;

    @Inject
    public SaveUserAnswerServlet(UserAnswerService userAnswerService) {
        this.userAnswerService = userAnswerService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String country = request.getHeader("X-AppEngine-Country");
        String region = request.getHeader("X-AppEngine-Region");
        String city = request.getHeader("X-AppEngine-City");
        String callback = request.getParameter("callback");
        String answer = request.getParameter("answer");
        String hitId = request.getParameter("hitId");
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = request.getHeader("X_FORWARDED_FOR");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
        }

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAnswer(answer);
        userAnswer.setDate(new Date());
        userAnswer.setIp(ip);
        userAnswer.setHitId(hitId);
        userAnswer.setLocationCity(city);
        userAnswer.setLocationCountry(country);
        userAnswer.setLocationRegion(region);
        userAnswerService.save(userAnswer);
        
        PrintWriter out = response.getWriter();
        String responseObject = new Gson().toJson(userAnswer);
        
        if(callback != null) {
            response.setContentType("text/javascript");
            out.println(callback + "(" + responseObject + ");");
        } else {
            response.setContentType("application/json");
            out.println(responseObject);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }
}

