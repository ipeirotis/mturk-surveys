package com.ipeirotis.entity;

import java.util.List;

import com.ipeirotis.entity.enums.SuggestionStyle;

public class Answer {

    private String text;
    private List<Selection> selections;
    private SuggestionStyle styleSuggestion;

    public Answer() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Selection> getSelections() {
        return selections;
    }

    public void setSelections(List<Selection> selections) {
        this.selections = selections;
    }

    public SuggestionStyle getStyleSuggestion() {
        return styleSuggestion;
    }

    public void setStyleSuggestion(SuggestionStyle styleSuggestion) {
        this.styleSuggestion = styleSuggestion;
    }

}
