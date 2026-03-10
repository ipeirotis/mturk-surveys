package com.ipeirotis.dto;

import java.util.Map;
import java.util.Set;

public class DemographicsSurveyAnswers {
    private Map<String, Map<String, Float>> countries;
    private Map<String, Map<String, Float>> yearOfBirth;
    private Map<String, Map<String, Float>> gender;
    private Map<String, Map<String, Float>> maritalStatus;
    private Map<String, Map<String, Float>> householdSize;
    private Map<String, Map<String, Float>> householdIncome;
    private Map<String, Map<String, Float>> educationalLevel;
    private Map<String, Map<String, Float>> timeSpentOnMturk;
    private Map<String, Map<String, Float>> weeklyIncomeFromMturk;
    private Map<String, Map<String, Float>> languagesSpoken;
    private Map<String, Set<String>> labels;

    public Map<String, Map<String, Float>> getCountries() {
        return countries;
    }

    public void setCountries(Map<String, Map<String, Float>> countries) {
        this.countries = countries;
    }

    public Map<String, Map<String, Float>> getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Map<String, Map<String, Float>> yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Map<String, Map<String, Float>> getGender() {
        return gender;
    }

    public void setGender(Map<String, Map<String, Float>> gender) {
        this.gender = gender;
    }

    public Map<String, Map<String, Float>> getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Map<String, Map<String, Float>> maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Map<String, Map<String, Float>> getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(Map<String, Map<String, Float>> householdSize) {
        this.householdSize = householdSize;
    }

    public Map<String, Map<String, Float>> getHouseholdIncome() {
        return householdIncome;
    }

    public void setHouseholdIncome(
            Map<String, Map<String, Float>> householdIncome) {
        this.householdIncome = householdIncome;
    }

    public Map<String, Map<String, Float>> getEducationalLevel() {
        return educationalLevel;
    }

    public void setEducationalLevel(Map<String, Map<String, Float>> educationalLevel) {
        this.educationalLevel = educationalLevel;
    }

    public Map<String, Map<String, Float>> getTimeSpentOnMturk() {
        return timeSpentOnMturk;
    }

    public void setTimeSpentOnMturk(Map<String, Map<String, Float>> timeSpentOnMturk) {
        this.timeSpentOnMturk = timeSpentOnMturk;
    }

    public Map<String, Map<String, Float>> getWeeklyIncomeFromMturk() {
        return weeklyIncomeFromMturk;
    }

    public void setWeeklyIncomeFromMturk(Map<String, Map<String, Float>> weeklyIncomeFromMturk) {
        this.weeklyIncomeFromMturk = weeklyIncomeFromMturk;
    }

    public Map<String, Map<String, Float>> getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(Map<String, Map<String, Float>> languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public Map<String, Set<String>> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, Set<String>> labels) {
        this.labels = labels;
    }

}
