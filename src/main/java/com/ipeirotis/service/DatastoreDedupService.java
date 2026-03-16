package com.ipeirotis.service;

import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class DatastoreDedupService {

	private static final Logger logger = Logger.getLogger(DatastoreDedupService.class.getName());

	@Autowired
	private SurveyService surveyService;

	/**
	 * Result of analyzing duplicates for a single day.
	 */
	public static class DedupResult {
		public final String date;
		public final int totalEntries;
		public final int uniqueGroups;
		public final int duplicateEntries;
		public final int groupsWithDuplicates;

		public DedupResult(String date, int totalEntries, int uniqueGroups,
						   int duplicateEntries, int groupsWithDuplicates) {
			this.date = date;
			this.totalEntries = totalEntries;
			this.uniqueGroups = uniqueGroups;
			this.duplicateEntries = duplicateEntries;
			this.groupsWithDuplicates = groupsWithDuplicates;
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("date", date);
			map.put("totalEntries", totalEntries);
			map.put("uniqueGroups", uniqueGroups);
			map.put("duplicateEntries", duplicateEntries);
			map.put("groupsWithDuplicates", groupsWithDuplicates);
			return map;
		}
	}

	/**
	 * Count duplicates for a single day without deleting anything.
	 *
	 * @param dateStr date in yyyy-MM-dd format
	 * @return dedup analysis result
	 */
	public DedupResult countDuplicates(String dateStr) throws ParseException {
		List<UserAnswer> answers = loadAnswersForDate(dateStr);
		if (answers.isEmpty()) {
			return new DedupResult(dateStr, 0, 0, 0, 0);
		}

		Map<String, List<UserAnswer>> groups = groupByWorkerAndHit(answers);

		int groupsWithDups = 0;
		int duplicateEntries = 0;
		for (List<UserAnswer> group : groups.values()) {
			if (group.size() > 1) {
				groupsWithDups++;
				duplicateEntries += group.size() - 1;
			}
		}

		return new DedupResult(dateStr, answers.size(), groups.size(),
				duplicateEntries, groupsWithDups);
	}

	/**
	 * Deduplicate UserAnswer entities in Datastore for a single day.
	 * Groups by (workerId, hitId) and deletes all but the earliest entry per group.
	 *
	 * @param dateStr date in yyyy-MM-dd format
	 * @return number of duplicate entities deleted
	 */
	public int deduplicateDate(String dateStr) throws ParseException {
		List<UserAnswer> answers = loadAnswersForDate(dateStr);
		if (answers.isEmpty()) {
			return 0;
		}

		Map<String, List<UserAnswer>> groups = groupByWorkerAndHit(answers);

		// Collect duplicates to delete (keep earliest by date, then by ID as tiebreaker)
		List<UserAnswer> toDelete = new ArrayList<>();
		for (List<UserAnswer> group : groups.values()) {
			if (group.size() <= 1) {
				continue;
			}
			// Sort by date ascending, then by id ascending
			group.sort((a, b) -> {
				int dateCompare = compareDates(a.getDate(), b.getDate());
				if (dateCompare != 0) return dateCompare;
				return Long.compare(a.getId() != null ? a.getId() : 0, b.getId() != null ? b.getId() : 0);
			});
			// Keep first, delete the rest
			for (int i = 1; i < group.size(); i++) {
				toDelete.add(group.get(i));
			}
		}

		if (toDelete.isEmpty()) {
			return 0;
		}

		// Delete in batches of 250
		int batchSize = 250;
		for (int i = 0; i < toDelete.size(); i += batchSize) {
			int end = Math.min(i + batchSize, toDelete.size());
			ofy().delete().entities(toDelete.subList(i, end)).now();
		}

		logger.info("Datastore dedup for " + dateStr + ": deleted " + toDelete.size()
				+ " duplicates out of " + answers.size() + " total entries");
		return toDelete.size();
	}

	private List<UserAnswer> loadAnswersForDate(String dateStr) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");
		Calendar dateFrom = Calendar.getInstance();
		dateFrom.setTime(df.parse(dateStr));
		CalendarUtils.truncateToDay(dateFrom);

		Calendar dateTo = (Calendar) dateFrom.clone();
		dateTo.add(Calendar.DAY_OF_MONTH, 1);

		return surveyService.listAnswers("demographics", dateFrom.getTime(), dateTo.getTime());
	}

	private Map<String, List<UserAnswer>> groupByWorkerAndHit(List<UserAnswer> answers) {
		Map<String, List<UserAnswer>> groups = new LinkedHashMap<>();
		for (UserAnswer ua : answers) {
			String key = (ua.getWorkerId() != null ? ua.getWorkerId() : "")
					+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(ua);
		}
		return groups;
	}

	private int compareDates(Date a, Date b) {
		if (a == null && b == null) return 0;
		if (a == null) return 1;
		if (b == null) return -1;
		return a.compareTo(b);
	}
}
