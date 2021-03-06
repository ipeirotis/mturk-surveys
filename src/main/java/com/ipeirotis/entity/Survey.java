package com.ipeirotis.entity;

import java.util.List;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

@Entity
@Cache
public class Survey {

    @Id
    private String id;
    private String title;
    private String description;
    private Double reward;
    private Integer maxAssignments;
    private String htmlQuestion;
    @Ignore
    private Boolean production;
    @Ignore
    private String template;
    @Ignore
    private List<Question> questions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getReward() {
        return reward;
    }

    public void setReward(Double reward) {
        this.reward = reward;
    }

    public Integer getMaxAssignments() {
        return maxAssignments;
    }

    public void setMaxAssignments(Integer maxAssignments) {
        this.maxAssignments = maxAssignments;
    }

    public String getHtmlQuestion() {
        return htmlQuestion;
    }

    public void setHtmlQuestion(String htmlQuestion) {
        this.htmlQuestion = htmlQuestion;
    }

    public Boolean getProduction() {
        return production;
    }

    public void setProduction(Boolean production) {
        this.production = production;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
