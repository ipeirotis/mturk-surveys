package com.ipeirotis.service;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.inject.Inject;
import com.googlecode.objectify.cmd.Query;
import com.ipeirotis.dao.UserAnswerDao;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.MD5;

public class UserAnswerService {

    private static final String DEMOGRAPHICS_SURVEY_ID = "demographics";

    private UserAnswerDao userAnswerDao;

    @Inject
    public UserAnswerService(UserAnswerDao userAnswerDao) {
        this.userAnswerDao = userAnswerDao;
    }

    public void save(UserAnswer userAnswer) {
        userAnswerDao.save(userAnswer);
    }

    public void save(List<UserAnswer> userAnswers) {
        userAnswerDao.save(userAnswers);
    }

    public List<UserAnswer> query(Map<String, Object> params) {
        return userAnswerDao.query(params).list();
    }

    public CollectionResponse<UserAnswer> list(String cursorString, Integer limit) throws NoSuchAlgorithmException {
        List<UserAnswer> result = new ArrayList<UserAnswer>();
        Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
                .filter("surveyId", DEMOGRAPHICS_SURVEY_ID).order("-date");

        if(cursorString != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursorString));
        }

        if(limit != null) {
            query = query.limit(limit);
        }

        boolean cont = false;
        QueryResultIterator<UserAnswer> iterator = query.iterator();

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
            Cursor cursor = iterator.getCursor();
            return CollectionResponse.<UserAnswer> builder().setItems(result).setNextPageToken(cursor.toWebSafeString()).build();
        } else {
            return CollectionResponse.<UserAnswer> builder().setItems(result).build();
        }
    }
}