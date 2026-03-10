package com.ipeirotis.config;

import com.googlecode.objectify.ObjectifyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ObjectifyService.Filter> objectifyFilterRegistration() {
        FilterRegistrationBean<ObjectifyService.Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ObjectifyService.Filter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

}
