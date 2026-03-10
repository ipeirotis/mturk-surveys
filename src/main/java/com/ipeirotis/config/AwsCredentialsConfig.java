package com.ipeirotis.config;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class AwsCredentialsConfig {

    private static final Logger logger = Logger.getLogger(AwsCredentialsConfig.class.getName());

    private static final String ACCESS_KEY_SECRET = "aws-access-key-id";
    private static final String SECRET_KEY_SECRET = "aws-secret-access-key";

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId != null) {
            try {
                String accessKey = getSecret(projectId, ACCESS_KEY_SECRET);
                String secretKey = getSecret(projectId, SECRET_KEY_SECRET);
                if (accessKey != null && secretKey != null) {
                    logger.info("AWS credentials loaded from GCP Secret Manager");
                    return StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey));
                }
            } catch (Exception e) {
                logger.log(Level.WARNING,
                        "Failed to load AWS credentials from Secret Manager, falling back to default provider chain", e);
            }
        }
        logger.info("Using default AWS credentials provider chain (env vars / profile)");
        return DefaultCredentialsProvider.create();
    }

    private String getSecret(String projectId, String secretId) throws Exception {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName name = SecretVersionName.of(projectId, secretId, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(name);
            return response.getPayload().getData().toStringUtf8();
        }
    }
}
