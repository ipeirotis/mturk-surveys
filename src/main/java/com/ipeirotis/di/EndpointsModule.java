package com.ipeirotis.di;

import java.util.HashSet;
import java.util.Set;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyFilter;
import com.ipeirotis.endpoints.MturkEndpoint;
import com.ipeirotis.endpoints.SurveyEndpoint;
import com.ipeirotis.servlet.CreateSurveyServlet;

public class EndpointsModule extends GuiceSystemServiceServletModule {
    @Override
    protected void configureServlets() {
        super.configureServlets();

        bind(ObjectifyFilter.class).in(Singleton.class);
        filter("/*").through(ObjectifyFilter.class);
        serve("/createSurvey").with(CreateSurveyServlet.class);

        Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
        serviceClasses.add(MturkEndpoint.class);
        serviceClasses.add(SurveyEndpoint.class);

        this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
    }
}
