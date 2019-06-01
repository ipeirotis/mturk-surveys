package com.ipeirotis.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.UserAnswerService;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

@Singleton
public class AddHitCreationDateToUserAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AddHitCreationDateToUserAnswerServlet.class.getName());

    private MturkService mturkService;
    private UserAnswerService userAnswerService;

    @Inject
    public AddHitCreationDateToUserAnswerServlet(MturkService mturkService, UserAnswerService userAnswerService){
        this.mturkService = mturkService;
        this.userAnswerService = userAnswerService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String hitId = request.getParameter("hitId");
        HIT hit = mturkService.getHIT(true, hitId);

        if(hit != null) {
            UserAnswer userAnswer = userAnswerService.get(hitId);
            userAnswer.setHitCreationDate(Date.from(hit.creationTime()));
            userAnswerService.save(userAnswer);
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

