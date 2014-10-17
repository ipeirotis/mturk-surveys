package com.ipeirotis.entity;

import java.util.List;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.ipeirotis.entity.enums.QuestionContentType;

@Entity
@Cache
public class Question {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;
    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String surveyId;
    private boolean isRequired = false;
    private String displayName;
    private String text;
    private QuestionContentType contentType;
    private List<Answer> answers;

    public Question(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public QuestionContentType getContentType() {
        return contentType;
    }

    public void setContentType(QuestionContentType contentType) {
        this.contentType = contentType;
    }

}
