package com.ipeirotis.dao;

import com.ipeirotis.entity.Question;
import com.ipeirotis.ofy.OfyBaseDao;

public class QuestionDao extends OfyBaseDao<Question>{

    protected QuestionDao() {
        super(Question.class);
    }

}
