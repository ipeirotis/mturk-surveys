package com.ipeirotis.di;

import java.util.HashSet;
import java.util.Set;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyFilter;
import com.ipeirotis.endpoints.MturkEndpoint;
import com.ipeirotis.endpoints.SurveyEndpoint;
import com.ipeirotis.servlet.ApproveAssignmentsServlet;
import com.ipeirotis.servlet.CreateHITServlet;
import com.ipeirotis.servlet.GetUserAnswerServlet;
import com.ipeirotis.servlet.MergeAnswersServlet;
import com.ipeirotis.servlet.SaveUserAnswerServlet;

public class EndpointsModule extends GuiceSystemServiceServletModule {
    @Override
    protected void configureServlets() {
        super.configureServlets();

        bind(ObjectifyFilter.class).in(Singleton.class);
        filter("/*").through(ObjectifyFilter.class);
        serve("/createHIT").with(CreateHITServlet.class);
        serve("/saveAnswer").with(SaveUserAnswerServlet.class);
        serve("/getAnswer").with(GetUserAnswerServlet.class);
        serve("/tasks/approveAssignments").with(ApproveAssignmentsServlet.class);
        serve("/tasks/mergeAnswers").with(MergeAnswersServlet.class);

        Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
        serviceClasses.add(MturkEndpoint.class);
        serviceClasses.add(SurveyEndpoint.class);

        this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
    }
}
