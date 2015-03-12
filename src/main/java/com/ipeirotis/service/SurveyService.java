package com.ipeirotis.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.inject.Inject;
import com.ipeirotis.dao.QuestionDao;
import com.ipeirotis.dao.SurveyDao;
import com.ipeirotis.entity.Answer;
import com.ipeirotis.entity.Question;
import com.ipeirotis.entity.Selection;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.enums.AnswerType;

public class SurveyService {

    private static final String REGEX_WHITESPACE_BETWEEN_HTML = "[>]{1}\\s+[<]{1}";

    private SurveyDao surveyDao;
    private QuestionDao questionDao;

    @Inject
    public SurveyService(SurveyDao surveyDao, QuestionDao questionDao) {
        this.surveyDao = surveyDao;
        this.questionDao = questionDao;
    }

    public Survey create(Survey survey) throws BadRequestException {
        validate(survey);

        if(survey.getQuestions() != null && survey.getQuestions().size() > 0) {
            for(Question question : survey.getQuestions()) {
                question.setSurveyId(survey.getId());
            }
            questionDao.saveAll(survey.getQuestions());
        }
        if(survey.getHtmlQuestion() != null) {
            survey.setHtmlQuestion(survey.getHtmlQuestion()
                    .replaceAll(REGEX_WHITESPACE_BETWEEN_HTML, "><").replaceAll("\n", "").replaceAll("\t", ""));
        }
        
        return surveyDao.saveAndGet(survey);
    }

    public Survey get(String id) {
        Survey survey = surveyDao.get(id);
        if(survey != null) {
            List<Question> questions = questionDao.listByProperty("surveyId", id);
            if(questions != null && questions.size() > 0) {
                survey.setQuestions(questions);
            }
        }
        return survey;
    }

    public void delete(String id) throws NotFoundException {
        Survey survey = surveyDao.get(id);
        if(survey != null) {
            surveyDao.delete(survey);
            List<Question> questions = questionDao.listByProperty("surveyId", id);
            if(questions != null && questions.size() > 0) {
                questionDao.delete(questions);
            }
        } else {
            throw new NotFoundException(String.format("Survey with id=%s doesn't exist", id));
        }
    }

    private void validate(Survey survey) throws BadRequestException {
        List<String> errors = new ArrayList<String>();

        if(StringUtils.isBlank(survey.getId())) {
            errors.add("id is required");
        }

        if(StringUtils.isBlank(survey.getTitle())) {
            errors.add("title is required");
        }

        if(StringUtils.isBlank(survey.getDescription())) {
            errors.add("description is required");
        }

        if(survey.getReward() == null) {
            errors.add("reward is required");
        }
        
        if(survey.getMaxAssignments() == null) {
            errors.add("maxAssignments is required");
        }

        List<Question> questions = survey.getQuestions();
        if((questions == null || questions.size() == 0) && StringUtils.isBlank(survey.getHtmlQuestion())) {
            errors.add("questions or htmlQuestions are required");
        } else if (questions != null) {
            int questionIndex = 1;
            for(Question question : questions) {
                if(StringUtils.isBlank(question.getContent())) {
                    errors.add(String.format("question %d content is required", questionIndex));
                }

                List<Answer> answers = question.getAnswers();
                if(answers == null || answers.size() == 0) {
                    errors.add(String.format("question %d at least one answer is required", questionIndex));
                } else {
                    int answerIndex = 1;
                    for(Answer answer : answers) {
                        if(answer.getType() == null) {
                            errors.add(String.format("question %d, answer %d type is required(freetext or selection)", questionIndex, answerIndex));
                        } else if (answer.getType() == AnswerType.selection) {
                            if (answer.getSelections() == null || answer.getSelections().size() == 0) {
                                   errors.add(String.format("question %d, answer %d selections are required because answer type is 'selection'", 
                                           questionIndex, answerIndex));
                            } else {
                                int selectionIndex = 1;
                                for(Selection selection : answer.getSelections()) {
                                    if(selection.getIdentifier() == null){
                                        errors.add(String.format("question %d, answer %d, selection %d identifier is required", 
                                                questionIndex, answerIndex, selectionIndex));
                                    }
                                    selectionIndex++;
                                }
                            }
                        }
                        answerIndex++;
                    }
                }
                questionIndex++;
            }
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(
                    String.format("Error saving survey: %s", StringUtils.join(errors, ", ")));
        }
    }
}
