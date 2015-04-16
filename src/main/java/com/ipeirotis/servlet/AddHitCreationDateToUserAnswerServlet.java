package com.ipeirotis.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.service.UserAnswerService;
import com.ipeirotis.service.mturk.GetHITService;

@Singleton
public class AddHitCreationDateToUserAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AddHitCreationDateToUserAnswerServlet.class.getName());

    private GetHITService getHITService;
    private UserAnswerService userAnswerService;

    @Inject
    public AddHitCreationDateToUserAnswerServlet(GetHITService getHITService,
            UserAnswerService userAnswerService){
        this.getHITService = getHITService;
        this.userAnswerService = userAnswerService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String hitId = request.getParameter("hitId");
            HIT hit = getHITService.getHIT(true, hitId);

            if(hit != null) {
                UserAnswer userAnswer = userAnswerService.get(hitId);
                userAnswer.setHitCreationDate(hit.getCreationTime().getTime());
                userAnswerService.save(userAnswer);
            }
        } catch (MturkException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void queueTask(String url, String hitId) {
        TaskOptions taskOptions = TaskOptions.Builder
                .withMethod(TaskOptions.Method.GET)
                .url(url)
                .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));

        if(hitId != null) {
            taskOptions.param("hitId", hitId);
        }

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(taskOptions);
    }

}

