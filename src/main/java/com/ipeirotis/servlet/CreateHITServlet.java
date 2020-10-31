package com.ipeirotis.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.util.MailUtil;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CreateHITServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CreateHITServlet.class.getName());

    private SurveyService surveyService;
    private MturkService mturkService;

    @Inject
    public CreateHITServlet(MturkService mturkService, SurveyService surveyService) {
        this.mturkService = mturkService;
        this.surveyService = surveyService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String surveyId = request.getParameter("surveyId");
        boolean production = "true".equals(request.getParameter("production")) ? true : false;

        try {
            Survey survey = surveyService.get(surveyId);
            if(survey == null) {
                String error = String.format("Error creating HIT: survey %s doesn't exist", surveyId);
                logger.log(Level.SEVERE, error);
                response.sendError(404, error);
            } else {
                double balance = mturkService.getAccountBalance(production);
                // if(balance < 10.0) {
                    MailUtil.send(String.format("Your balance is too low (%.2f)", balance),
                            "mturk-surveys", "ipeirotis@gmail.com",
                            "mturk-surveys", "ipeirotis@gmail.com");
                    logger.warning(String.format("Balance is too low (%.2f)", balance));
                /*
                } else {
                    HIT hit = mturkService.createHIT(production, survey);
                    response.setContentType("text/plain");
                    String responseText = "created HIT with id: " + hit.hitId() +
                            ", preview: https://" + (production?"www":"workersandbox") + ".mturk.com/mturk/preview?groupId=" + hit.hitGroupId();
                    logger.info(responseText);
                    response.getWriter().println(responseText);
                }
                */
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating HIT", e);
            queueTask(surveyId, production);
            response.sendError(500, e.getMessage());
        }
    }

    public void queueTask(String surveyId, boolean production) {
        TaskOptions taskOptions = TaskOptions.Builder
                .withMethod(TaskOptions.Method.GET)
                .url("/tasks/createHIT")
                .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));

        taskOptions.param("surveyId", surveyId);
        taskOptions.param("production", String.valueOf(production));

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(taskOptions);
    }

}

