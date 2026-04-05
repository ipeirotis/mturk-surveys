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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    /**
     * Find an existing answer by workerId and hitId.
     * Returns the first match, or null if none exists.
     */
    public UserAnswer findByWorkerAndHit(String workerId, String hitId) {
        return ofy().load().type(UserAnswer.class)
                .filter("workerId", workerId)
                .filter("hitId", hitId)
                .first().now();
    }

    public void save(List<UserAnswer> userAnswers) {
        userAnswerDao.save(userAnswers);
    }

    public List<UserAnswer> query(Map<String, Object> params) {
        return userAnswerDao.query(params).list();
    }

    public ListByCursorResult<UserAnswer> list(String cursorString, Integer limit, Date from, Date to) {
        List<UserAnswer> result = new ArrayList<UserAnswer>();
        Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
                .filter("surveyId", DEMOGRAPHICS_SURVEY_ID).order("-date");

        if (from != null) {
            query = query.filter("date >=", from);
        }
        if (to != null) {
            query = query.filter("date <", to);
        }

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
            applyPrivacyTransforms(userAnswer);
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

    /**
     * List demographics answers by date range. Uses the (date ASC) index.
     * Results have privacy transforms applied (MD5-hashed workerId, stripped IP).
     * Fetches in chunks of 500 to limit memory usage.
     */
    public List<UserAnswer> listByDateRange(Date from, Date to) {
        List<UserAnswer> result = new ArrayList<>();
        iterateByDateRange(from, to, ua -> {
            applyPrivacyTransforms(ua);
            result.add(ua);
        });
        return result;
    }

    /**
     * Iterate over demographics answers by date range in chunks of 500,
     * calling the consumer for each entity. This avoids loading the entire
     * result set into memory at once.
     */
    public void iterateByDateRange(Date from, Date to, Consumer<UserAnswer> consumer) {
        final int chunkSize = 500;
        Cursor cursor = null;
        boolean hasMore = true;

        while (hasMore) {
            Query<UserAnswer> query = ofy().load().type(UserAnswer.class)
                    .filter("surveyId", DEMOGRAPHICS_SURVEY_ID)
                    .filter("date >=", from)
                    .filter("date <", to)
                    .order("date")
                    .limit(chunkSize);

            if (cursor != null) {
                query = query.startAt(cursor);
            }

            QueryResults<UserAnswer> iterator = query.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                consumer.accept(iterator.next());
                count++;
            }

            if (count < chunkSize) {
                hasMore = false;
            } else {
                cursor = iterator.getCursorAfter();
            }
        }
    }

    private void applyPrivacyTransforms(UserAnswer userAnswer) {
        String workerId = userAnswer.getWorkerId();
        if (workerId != null) {
            userAnswer.setWorkerId(MD5.crypt(workerId));
        }
        userAnswer.setIp(null);
    }
}
