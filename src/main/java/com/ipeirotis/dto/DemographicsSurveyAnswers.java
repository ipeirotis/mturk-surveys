package com.ipeirotis.dto;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class DemographicsSurveyAnswers {
    private Map<Date, Map<String, Float>> byCountry;
    private Map<Date, Map<String, Float>> byBirthday;
    private Map<Date, Map<String, Float>> byGender;
    private Map<Date, Map<String, Float>> byMaritalStatus;
    private Map<Date, Map<String, Float>> byHouseholdSize;
    private Map<Date, Map<String, Float>> byHouseholdIncome;
    private Map<String, Set<String>> labels;

    public Map<Date, Map<String, Float>> getByCountry() {
        return byCountry;
    }

    public void setByCountry(Map<Date, Map<String, Float>> byCountry) {
        this.byCountry = byCountry;
    }

    public Map<Date, Map<String, Float>> getByBirthday() {
        return byBirthday;
    }

    public void setByBirthday(Map<Date, Map<String, Float>> byBirthday) {
        this.byBirthday = byBirthday;
    }

    public Map<Date, Map<String, Float>> getByGender() {
        return byGender;
    }

    public void setByGender(Map<Date, Map<String, Float>> byGender) {
        this.byGender = byGender;
    }

    public Map<Date, Map<String, Float>> getByMaritalStatus() {
        return byMaritalStatus;
    }

    public void setByMaritalStatus(Map<Date, Map<String, Float>> byMaritalStatus) {
        this.byMaritalStatus = byMaritalStatus;
    }

    public Map<Date, Map<String, Float>> getByHouseholdSize() {
        return byHouseholdSize;
    }

    public void setByHouseholdSize(Map<Date, Map<String, Float>> byHouseholdSize) {
        this.byHouseholdSize = byHouseholdSize;
    }

    public Map<Date, Map<String, Float>> getByHouseholdIncome() {
        return byHouseholdIncome;
    }

    public void setByHouseholdIncome(
            Map<Date, Map<String, Float>> byHouseholdIncome) {
        this.byHouseholdIncome = byHouseholdIncome;
    }

    public Map<String, Set<String>> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Set<String>> labels) {
        this.labels = labels;
    }
}
