package com.ipeirotis.dto;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class DemographicsSurveyAnswers {
    private Map<Date, Map<String, Float>> countries;
    private Map<Date, Map<String, Float>> yearOfBirth;
    private Map<Date, Map<String, Float>> gender;
    private Map<Date, Map<String, Float>> maritalStatus;
    private Map<Date, Map<String, Float>> householdSize;
    private Map<Date, Map<String, Float>> householdIncome;
    private Map<String, Set<String>> labels;

    public Map<Date, Map<String, Float>> getCountries() {
        return countries;
    }

    public void setCountries(Map<Date, Map<String, Float>> countries) {
        this.countries = countries;
    }

    public Map<Date, Map<String, Float>> getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Map<Date, Map<String, Float>> yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Map<Date, Map<String, Float>> getGender() {
        return gender;
    }

    public void setGender(Map<Date, Map<String, Float>> gender) {
        this.gender = gender;
    }

    public Map<Date, Map<String, Float>> getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Map<Date, Map<String, Float>> maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Map<Date, Map<String, Float>> getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(Map<Date, Map<String, Float>> householdSize) {
        this.householdSize = householdSize;
    }

    public Map<Date, Map<String, Float>> getHouseholdIncome() {
        return householdIncome;
    }

    public void setHouseholdIncome(Map<Date, Map<String, Float>> householdIncome) {
        this.householdIncome = householdIncome;
    }

    public Map<String, Set<String>> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Set<String>> labels) {
        this.labels = labels;
    }

}
