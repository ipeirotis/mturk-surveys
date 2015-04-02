package com.ipeirotis.dto;

public class DemographicsSurveyAnswersByPeriod {

    private DemographicsSurveyAnswers hourly;
    private DemographicsSurveyAnswers daily;
    private DemographicsSurveyAnswers weekly;

    public DemographicsSurveyAnswers getHourly() {
        return hourly;
    }

    public void setHourly(DemographicsSurveyAnswers hourly) {
        this.hourly = hourly;
    }

    public DemographicsSurveyAnswers getDaily() {
        return daily;
    }

    public void setDaily(DemographicsSurveyAnswers daily) {
        this.daily = daily;
    }

    public DemographicsSurveyAnswers getWeekly() {
        return weekly;
    }

    public void setWeekly(DemographicsSurveyAnswers weekly) {
        this.weekly = weekly;
    }

}
