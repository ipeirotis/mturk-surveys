package com.ipeirotis.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MetricsConfig {

    private static final Logger logger = LoggerFactory.getLogger(MetricsConfig.class);

    @Bean
    public StackdriverMeterRegistry stackdriverMeterRegistry() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isBlank()) {
            projectId = "mturk-demographics";
        }
        final String resolvedProjectId = projectId;

        StackdriverConfig config = new StackdriverConfig() {
            @Override
            public String projectId() {
                return resolvedProjectId;
            }

            @Override
            public Duration step() {
                return Duration.ofMinutes(1);
            }

            @Override
            public String get(String key) {
                return null;
            }
        };

        logger.info("Initializing Stackdriver metrics registry for project: " + resolvedProjectId);
        return StackdriverMeterRegistry.builder(config).build();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
