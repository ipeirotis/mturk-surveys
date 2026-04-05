package com.ipeirotis.service;

import com.ipeirotis.entity.Survey;
import com.ipeirotis.util.SafeDecimalFormat;
import jakarta.annotation.PreDestroy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.mturk.MTurkClient;
import software.amazon.awssdk.services.mturk.model.*;

import java.net.URI;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import software.amazon.awssdk.regions.Region;

@Service
public class MturkService {

    private static final Logger logger = Logger.getLogger(MturkService.class.getName());

    private static NumberFormat numberFormat = SafeDecimalFormat.forPattern("#0.00");

    private static final String SANDBOX_ENDPOINT = "https://mturk-requester-sandbox.us-east-1.amazonaws.com";
    private static final long DEFAULT_ASSIGNMENT_DURATION_IN_SECONDS = (long) 60 * 60; // 1 hour
    private static final long DEFAULT_AUTO_APPROVAL_DELAY_IN_SECONDS = (long) 60; // 60 sec
    private static final long DEFAULT_LIFETIME_IN_SECONDS = (long) 60 * 60 * 24 * 3; // 3 days
    private static final long DEFAULT_FRAME_HEIGHT = 450L; // px
    private static final String CDATA_HEADER = "<![CDATA[";
    private static final String CDATA_FOOTER = "]]>";

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration API_CALL_TIMEOUT = Duration.ofSeconds(30);
    private static final Region DEFAULT_REGION = Region.US_EAST_1;

    private final MTurkClient productionClient;
    private final MTurkClient sandboxClient;

    public MturkService(AwsCredentialsProvider awsCredentialsProvider) {
        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(API_CALL_TIMEOUT)
                .apiCallAttemptTimeout(READ_TIMEOUT)
                .build();

        this.productionClient = MTurkClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(DEFAULT_REGION)
                .overrideConfiguration(overrideConfig)
                .build();

        this.sandboxClient = MTurkClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(DEFAULT_REGION)
                .endpointOverride(URI.create(SANDBOX_ENDPOINT))
                .overrideConfiguration(overrideConfig)
                .build();
    }

    @PreDestroy
    public void close() {
        try { productionClient.close(); } catch (Exception e) { /* ignore */ }
        try { sandboxClient.close(); } catch (Exception e) { /* ignore */ }
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public HIT getHIT(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        GetHitRequest.Builder requestBuilder = GetHitRequest.builder().hitId(hitId);
        GetHitResponse response = client.getHIT(requestBuilder.build());
        return response.hit();
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public DeleteHitResponse deleteHIT(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        DeleteHitRequest.Builder requestBuilder = DeleteHitRequest.builder().hitId(hitId);
        return client.deleteHIT(requestBuilder.build());
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<Assignment> listAssignmentsForHit(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        ListAssignmentsForHitRequest.Builder requestBuilder = ListAssignmentsForHitRequest.builder().hitId(hitId);
        ListAssignmentsForHitResponse response = client.listAssignmentsForHIT(requestBuilder.build());
        return response.assignments();
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void approveAssignment(Boolean production, String assignmentId) {
        MTurkClient client = getClient(production);
        ApproveAssignmentRequest.Builder requestBuilder = ApproveAssignmentRequest.builder().assignmentId(assignmentId);

        client.approveAssignment(requestBuilder.build());
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<HIT> listHits(Boolean production) {
        MTurkClient client = getClient(production);
        ListHiTsRequest.Builder requestBuilder = ListHiTsRequest.builder().maxResults(100);
        ListHiTsResponse response = client.listHITs(requestBuilder.build());
        return response.hiTs();
    }

    @Retryable(retryFor = {SdkClientException.class, SdkServiceException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public HIT createHIT(Boolean production, Survey survey, String idempotencyToken) {
        MTurkClient client = getClient(production);
        CreateHitRequest.Builder requestBuilder = CreateHitRequest.builder();
        requestBuilder.title(survey.getTitle());
        requestBuilder.description(survey.getDescription());
        requestBuilder.question(survey.getHtmlQuestion());
        requestBuilder.reward(String.valueOf(survey.getReward()));
        requestBuilder.maxAssignments(survey.getMaxAssignments());
        requestBuilder.assignmentDurationInSeconds(DEFAULT_ASSIGNMENT_DURATION_IN_SECONDS);
        requestBuilder.autoApprovalDelayInSeconds(DEFAULT_AUTO_APPROVAL_DELAY_IN_SECONDS);
        requestBuilder.lifetimeInSeconds(DEFAULT_LIFETIME_IN_SECONDS);

        // Idempotency token: caller-supplied stable token to prevent duplicate HITs on retries
        requestBuilder.uniqueRequestToken(idempotencyToken);

        if (survey.getHtmlQuestion() != null) {
            requestBuilder.question(wrapHTMLQuestions(survey.getHtmlQuestion(), DEFAULT_FRAME_HEIGHT));
        }

        if (survey.getReward() != null) {
            requestBuilder.reward(numberFormat.format(survey.getReward()));
        }

        CreateHitResponse response = client.createHIT(requestBuilder.build());
        return response.hit();
    }

    private MTurkClient getClient(Boolean production) {
        return (production != null && production) ? productionClient : sandboxClient;
    }

    private String wrapHTMLQuestions(String html, long frameHeight) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\"><HTMLContent>"
                + CDATA_HEADER
                + html
                + CDATA_FOOTER
                + "</HTMLContent><FrameHeight>"
                + frameHeight
                + "</FrameHeight></HTMLQuestion>";
    }

}
