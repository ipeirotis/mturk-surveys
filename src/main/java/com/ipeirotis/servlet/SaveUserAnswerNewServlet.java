package com.ipeirotis.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.UserAnswerService;

@Singleton
public class SaveUserAnswerNewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SaveUserAnswerNewServlet.class.getName());

    private UserAnswerService userAnswerService;

    @Inject
    public SaveUserAnswerNewServlet(UserAnswerService userAnswerService) {
        this.userAnswerService = userAnswerService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Gson gson = new Gson();
        UserAnswer userAnswer = gson.fromJson(request.getParameter("userAnswer"), UserAnswer.class);
        if(userAnswer == null) {
            response.sendError(400);
        }
        
        String country = request.getHeader("X-AppEngine-Country");
        String region = request.getHeader("X-AppEngine-Region");
        String city = request.getHeader("X-AppEngine-City");
        String callback = request.getParameter("callback");
        String ip = getIp(request);

        userAnswer.setDate(new Date());
        userAnswer.setIp(ip);
        userAnswer.setLocationCity(city);
        userAnswer.setLocationCountry(country);
        userAnswer.setLocationRegion(region);
        userAnswerService.save(userAnswer);
        
        PrintWriter out = response.getWriter();
        String responseObject = gson.toJson(userAnswer);

        if(callback != null) {
            response.setContentType("text/javascript");
            out.println(callback + "(" + responseObject + ");");
        } else {
            response.setContentType("application/json");
            out.println(responseObject);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    private String getIp(HttpServletRequest request) {
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

