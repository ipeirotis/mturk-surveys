package com.ipeirotis.service.mturk;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import com.google.inject.Inject;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.model.AnswerSpecificationType;
import com.ipeirotis.mturk.model.ContentType;
import com.ipeirotis.mturk.model.FreeTextAnswerType;
import com.ipeirotis.mturk.model.QuestionForm;
import com.ipeirotis.mturk.requester.CreateHITRequest;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.mturk.requester.OperationRequest;
import com.ipeirotis.mturk.requester.Price;
import com.ipeirotis.mturk.requester.ReviewPolicy;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.util.JAXBUtil;

public class CreateHITService extends BaseMturkService<CreateHITRequest, HIT>{

    private static final long DEFAULT_ASSIGNMENT_DURATION_IN_SECONDS = (long) 60 * 60; // 1 hour
    private static final long DEFAULT_AUTO_APPROVAL_DELAY_IN_SECONDS = (long) 60 * 60 * 24 * 15; // 15 days
    private static final long DEFAULT_LIFETIME_IN_SECONDS = (long) 60 * 60 * 24 * 3; // 3 days
    private static final String QUESTION_NS = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd";

    private SurveyService surveyService;

    @Inject
    public CreateHITService(SurveyService surveyService) {
        this.surveyService = surveyService;
    }
    
    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<CreateHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<HIT>> result) {

        getPort().createHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
    }

    public HIT createHIT(String surveyId) throws Exception {
        Survey survey = surveyService.get(surveyId);
        List<Question> questions = ofy().load().type(Question.class).filter("surveyId", surveyId).list();
        return this.createHIT(survey.getTitle(), survey.getDescription(), 
                questions, survey.getReward(), survey.getMaxAssignments());
    }
    
    public HIT createHIT(String title, String description, 
            List<Question> questions, Double reward, Integer maxAssignments) throws Exception {
        return this.createHIT(
                null, // HITTypeId
                title, 
                description, // description
                null, // keywords 
                questions, 
                reward,
                DEFAULT_ASSIGNMENT_DURATION_IN_SECONDS,
                DEFAULT_AUTO_APPROVAL_DELAY_IN_SECONDS,
                DEFAULT_LIFETIME_IN_SECONDS,
                maxAssignments,
                null, // requesterAnnotation
                null, // uniqueRequesterToken
                null, // assignmentReviewPolicy
                null  // hitReviewPolicy
            );
    }

    public HIT createHIT(String hitTypeId, String title, String description, String keywords, 
            List<Question> questions, Double reward, Long assignmentDurationInSeconds, Long autoApprovalDelayInSeconds, 
            Long lifetimeInSeconds, Integer maxAssignments, String requesterAnnotation,
            String uniqueRequestToken, ReviewPolicy assignmentReviewPolicy, ReviewPolicy hitReviewPolicy) throws Exception {

        CreateHITRequest req = wrapHITParams(hitTypeId, title, description, keywords,
                questions, reward, assignmentDurationInSeconds, autoApprovalDelayInSeconds,
                lifetimeInSeconds, maxAssignments, requesterAnnotation, uniqueRequestToken,
                assignmentReviewPolicy, hitReviewPolicy, null);
        Holder<List<HIT>> result = this.request("CreateHIT", req);

        if(result.value != null && result.value.size() != 0) {
            return result.value.get(0);
        } else {
            throw new MturkException("Unknown server error");
        }
    }

    private CreateHITRequest wrapHITParams(String hitTypeId, String title, String description, String keywords, 
            List<Question> questions, Double reward, Long assignmentDurationInSeconds, Long autoApprovalDelayInSeconds, 
            Long lifetimeInSeconds, Integer maxAssignments, String requesterAnnotation,
            String uniqueRequestToken, ReviewPolicy assignmentReviewPolicy, ReviewPolicy hitReviewPolicy,
            String hitLayoutId) {
        CreateHITRequest request = new CreateHITRequest();
        
        if (questions != null)        request.setQuestion(wrapQuestions(questions));
        if (lifetimeInSeconds != null)request.setLifetimeInSeconds(lifetimeInSeconds);
        if (hitTypeId != null)        request.setHITTypeId(hitTypeId);
        if (title != null)            request.setTitle(title);
        if (description != null)      request.setDescription(description);
        if (keywords != null)         request.setKeywords(keywords);
        if (maxAssignments != null)   request.setMaxAssignments(maxAssignments);
        if (hitReviewPolicy != null)  request.setHITReviewPolicy(hitReviewPolicy);
        if (hitLayoutId != null)      request.setHITLayoutId(hitLayoutId);
        if (requesterAnnotation != null)        request.setRequesterAnnotation(requesterAnnotation);
        if (assignmentDurationInSeconds != null)request.setAssignmentDurationInSeconds(assignmentDurationInSeconds);
        if (autoApprovalDelayInSeconds != null) request.setAutoApprovalDelayInSeconds(autoApprovalDelayInSeconds);
        if (assignmentReviewPolicy != null)     request.setAssignmentReviewPolicy(assignmentReviewPolicy);
        if (uniqueRequestToken != null)         request.setUniqueRequestToken(uniqueRequestToken);
          
        if (reward != null) {
          Price p = new Price();
          p.setAmount(new BigDecimal(reward));
          p.setCurrencyCode("USD");
          request.setReward(p);
        }
        return request;
    }

    private String wrapQuestions(List<Question> questions) {
        QuestionForm form = new QuestionForm();
        for(Question question : questions) {
            com.ipeirotis.mturk.model.Question mturkQuestion = new com.ipeirotis.mturk.model.Question();
            
            ContentType content = new ContentType();
            JAXBElement<String> contentElement = new JAXBElement<String>(new QName(QUESTION_NS, "Text"), 
                    String.class, question.getText());
            content.getTitleOrTextOrList().add(contentElement);

            FreeTextAnswerType freeTextAnswerType = new FreeTextAnswerType();
            freeTextAnswerType.setDefaultText(question.getText());

            AnswerSpecificationType answerSpecificationType = new AnswerSpecificationType();
            answerSpecificationType.setFreeTextAnswer(freeTextAnswerType);

            mturkQuestion.setQuestionContent(content);
            mturkQuestion.setAnswerSpecification(answerSpecificationType);
            mturkQuestion.setIsRequired(question.isRequired());
            mturkQuestion.setQuestionIdentifier(question.getId().toString());
            mturkQuestion.setDisplayName(question.getDisplayName());

            form.getOverviewOrQuestion().add(mturkQuestion);
        }System.out.println(JAXBUtil.marshal(form));
        return JAXBUtil.marshal(form);
    }

}
