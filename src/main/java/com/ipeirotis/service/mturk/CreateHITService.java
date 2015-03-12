package com.ipeirotis.service.mturk;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import com.ipeirotis.entity.Answer;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Selection;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.enums.AnswerType;
import com.ipeirotis.exception.MturkException;
import com.ipeirotis.mturk.model.AnswerSpecificationType;
import com.ipeirotis.mturk.model.BinaryContentType;
import com.ipeirotis.mturk.model.ContentType;
import com.ipeirotis.mturk.model.FreeTextAnswerType;
import com.ipeirotis.mturk.model.QuestionForm;
import com.ipeirotis.mturk.model.SelectionAnswerType;
import com.ipeirotis.mturk.model.Selections;
import com.ipeirotis.mturk.requester.CreateHITRequest;
import com.ipeirotis.mturk.requester.HIT;
import com.ipeirotis.mturk.requester.OperationRequest;
import com.ipeirotis.mturk.requester.Price;
import com.ipeirotis.mturk.requester.ReviewPolicy;
import com.ipeirotis.util.JAXBUtil;

public class CreateHITService extends BaseMturkService<CreateHITRequest, HIT>{

    private static final long DEFAULT_ASSIGNMENT_DURATION_IN_SECONDS = (long) 60 * 60; // 1 hour
    private static final long DEFAULT_AUTO_APPROVAL_DELAY_IN_SECONDS = (long) 60 * 60 * 24 * 15; // 15 days
    private static final long DEFAULT_LIFETIME_IN_SECONDS = (long) 60 * 60 * 24 * 3; // 3 days
    private static final long DEFAULT_FRAME_HEIGHT = 450L; // px
    private static final String CDATA_HEADER = "<![CDATA[";
    private static final String CDATA_FOOTER = "]]>";
    private static final String QUESTION_NS = "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd";
    
    @Override
    protected void run(String awsAccessKeyId, Calendar timestamp,
            String signature, String validate, String credential,
            List<CreateHITRequest> request,
            Holder<OperationRequest> operationRequest,
            Holder<List<HIT>> result) {

        getPort().createHIT(awsAccessKeyId, timestamp, signature, validate, 
                credential, request, operationRequest, result);
   }

    public HIT createHIT(Survey survey) throws MturkException {
        return this.createHIT(survey.getTitle(), survey.getDescription(), survey.getHtmlQuestion(),
                survey.getQuestions(), survey.getReward(), survey.getMaxAssignments());
    }
    
    public HIT createHIT(String title, String description, String htmlQuestion,
            List<Question> questions, Double reward, Integer maxAssignments) throws MturkException {
        return this.createHIT(
                null, // HITTypeId
                title,
                description,
                null, // keywords
                htmlQuestion,
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

    public HIT createHIT(String hitTypeId, String title, String description, String keywords, String htmlQuestion,
            List<Question> questions, Double reward, Long assignmentDurationInSeconds, Long autoApprovalDelayInSeconds, 
            Long lifetimeInSeconds, Integer maxAssignments, String requesterAnnotation,
            String uniqueRequestToken, ReviewPolicy assignmentReviewPolicy, ReviewPolicy hitReviewPolicy) throws MturkException {

        CreateHITRequest req = wrapHITParams(hitTypeId, title, description, keywords, htmlQuestion,
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
            String htmlQuestion, List<Question> questions, Double reward, Long assignmentDurationInSeconds, 
            Long autoApprovalDelayInSeconds, Long lifetimeInSeconds, Integer maxAssignments, 
            String requesterAnnotation, String uniqueRequestToken, ReviewPolicy assignmentReviewPolicy, 
            ReviewPolicy hitReviewPolicy, String hitLayoutId) {
        CreateHITRequest request = new CreateHITRequest();
        
        if (htmlQuestion != null) {
            request.setQuestion(wrapHTMLQuestions(htmlQuestion, DEFAULT_FRAME_HEIGHT));
        }
        if (questions != null && questions.size() > 0) {
            request.setQuestion(wrapQuestions(questions));
        }
        request.setLifetimeInSeconds(lifetimeInSeconds);
        request.setHITTypeId(hitTypeId);
        request.setTitle(title);
        request.setDescription(description);
        request.setKeywords(keywords);
        request.setMaxAssignments(maxAssignments);
        request.setHITReviewPolicy(hitReviewPolicy);
        request.setHITLayoutId(hitLayoutId);
        request.setRequesterAnnotation(requesterAnnotation);
        request.setAssignmentDurationInSeconds(assignmentDurationInSeconds);
        request.setAutoApprovalDelayInSeconds(autoApprovalDelayInSeconds);
        request.setAssignmentReviewPolicy(assignmentReviewPolicy);
        request.setUniqueRequestToken(uniqueRequestToken);
          
        if (reward != null) {
          Price p = new Price();
          p.setAmount(new BigDecimal(reward));
          p.setCurrencyCode("USD");
          request.setReward(p);
        }
        return request;
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

    private String wrapQuestions(List<Question> questions) {
        QuestionForm form = new QuestionForm();
        for(Question question : questions) {
            com.ipeirotis.mturk.model.Question mturkQuestion = new com.ipeirotis.mturk.model.Question();
            
            ContentType content = new ContentType();
            JAXBElement<String> contentElement = new JAXBElement<String>(new QName(QUESTION_NS, "Text"), 
                    String.class, question.getContent());
            content.getTitleOrTextOrList().add(contentElement);

            AnswerSpecificationType answerSpecificationType = new AnswerSpecificationType();
            for(Answer answer : question.getAnswers()) {
                if(answer.getType() == AnswerType.freetext) {
                    answerSpecificationType.setFreeTextAnswer(new FreeTextAnswerType());
                } else if(answer.getType() == AnswerType.selection) {
                    SelectionAnswerType selectionAnswerType = new SelectionAnswerType();
                    if(answer.getSuggestionStyle() != null) {
                        selectionAnswerType.setStyleSuggestion(answer.getSuggestionStyle().toString());
                    }
                    List<Selection> answerSelections = answer.getSelections();
                    Selections selections = new Selections();
                    if(answerSelections != null) {
                        for(Selection selection : answerSelections) {
                            com.ipeirotis.mturk.model.Selection s = new com.ipeirotis.mturk.model.Selection();
                            s.setSelectionIdentifier(selection.getIdentifier());
                            s.setText(selection.getText());
                            if(selection.getBinaryContentUrl() != null) {
                                BinaryContentType binaryContentType = new BinaryContentType();
                                binaryContentType.setDataURL(selection.getBinaryContentUrl());
                                s.setBinary(binaryContentType);
                            }
                            selections.getSelection().add(s);
                        }
                    }
                    selectionAnswerType.setSelections(selections);
                    answerSpecificationType.setSelectionAnswer(selectionAnswerType);
                }
            }

            mturkQuestion.setQuestionContent(content);
            mturkQuestion.setAnswerSpecification(answerSpecificationType);
            mturkQuestion.setIsRequired(question.isRequired());
            mturkQuestion.setQuestionIdentifier(question.getId().toString());
            mturkQuestion.setDisplayName(question.getDisplayName());

            form.getOverviewOrQuestion().add(mturkQuestion);
        }
        return JAXBUtil.marshal(form);
    }

}
