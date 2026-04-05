package com.ipeirotis.config;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.googlecode.objectify.ObjectifyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(FilterConfig.class);

    private static final String ADMIN_KEY_SECRET = "task-admin-key";

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilterRegistration() {
        FilterRegistrationBean<RequestCorrelationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestCorrelationFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ObjectifyService.Filter> objectifyFilterRegistration() {
        FilterRegistrationBean<ObjectifyService.Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ObjectifyService.Filter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TaskAuthFilter> taskAuthFilterRegistration() {
        String adminKey = loadAdminKey();
        FilterRegistrationBean<TaskAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TaskAuthFilter(adminKey));
        registration.addUrlPatterns("/tasks/*");
        registration.setOrder(2);
        return registration;
    }

    /**
     * Load the task admin key from GCP Secret Manager, falling back to
     * the TASK_ADMIN_KEY env var for local development.
     */
    private String loadAdminKey() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId != null) {
            try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
                SecretVersionName name = SecretVersionName.of(projectId, ADMIN_KEY_SECRET, "latest");
                AccessSecretVersionResponse response = client.accessSecretVersion(name);
                String key = response.getPayload().getData().toStringUtf8();
                logger.info("Task admin key loaded from GCP Secret Manager");
                return key;
            } catch (Exception e) {
                logger.warn(
                        "Failed to load task admin key from Secret Manager, falling back to env var", e);
            }
        }
        String envKey = System.getenv("TASK_ADMIN_KEY");
        if (envKey != null && !envKey.isBlank()) {
            logger.info("Task admin key loaded from TASK_ADMIN_KEY env var");
        }
        return envKey;
    }

}
