package com.ipeirotis.dao;

import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.ofy.OfyBaseDao;

public class UserAnswerDao extends OfyBaseDao<UserAnswer>{

    protected UserAnswerDao() {
        super(UserAnswer.class);
    }

}
