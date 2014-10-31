package com.ipeirotis.service;

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

}
