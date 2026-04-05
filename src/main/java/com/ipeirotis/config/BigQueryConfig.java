package com.ipeirotis.config;

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.threeten.bp.Duration;

@Configuration
public class BigQueryConfig {

    @Bean
    public BigQuery bigQuery() {
        RetrySettings retrySettings = BigQueryOptions.getDefaultInstance()
                .getRetrySettings().toBuilder()
                .setTotalTimeout(Duration.ofSeconds(120))
                .setInitialRetryDelay(Duration.ofSeconds(1))
                .setRetryDelayMultiplier(2.0)
                .setMaxRetryDelay(Duration.ofSeconds(16))
                .setMaxAttempts(4)
                .build();

        return BigQueryOptions.newBuilder()
                .setRetrySettings(retrySettings)
                .build()
                .getService();
    }
}
