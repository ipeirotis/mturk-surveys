package com.ipeirotis.config;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.googlecode.objectify.ObjectifyService;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.service.MturkService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class HealthIndicatorConfig {

    private static final Logger logger = LoggerFactory.getLogger(HealthIndicatorConfig.class);

    @Bean
    public HealthIndicator datastoreHealthIndicator() {
        return () -> {
            try {
                int count = ObjectifyService.ofy().load().type(Survey.class).limit(1).count();
                return Health.up()
                        .withDetail("status", "reachable")
                        .build();
            } catch (Exception e) {
                logger.warn("Datastore health check failed", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator mturkHealthIndicator(MturkService mturkService) {
        return () -> {
            try {
                String balance = mturkService.getAccountBalance();
                return Health.up()
                        .withDetail("balance", balance)
                        .build();
            } catch (Exception e) {
                logger.warn("MTurk health check failed", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator bigqueryHealthIndicator(BigQuery bigQuery) {
        return () -> {
            try {
                Dataset dataset = bigQuery.getDataset(DatasetId.of("demographics"));
                if (dataset != null) {
                    return Health.up()
                            .withDetail("dataset", "demographics")
                            .build();
                }
                return Health.down()
                        .withDetail("error", "demographics dataset not found")
                        .build();
            } catch (BigQueryException e) {
                logger.warn("BigQuery health check failed", e);
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}
