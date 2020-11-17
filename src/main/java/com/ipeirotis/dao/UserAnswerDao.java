package com.ipeirotis.dao;

import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.ofy.OfyBaseDao;
import org.springframework.stereotype.Service;

@Service
public class UserAnswerDao extends OfyBaseDao<UserAnswer>{

    protected UserAnswerDao() {
        super(UserAnswer.class);
    }

}
