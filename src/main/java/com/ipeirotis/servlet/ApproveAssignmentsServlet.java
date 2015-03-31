package com.ipeirotis.servlet;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.io.IOException;
import java.util.List;
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
import com.ipeirotis.mturk.requester.Assignment;
import com.ipeirotis.service.mturk.ApproveAssignmentService;
import com.ipeirotis.service.mturk.GetAssignmentsForHITService;

@Singleton
public class ApproveAssignmentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ApproveAssignmentsServlet.class.getName());

    private ApproveAssignmentService approveAssignmentService;
    private GetAssignmentsForHITService getAssignmentsForHITService;

    @Inject
    public ApproveAssignmentsServlet(ApproveAssignmentService approveAssignmentService,
            GetAssignmentsForHITService getAssignmentsForHITService){
        this.approveAssignmentService = approveAssignmentService;
        this.getAssignmentsForHITService = getAssignmentsForHITService;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String cursor = request.getParameter("cursor");
        String sched = request.getParameter("sched");
        
        if(sched == null) {
            String nextPageToken = approve(cursor);
            if(nextPageToken != null) {
                queueTask("/approveAssignments", nextPageToken);
            }
        } else {
            queueTask("/approveAssignments", null);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }

    
    private String approve(String cursorString) {
        Query<UserAnswer> query = ofy().load().type(UserAnswer.class).limit(30);

        if (cursorString != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursorString));
        }

        boolean cont = false;
        QueryResultIterator<UserAnswer> iterator = query.iterator();

        while (iterator.hasNext()) {
            UserAnswer userAnswer = iterator.next();
            try {
                List<Assignment> assignments = getAssignmentsForHITService.getAssignments(true, userAnswer.getHitId());
                for(Assignment assignment: assignments) {
                    approveAssignmentService.approveAssignment(true, assignment.getAssignmentId());
                }
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

