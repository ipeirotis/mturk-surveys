package com.ipeirotis;

import com.googlecode.objectify.ObjectifyService;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class MturkSurveysApplication {

    private static final Set<Class> classes;

    static {
        classes = new HashSet<>();
        classes.add(Question.class);
        classes.add(Survey.class);
        classes.add(UserAnswer.class);
    }

	public static void main(String[] args) {
        ObjectifyService.init();

        for (Class clazz : classes) {
            ObjectifyService.register(clazz);
		}
		
		SpringApplication.run(MturkSurveysApplication.class, args);
	}

}
