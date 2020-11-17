package com.ipeirotis.dao;

import com.ipeirotis.entity.Question;
import com.ipeirotis.ofy.OfyBaseDao;
import org.springframework.stereotype.Service;

@Service
public class QuestionDao extends OfyBaseDao<Question>{

    protected QuestionDao() {
        super(Question.class);
    }

}
