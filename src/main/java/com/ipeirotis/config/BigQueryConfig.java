package com.ipeirotis.config;

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BigQueryConfig {

    @Bean
    public BigQuery bigQuery() {
        RetrySettings defaults = BigQueryOptions.getDefaultInstance().getRetrySettings();
        RetrySettings retrySettings = defaults.toBuilder()
                .setMaxAttempts(4)
                .setRetryDelayMultiplier(2.0)
                .build();

        return BigQueryOptions.newBuilder()
                .setRetrySettings(retrySettings)
                .build()
                .getService();
    }
}
