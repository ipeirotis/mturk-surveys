package com.ipeirotis.entity;

import java.util.List;

import com.ipeirotis.entity.enums.AnswerType;
import com.ipeirotis.entity.enums.SuggestionStyle;

public class Answer {

    private AnswerType type;
    private List<Selection> selections;
    private SuggestionStyle suggestionStyle;

    public Answer() {

    }

    public List<Selection> getSelections() {
        return selections;
    }

    public void setSelections(List<Selection> selections) {
        this.selections = selections;
    }

    public SuggestionStyle getSuggestionStyle() {
        return suggestionStyle;
    }

    public void setSuggestionStyle(SuggestionStyle suggestionStyle) {
        this.suggestionStyle = suggestionStyle;
    }

    public AnswerType getType() {
        return type;
    }

    public void setType(AnswerType type) {
        this.type = type;
    }

}
