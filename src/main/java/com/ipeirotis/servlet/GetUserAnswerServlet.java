package com.ipeirotis.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GetUserAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(GetUserAnswerServlet.class.getName());

    private UserAnswerService userAnswerService;

    @Inject
    public GetUserAnswerServlet(UserAnswerService userAnswerService) {
        this.userAnswerService = userAnswerService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String callback = request.getParameter("callback");
        String workerId = request.getParameter("workerId");
        String surveyId = request.getParameter("surveyId");
        PrintWriter out = response.getWriter();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("workerId", workerId);
        params.put("surveyId", surveyId);
        List<UserAnswer> existingList = userAnswerService.query(params);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);

        for(UserAnswer userAnswer : existingList) {
            if(userAnswer.getDate().after(cal.getTime()) && callback != null) {
                response.setContentType("text/javascript");
                out.println(callback + "(" + new Gson().toJson(userAnswer) +");");
                return;
            }
        }

        if(callback != null) {
            response.setContentType("text/javascript");
            out.println(callback + "();");
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }
}

