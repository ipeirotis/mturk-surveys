package com.ipeirotis.dto;

import java.util.Map;

public class DemographicsSurveyAnswersByPeriod {

    private DemographicsSurveyAnswers hourly;
    private DemographicsSurveyAnswers daily;
    private DemographicsSurveyAnswers weekly;
    /** Granularity used for the "daily" field: "daily", "weekly", or "monthly". */
    private String dailyGranularity;

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

    public String getDailyGranularity() {
        return dailyGranularity;
    }

    public void setDailyGranularity(String dailyGranularity) {
        this.dailyGranularity = dailyGranularity;
    }
}
