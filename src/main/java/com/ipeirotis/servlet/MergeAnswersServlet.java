package com.ipeirotis.servlet;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Singleton;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;

@Singleton
public class MergeAnswersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MergeAnswersServlet.class.getName());

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cursor = request.getParameter("cursor");
        String sched = request.getParameter("sched");
        
        if(sched == null) {
            String nextPageToken = merge(cursor);
            if(nextPageToken != null) {
                queueTask("/tasks/mergeAnswers", nextPageToken);
            }
        } else {
            queueTask("/tasks/mergeAnswers", null);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }

    
    private String merge(String cursorString) {
        Query<UserAnswer> query = ofy().load().type(UserAnswer.class).limit(1000);
        List<UserAnswer> toSaveList = new ArrayList<UserAnswer>(); 

        if (cursorString != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursorString));
        }

        boolean cont = false;
        QueryResultIterator<UserAnswer> iterator = query.iterator();

        while (iterator.hasNext()) {
            UserAnswer userAnswer = iterator.next();
            Map<String, String> answers = userAnswer.getAnswers();
            if(answers != null) {
                String answer = answers.get("householdIncome");
                if("$100,000-$149,999".equals(answer) || "$150,000-$199,999".equals(answer) ||
                        "$200,000-$249,999".equals(answer) || "$300,000 or more".equals(answer)) {
                    answers.put("householdIncome", "$100,000 or more");
                    toSaveList.add(userAnswer);
                }
            }
            cont = true;
        }

        if(toSaveList.size() > 0) {
            ofy().save().entities(toSaveList).now();
            logger.info(String.format("Merged %d answers", toSaveList.size()));
        }
        
        if(cont) {
            Cursor cursor = iterator.getCursor();
            return cursor.toWebSafeString();
        } else {
            return null;
        }
    }

    public void queueTask(String url, String cursorStr) {
        TaskOptions taskOptions = TaskOptions.Builder
                .withMethod(TaskOptions.Method.GET)
                .url(url)
                .retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));

        if(cursorStr != null) {
            taskOptions.param("cursor", cursorStr);
        }

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(taskOptions);
    }

}

