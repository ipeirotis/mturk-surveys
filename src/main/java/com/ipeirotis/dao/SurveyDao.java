package com.ipeirotis.dao;

import com.ipeirotis.entity.Survey;
import com.ipeirotis.ofy.OfyBaseDao;
import org.springframework.stereotype.Service;

@Service
public class SurveyDao extends OfyBaseDao<Survey>{

    protected SurveyDao() {
        super(Survey.class);
    }

}
