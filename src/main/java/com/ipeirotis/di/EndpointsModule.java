package com.ipeirotis.di;

import java.util.HashSet;
import java.util.Set;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyFilter;
import com.ipeirotis.endpoints.MturkEndpoint;
import com.ipeirotis.endpoints.SurveyEndpoint;
import com.ipeirotis.servlet.AddHitCreationDateToUserAnswerServlet;
import com.ipeirotis.servlet.ApproveAssignmentsServlet;
import com.ipeirotis.servlet.CreateHITServlet;
import com.ipeirotis.servlet.DisposeHITsServlet;
import com.ipeirotis.servlet.GetUserAnswerServlet;
import com.ipeirotis.servlet.MergeAnswersServlet;
import com.ipeirotis.servlet.SaveUserAnswerServlet;

public class EndpointsModule extends GuiceSystemServiceServletModule {
    @Override
    protected void configureServlets() {
        super.configureServlets();

        bind(ObjectifyFilter.class).in(Singleton.class);
        filter("/*").through(ObjectifyFilter.class);
        serve("/saveAnswer").with(SaveUserAnswerServlet.class);
        serve("/getAnswer").with(GetUserAnswerServlet.class);
        serve("/tasks/createHIT").with(CreateHITServlet.class);
        serve("/tasks/disposeHITs").with(DisposeHITsServlet.class);
        serve("/tasks/approveAssignments").with(ApproveAssignmentsServlet.class);
        serve("/tasks/mergeAnswers").with(MergeAnswersServlet.class);
        serve("/tasks/addHitCreationTime").with(AddHitCreationDateToUserAnswerServlet.class);

        Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
        serviceClasses.add(MturkEndpoint.class);
        serviceClasses.add(SurveyEndpoint.class);

        this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
    }
}
