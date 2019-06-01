package com.ipeirotis.di;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.ipeirotis.dao.QuestionDao;
import com.ipeirotis.dao.SurveyDao;
import com.ipeirotis.dao.UserAnswerDao;
import com.ipeirotis.service.MturkService;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.UserAnswerService;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(QuestionDao.class).in(Singleton.class);
        bind(SurveyDao.class).in(Singleton.class);
        bind(UserAnswerDao.class).in(Singleton.class);

        bind(SurveyService.class).in(Singleton.class);
        bind(UserAnswerService.class).in(Singleton.class);

        bind(MturkService.class).in(Singleton.class);
    }

}