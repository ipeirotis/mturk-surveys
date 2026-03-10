package com.ipeirotis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Raw count data from pre-computed daily demographics snapshots")
public class DemographicsCountsResponse {

	@Schema(description = "Per-period breakdown of demographics counts (daily, weekly, or monthly depending on range)")
	private List<DailyCount> days;

	@Schema(description = "Total responses across all days in the range")
	private int totalResponses;

	@Schema(description = "Granularity of the 'days' field: daily, weekly, or monthly")
	private String granularity;

	@Schema(description = "Country counts summed across all days")
	private Map<String, Integer> totalCountries;
	@Schema(description = "Year of birth counts summed across all days (grouped by decade)")
	private Map<String, Integer> totalYearOfBirth;
	@Schema(description = "Gender counts summed across all days")
	private Map<String, Integer> totalGender;
	@Schema(description = "Marital status counts summed across all days")
	private Map<String, Integer> totalMaritalStatus;
	@Schema(description = "Household size counts summed across all days")
	private Map<String, Integer> totalHouseholdSize;
	@Schema(description = "Household income counts summed across all days")
	private Map<String, Integer> totalHouseholdIncome;
	@Schema(description = "Educational level counts summed across all days")
	private Map<String, Integer> totalEducationalLevel;
	@Schema(description = "Time spent on MTurk counts summed across all days")
	private Map<String, Integer> totalTimeSpentOnMturk;
	@Schema(description = "Weekly income from MTurk counts summed across all days")
	private Map<String, Integer> totalWeeklyIncomeFromMturk;
	@Schema(description = "Languages spoken counts summed across all days")
	private Map<String, Integer> totalLanguagesSpoken;

	public List<DailyCount> getDays() {
		return days;
	}

	public void setDays(List<DailyCount> days) {
		this.days = days;
	}

	public int getTotalResponses() {
		return totalResponses;
	}

	public void setTotalResponses(int totalResponses) {
		this.totalResponses = totalResponses;
	}

	public Map<String, Integer> getTotalCountries() {
		return totalCountries;
	}

	public void setTotalCountries(Map<String, Integer> totalCountries) {
		this.totalCountries = totalCountries;
	}

	public Map<String, Integer> getTotalYearOfBirth() {
		return totalYearOfBirth;
	}

	public void setTotalYearOfBirth(Map<String, Integer> totalYearOfBirth) {
		this.totalYearOfBirth = totalYearOfBirth;
	}

	public Map<String, Integer> getTotalGender() {
		return totalGender;
	}

	public void setTotalGender(Map<String, Integer> totalGender) {
		this.totalGender = totalGender;
	}

	public Map<String, Integer> getTotalMaritalStatus() {
		return totalMaritalStatus;
	}

	public void setTotalMaritalStatus(Map<String, Integer> totalMaritalStatus) {
		this.totalMaritalStatus = totalMaritalStatus;
	}

	public Map<String, Integer> getTotalHouseholdSize() {
		return totalHouseholdSize;
	}

	public void setTotalHouseholdSize(Map<String, Integer> totalHouseholdSize) {
		this.totalHouseholdSize = totalHouseholdSize;
	}

	public Map<String, Integer> getTotalHouseholdIncome() {
		return totalHouseholdIncome;
	}

	public void setTotalHouseholdIncome(Map<String, Integer> totalHouseholdIncome) {
		this.totalHouseholdIncome = totalHouseholdIncome;
	}

	public Map<String, Integer> getTotalEducationalLevel() {
		return totalEducationalLevel;
	}

	public void setTotalEducationalLevel(Map<String, Integer> totalEducationalLevel) {
		this.totalEducationalLevel = totalEducationalLevel;
	}

	public Map<String, Integer> getTotalTimeSpentOnMturk() {
		return totalTimeSpentOnMturk;
	}

	public void setTotalTimeSpentOnMturk(Map<String, Integer> totalTimeSpentOnMturk) {
		this.totalTimeSpentOnMturk = totalTimeSpentOnMturk;
	}

	public Map<String, Integer> getTotalWeeklyIncomeFromMturk() {
		return totalWeeklyIncomeFromMturk;
	}

	public void setTotalWeeklyIncomeFromMturk(Map<String, Integer> totalWeeklyIncomeFromMturk) {
		this.totalWeeklyIncomeFromMturk = totalWeeklyIncomeFromMturk;
	}

	public Map<String, Integer> getTotalLanguagesSpoken() {
		return totalLanguagesSpoken;
	}

	public void setTotalLanguagesSpoken(Map<String, Integer> totalLanguagesSpoken) {
		this.totalLanguagesSpoken = totalLanguagesSpoken;
	}

	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	@Schema(description = "Demographics counts for a single day")
	public static class DailyCount {
		@Schema(description = "Date in yyyy-MM-dd format")
		private String date;
		@Schema(description = "Total number of survey responses on this day")
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
	}
}
