package com.ipeirotis.servlet;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.service.mturk.DisposeHITService;

@Singleton
public class DisposeHITsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DisposeHITsServlet.class.getName());

    private DisposeHITService disposeHITService;

    @Inject
    public DisposeHITsServlet(DisposeHITService disposeHITService) {
        this.disposeHITService = disposeHITService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cursor = request.getParameter("cursor");
        String sched = request.getParameter("sched");

        if(sched == null) {
            String nextPageToken = dispose(cursor);
            if(nextPageToken != null) {
                queueTask("/tasks/disposeHITs", nextPageToken);
            }
        } else {
            queueTask("/tasks/disposeHITs", null);
        }
    }

    private String dispose(String cursorString) {
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(new Date());
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(endCal.getTime());
        startCal.add(Calendar.DAY_OF_MONTH, -1);

        Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
                .filter("date >=", startCal.getTime()).filter("date <", endCal.getTime()).limit(30);

        if (cursorString != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursorString));
        }

        boolean cont = false;
        QueryResultIterator<UserAnswer> iterator = query.iterator();

        while (iterator.hasNext()) {
            UserAnswer userAnswer = iterator.next();
            try {
                disposeHITService.disposeHIT(true, userAnswer.getHitId());
                logger.log(Level.INFO, String.format("Disposed HIT %s", userAnswer.getHitId()));
            } catch (MturkException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
            cont = true;
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

