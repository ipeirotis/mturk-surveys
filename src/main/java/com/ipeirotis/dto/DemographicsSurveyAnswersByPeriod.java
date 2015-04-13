package com.ipeirotis.dto;

import java.util.Map;

public class DemographicsSurveyAnswersByPeriod {

    private Map<String, DemographicsSurveyAnswers> hourly;
    private Map<String, DemographicsSurveyAnswers> daily;
    private Map<String, DemographicsSurveyAnswers> weekly;

    public Map<String, DemographicsSurveyAnswers> getHourly() {
        return hourly;
    }

    public void setHourly(Map<String, DemographicsSurveyAnswers> hourly) {
        this.hourly = hourly;
    }

    public Map<String, DemographicsSurveyAnswers> getDaily() {
        return daily;
    }

    public void setDaily(Map<String, DemographicsSurveyAnswers> daily) {
        this.daily = daily;
    }

    public Map<String, DemographicsSurveyAnswers> getWeekly() {
        return weekly;
    }

    public void setWeekly(Map<String, DemographicsSurveyAnswers> weekly) {
        this.weekly = weekly;
    }
}
