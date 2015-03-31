package com.ipeirotis.dto;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BirthSurveyAnswers {
    private Map<Date, Map<String, Float>> data;
    private Set<String> decades;

    public Map<Date, Map<String, Float>> getData() {
        return data;
    }

    public void setData(Map<Date, Map<String, Float>> data) {
        this.data = data;
    }

    public Set<String> getDecades() {
        return decades;
    }

    public void setDecades(Set<String> decades) {
        this.decades = decades;
    }

}
