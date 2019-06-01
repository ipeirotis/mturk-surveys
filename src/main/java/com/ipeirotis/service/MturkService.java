package com.ipeirotis.service;

import com.ipeirotis.entity.Survey;
import com.ipeirotis.util.SafeDecimalFormat;
import software.amazon.awssdk.services.mturk.MTurkClient;
import software.amazon.awssdk.services.mturk.MTurkClientBuilder;
import software.amazon.awssdk.services.mturk.model.*;

import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public double getAccountBalance(Boolean production) {
        MTurkClient client = getClient(production);
        GetAccountBalanceRequest.Builder requestBuilder = GetAccountBalanceRequest.builder();
        GetAccountBalanceResponse response = client.getAccountBalance(requestBuilder.build());

        try {
            return numberFormat.parse(response.availableBalance()).doubleValue();
        } catch (ParseException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return 0;
        }
    }

    public HIT getHIT(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        GetHitRequest.Builder requestBuilder = GetHitRequest.builder().hitId(hitId);
        GetHitResponse response = client.getHIT(requestBuilder.build());
        return response.hit();
    }

    public void deleteHIT(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        DeleteHitRequest.Builder requestBuilder = DeleteHitRequest.builder().hitId(hitId);
        client.deleteHIT(requestBuilder.build());
    }

    public List<Assignment> listAssignmentsForHit(Boolean production, String hitId) {
        MTurkClient client = getClient(production);
        ListAssignmentsForHitRequest.Builder requestBuilder = ListAssignmentsForHitRequest.builder().hitId(hitId);
        ListAssignmentsForHitResponse response = client.listAssignmentsForHIT(requestBuilder.build());
        return response.assignments();
    }

    public void approveAssignment(Boolean production, String assignmentId) {
        MTurkClient client = getClient(production);
        ApproveAssignmentRequest.Builder requestBuilder = ApproveAssignmentRequest.builder().assignmentId(assignmentId);

        client.approveAssignment(requestBuilder.build());
    }

    public List<HIT> listHits(Boolean production) {
        MTurkClient client = getClient(production);
        ListHiTsRequest.Builder requestBuilder = ListHiTsRequest.builder().maxResults(100);
        ListHiTsResponse response = client.listHITs(requestBuilder.build());
        return response.hiTs();
    }

    public HIT createHIT(Boolean production, Survey survey) {
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
        MTurkClientBuilder builder = MTurkClient.builder();
        if(production == null || !production) {
            builder.endpointOverride(URI.create(SANDBOX_ENDPOINT));
        }
        return builder.build();
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