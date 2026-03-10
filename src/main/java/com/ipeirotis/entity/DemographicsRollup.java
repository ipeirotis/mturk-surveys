package com.ipeirotis.entity;

import java.util.Map;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Pre-aggregated demographics counts for a week or month.
 * Merges daily DemographicsSnapshot data so that large date ranges
 * can be served by loading ~130 monthly or ~570 weekly entities
 * instead of ~4000 daily ones.
 *
 * ID format: "weekly:2024-01-01" or "monthly:2024-01-01"
 * (period start date in yyyy-MM-dd).
 * Queries use key-based range filters on the ID, so no property indexes are needed.
 */
@Entity
@Cache
public class DemographicsRollup {

    @Id
    private String id;

    private String granularity; // "weekly" or "monthly"

    private String date; // period start in yyyy-MM-dd

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGranularity() { return granularity; }
    public void setGranularity(String granularity) { this.granularity = granularity; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalResponses() { return totalResponses; }
    public void setTotalResponses(int totalResponses) { this.totalResponses = totalResponses; }

    public Map<String, Integer> getCountries() { return countries; }
    public void setCountries(Map<String, Integer> countries) { this.countries = countries; }

    public Map<String, Integer> getYearOfBirth() { return yearOfBirth; }
    public void setYearOfBirth(Map<String, Integer> yearOfBirth) { this.yearOfBirth = yearOfBirth; }

    public Map<String, Integer> getGender() { return gender; }
    public void setGender(Map<String, Integer> gender) { this.gender = gender; }

    public Map<String, Integer> getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(Map<String, Integer> maritalStatus) { this.maritalStatus = maritalStatus; }

    public Map<String, Integer> getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Map<String, Integer> householdSize) { this.householdSize = householdSize; }

    public Map<String, Integer> getHouseholdIncome() { return householdIncome; }
    public void setHouseholdIncome(Map<String, Integer> householdIncome) { this.householdIncome = householdIncome; }

    public Map<String, Integer> getEducationalLevel() { return educationalLevel; }
    public void setEducationalLevel(Map<String, Integer> educationalLevel) { this.educationalLevel = educationalLevel; }

    public Map<String, Integer> getTimeSpentOnMturk() { return timeSpentOnMturk; }
    public void setTimeSpentOnMturk(Map<String, Integer> timeSpentOnMturk) { this.timeSpentOnMturk = timeSpentOnMturk; }

    public Map<String, Integer> getWeeklyIncomeFromMturk() { return weeklyIncomeFromMturk; }
    public void setWeeklyIncomeFromMturk(Map<String, Integer> weeklyIncomeFromMturk) { this.weeklyIncomeFromMturk = weeklyIncomeFromMturk; }

    public Map<String, Integer> getLanguagesSpoken() { return languagesSpoken; }
    public void setLanguagesSpoken(Map<String, Integer> languagesSpoken) { this.languagesSpoken = languagesSpoken; }
}
