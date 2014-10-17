package com.ipeirotis.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.ipeirotis.dao.QuestionDao;
import com.ipeirotis.dao.SurveyDao;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.mturk.CreateHITService;
import com.ipeirotis.service.mturk.GetAccountBalanceService;
import com.ipeirotis.service.mturk.GetHITService;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(QuestionDao.class).in(Singleton.class);
        bind(SurveyDao.class).in(Singleton.class);

        bind(SurveyService.class).in(Singleton.class);

        // mturk services
        bind(CreateHITService.class).in(Singleton.class);
        bind(GetHITService.class).in(Singleton.class);
        bind(GetAccountBalanceService.class).in(Singleton.class);
    }

}