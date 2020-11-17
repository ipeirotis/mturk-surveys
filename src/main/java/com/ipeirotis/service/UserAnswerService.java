package com.ipeirotis.service;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.QueryResults;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.dao.UserAnswerDao;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.ofy.ListByCursorResult;
import com.ipeirotis.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserAnswerService {

    private static final String DEMOGRAPHICS_SURVEY_ID = "demographics";

    @Autowired
    private UserAnswerDao userAnswerDao;

    public void save(UserAnswer userAnswer) {
        userAnswerDao.save(userAnswer);
    }

    public UserAnswer get(String hitId) {
        return userAnswerDao.getByProperty("hitId", hitId);
    }

    public void save(List<UserAnswer> userAnswers) {
        userAnswerDao.save(userAnswers);
    }

    public List<UserAnswer> query(Map<String, Object> params) {
        return userAnswerDao.query(params).list();
    }

    public ListByCursorResult<UserAnswer> list(String cursorString, Integer limit) {
        List<UserAnswer> result = new ArrayList<UserAnswer>();
        Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
                .filter("surveyId", DEMOGRAPHICS_SURVEY_ID).order("-date");

        if(cursorString != null) {
            query = query.startAt(Cursor.fromUrlSafe(cursorString));
        }

        if(limit != null) {
            query = query.limit(limit);
        }

        boolean cont = false;
        QueryResults<UserAnswer> iterator = query.iterator();

        while (iterator.hasNext()) {
            UserAnswer userAnswer = iterator.next();
            String workerId = userAnswer.getWorkerId();
            if(workerId != null) {
                userAnswer.setWorkerId(MD5.crypt(workerId));
            }
            userAnswer.setIp(null);
            result.add(userAnswer);
            cont = true;
        }

        if(cont) {
            Cursor cursor = iterator.getCursorAfter();
            return new ListByCursorResult<UserAnswer>().setItems(result).setNextPageToken(cursor.toUrlSafe());
        } else {
            return new ListByCursorResult<UserAnswer>().setItems(result);
        }
    }
}