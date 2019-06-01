package com.ipeirotis.di;

import com.google.api.server.spi.guice.EndpointsModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyFilter;
import com.ipeirotis.endpoints.MturkEndpoint;
import com.ipeirotis.endpoints.SurveyEndpoint;
import com.ipeirotis.servlet.*;

public class MturkEndpointsModule extends EndpointsModule {
    @Override
    protected void configureServlets() {
        super.configureServlets();

        bind(ObjectifyFilter.class).in(Singleton.class);
        filter("/*").through(ObjectifyFilter.class);
        serve("/saveAnswer").with(SaveUserAnswerServlet.class);
        serve("/getAnswer").with(GetUserAnswerServlet.class);
        serve("/getBalance").with(GetAccountBalanceServlet.class);
        serve("/tasks/createHIT").with(CreateHITServlet.class);
        serve("/tasks/deleteHITs").with(DeleteHITsServlet.class);
        serve("/tasks/approveAssignments").with(ApproveAssignmentsServlet.class);
        serve("/tasks/mergeAnswers").with(MergeAnswersServlet.class);
        serve("/tasks/addHitCreationTime").with(AddHitCreationDateToUserAnswerServlet.class);

        configureEndpoints("/_ah/api/*", ImmutableList.of(MturkEndpoint.class, SurveyEndpoint.class));
    }
}
