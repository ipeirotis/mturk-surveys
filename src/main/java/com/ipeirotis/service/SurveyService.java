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
import com.ipeirotis.entity.Survey;

public class SurveyService {
    
    private SurveyDao surveyDao;
    private QuestionDao questionDao;

    @Inject
    public SurveyService(SurveyDao surveyDao, QuestionDao questionDao) {
        this.surveyDao = surveyDao;
        this.questionDao = questionDao;
    }

    public void create(Survey survey) throws BadRequestException {
        validate(survey);

        for(Question question : survey.getQuestions()) {
            question.setSurveyId(survey.getId());
        }
        
        surveyDao.save(survey);
        questionDao.saveAll(survey.getQuestions());
    }

    public Survey get(String id) {
        Survey survey = surveyDao.get(id);
        if(survey != null) {
            List<Question> questions = questionDao.listByProperty("surveyId", id);
            survey.setQuestions(questions);
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
        if(questions == null || questions.size() == 0) {
            errors.add("at least one question is required");
        } else {
            int questionIndex = 1;
            for(Question question : questions) {
                if(StringUtils.isBlank(question.getText())) {
                    errors.add(String.format("question %d text is required", questionIndex));
                }
    
                List<Answer> answers = question.getAnswers();
                if(answers == null || answers.size() == 0) {
                    errors.add(String.format("question %d at least one answer is required", questionIndex));
                } else {
                    int answerIndex = 1;
                    for(Answer answer : answers) {
                        if(StringUtils.isBlank(answer.getText()) && answer.getSelections() == null) {
                            errors.add(String.format("question %d, answer %d text or selections are required", questionIndex, answerIndex));
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
