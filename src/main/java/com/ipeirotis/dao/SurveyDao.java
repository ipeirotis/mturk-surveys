package com.ipeirotis.dao;

import com.ipeirotis.entity.Survey;
import com.ipeirotis.ofy.OfyBaseDao;

public class SurveyDao extends OfyBaseDao<Survey>{

    protected SurveyDao() {
        super(Survey.class);
    }

}
