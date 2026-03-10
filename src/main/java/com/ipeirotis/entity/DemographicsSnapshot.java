package com.ipeirotis.entity;

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

    // Hourly breakdown: hour (0-23) -> count
    private Map<String, Integer> hourlyTotals;
    // Hourly demographic breakdowns: "hour:dimension:value" -> count
    private Map<String, Integer> hourlyCountries;
    private Map<String, Integer> hourlyYearOfBirth;
    private Map<String, Integer> hourlyGender;
    private Map<String, Integer> hourlyMaritalStatus;
    private Map<String, Integer> hourlyHouseholdSize;
    private Map<String, Integer> hourlyHouseholdIncome;

    // Day of week (Sun, Mon, etc.)
    private String dayOfWeek;

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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
