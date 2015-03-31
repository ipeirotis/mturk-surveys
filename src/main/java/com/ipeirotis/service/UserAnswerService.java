package com.ipeirotis.service;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.ipeirotis.dao.UserAnswerDao;
import com.ipeirotis.entity.UserAnswer;

public class UserAnswerService {

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
}