package com.ipeirotis.entity;

import java.util.Date;
import java.util.Map;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Pre-aggregated demographics counts for a single date.
 * The id is the date string in MM/dd/yyyy format.
 * Each map stores: demographic_value -> count.
 */
@Entity
@Cache
public class DemographicsSnapshot {

    @Id
    private String id; // date in MM/dd/yyyy format

    @Index
    private String date; // yyyy-MM-dd format, indexed for range queries

    private int totalResponses;

    private Map<String, Integer> countries;
    private Map<String, Integer> yearOfBirth;
    private Map<String, Integer> gender;
    private Map<String, Integer> maritalStatus;
    private Map<String, Integer> householdSize;
    private Map<String, Integer> householdIncome;
    private Map<String, Integer> educationalLevel;
    private Map<String, Integer> timeSpentOnMturk;
    private Map<String, Integer> weeklyIncomeFromMturk;
    private Map<String, Integer> languagesSpoken;
    private Map<String, Integer> countriesDetailed;
    private Map<String, Integer> usStates;

    // Hourly breakdown: hour (0-23) -> count
    private Map<String, Integer> hourlyTotals;
    // Hourly demographic breakdowns: "hour:value" -> count
    private Map<String, Integer> hourlyCountries;
    private Map<String, Integer> hourlyYearOfBirth;
    private Map<String, Integer> hourlyGender;
    private Map<String, Integer> hourlyMaritalStatus;
    private Map<String, Integer> hourlyHouseholdSize;
    private Map<String, Integer> hourlyHouseholdIncome;
    private Map<String, Integer> hourlyEducationalLevel;
    private Map<String, Integer> hourlyTimeSpentOnMturk;
    private Map<String, Integer> hourlyWeeklyIncomeFromMturk;
    private Map<String, Integer> hourlyLanguagesSpoken;

    // Response time percentiles (minutes between HIT creation and answer submission)
    private Long medianResponseTimeMinutes;
    private Long p25ResponseTimeMinutes;
    private Long p75ResponseTimeMinutes;
    private Integer responseTimeCount; // number of answers with valid response times

    // Day of week (Sun, Mon, etc.)
    private String dayOfWeek;

    // Timestamp of last update, used for optimistic locking
    private Date lastUpdated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }

    public Map<String, Integer> getCountries() {
        return countries;
    }

    public void setCountries(Map<String, Integer> countries) {
        this.countries = countries;
    }

    public Map<String, Integer> getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Map<String, Integer> yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Map<String, Integer> getGender() {
        return gender;
    }

    public void setGender(Map<String, Integer> gender) {
        this.gender = gender;
    }

    public Map<String, Integer> getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Map<String, Integer> maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Map<String, Integer> getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(Map<String, Integer> householdSize) {
        this.householdSize = householdSize;
    }

    public Map<String, Integer> getHouseholdIncome() {
        return householdIncome;
    }

    public void setHouseholdIncome(Map<String, Integer> householdIncome) {
        this.householdIncome = householdIncome;
    }

    public Map<String, Integer> getHourlyTotals() {
        return hourlyTotals;
    }

    public void setHourlyTotals(Map<String, Integer> hourlyTotals) {
        this.hourlyTotals = hourlyTotals;
    }

    public Map<String, Integer> getHourlyCountries() {
        return hourlyCountries;
    }

    public void setHourlyCountries(Map<String, Integer> hourlyCountries) {
        this.hourlyCountries = hourlyCountries;
    }

    public Map<String, Integer> getHourlyYearOfBirth() {
        return hourlyYearOfBirth;
    }

    public void setHourlyYearOfBirth(Map<String, Integer> hourlyYearOfBirth) {
        this.hourlyYearOfBirth = hourlyYearOfBirth;
    }

    public Map<String, Integer> getHourlyGender() {
        return hourlyGender;
    }

    public void setHourlyGender(Map<String, Integer> hourlyGender) {
        this.hourlyGender = hourlyGender;
    }

    public Map<String, Integer> getHourlyMaritalStatus() {
        return hourlyMaritalStatus;
    }

    public void setHourlyMaritalStatus(Map<String, Integer> hourlyMaritalStatus) {
        this.hourlyMaritalStatus = hourlyMaritalStatus;
    }

    public Map<String, Integer> getHourlyHouseholdSize() {
        return hourlyHouseholdSize;
    }

    public void setHourlyHouseholdSize(Map<String, Integer> hourlyHouseholdSize) {
        this.hourlyHouseholdSize = hourlyHouseholdSize;
    }

    public Map<String, Integer> getHourlyHouseholdIncome() {
        return hourlyHouseholdIncome;
    }

    public void setHourlyHouseholdIncome(Map<String, Integer> hourlyHouseholdIncome) {
        this.hourlyHouseholdIncome = hourlyHouseholdIncome;
    }

    public Map<String, Integer> getEducationalLevel() {
        return educationalLevel;
    }

    public void setEducationalLevel(Map<String, Integer> educationalLevel) {
        this.educationalLevel = educationalLevel;
    }

    public Map<String, Integer> getTimeSpentOnMturk() {
        return timeSpentOnMturk;
    }

    public void setTimeSpentOnMturk(Map<String, Integer> timeSpentOnMturk) {
        this.timeSpentOnMturk = timeSpentOnMturk;
    }

    public Map<String, Integer> getWeeklyIncomeFromMturk() {
        return weeklyIncomeFromMturk;
    }

    public void setWeeklyIncomeFromMturk(Map<String, Integer> weeklyIncomeFromMturk) {
        this.weeklyIncomeFromMturk = weeklyIncomeFromMturk;
    }

    public Map<String, Integer> getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(Map<String, Integer> languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    public Map<String, Integer> getHourlyEducationalLevel() {
        return hourlyEducationalLevel;
    }

    public void setHourlyEducationalLevel(Map<String, Integer> hourlyEducationalLevel) {
        this.hourlyEducationalLevel = hourlyEducationalLevel;
    }

    public Map<String, Integer> getHourlyTimeSpentOnMturk() {
        return hourlyTimeSpentOnMturk;
    }

    public void setHourlyTimeSpentOnMturk(Map<String, Integer> hourlyTimeSpentOnMturk) {
        this.hourlyTimeSpentOnMturk = hourlyTimeSpentOnMturk;
    }

    public Map<String, Integer> getHourlyWeeklyIncomeFromMturk() {
        return hourlyWeeklyIncomeFromMturk;
    }

    public void setHourlyWeeklyIncomeFromMturk(Map<String, Integer> hourlyWeeklyIncomeFromMturk) {
        this.hourlyWeeklyIncomeFromMturk = hourlyWeeklyIncomeFromMturk;
    }

    public Map<String, Integer> getHourlyLanguagesSpoken() {
        return hourlyLanguagesSpoken;
    }

    public void setHourlyLanguagesSpoken(Map<String, Integer> hourlyLanguagesSpoken) {
        this.hourlyLanguagesSpoken = hourlyLanguagesSpoken;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Map<String, Integer> getCountriesDetailed() {
        return countriesDetailed;
    }

    public void setCountriesDetailed(Map<String, Integer> countriesDetailed) {
        this.countriesDetailed = countriesDetailed;
    }

    public Map<String, Integer> getUsStates() {
        return usStates;
    }

    public void setUsStates(Map<String, Integer> usStates) {
        this.usStates = usStates;
    }

    public Long getMedianResponseTimeMinutes() {
        return medianResponseTimeMinutes;
    }

    public void setMedianResponseTimeMinutes(Long medianResponseTimeMinutes) {
        this.medianResponseTimeMinutes = medianResponseTimeMinutes;
    }

    public Long getP25ResponseTimeMinutes() {
        return p25ResponseTimeMinutes;
    }

    public void setP25ResponseTimeMinutes(Long p25ResponseTimeMinutes) {
        this.p25ResponseTimeMinutes = p25ResponseTimeMinutes;
    }

    public Long getP75ResponseTimeMinutes() {
        return p75ResponseTimeMinutes;
    }

    public void setP75ResponseTimeMinutes(Long p75ResponseTimeMinutes) {
        this.p75ResponseTimeMinutes = p75ResponseTimeMinutes;
    }

    public Integer getResponseTimeCount() {
        return responseTimeCount;
    }

    public void setResponseTimeCount(Integer responseTimeCount) {
        this.responseTimeCount = responseTimeCount;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
