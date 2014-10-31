package com.ipeirotis.ofy;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;

public class OfyService {
	static {
        register(Question.class);
        register(Survey.class);
        register(UserAnswer.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
    
    public static void register(Class<?> clazz){
    	factory().register(clazz);
    }
}
