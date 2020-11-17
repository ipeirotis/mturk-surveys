package com.ipeirotis;

import com.google.appengine.api.utils.SystemProperty;
import com.google.cloud.datastore.DatastoreOptions;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.HashSet;
import java.util.Set;

public class ServletInitializer extends SpringBootServletInitializer {

    private static final boolean isDev = SystemProperty.environment.value() != SystemProperty.Environment.Value.Production;
    private static final Set<Class> classes;

    static {
        classes = new HashSet<>();
        classes.add(Question.class);
        classes.add(Survey.class);
        classes.add(UserAnswer.class);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        if(isDev) {
            ObjectifyService.init(new ObjectifyFactory(
                    DatastoreOptions.newBuilder()
                            .setHost("http://localhost:8484")
                            .setProjectId("mturk-demographics")
                            .build()
                            .getService()
            ));
        } else {
            ObjectifyService.init();
        }

        for (Class clazz : classes) {
            ObjectifyService.register(clazz);
        }

        return application.sources(MturkSurveysApplication.class);
    }

}
