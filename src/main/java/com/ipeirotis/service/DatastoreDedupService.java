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
	 * Deduplicate UserAnswer entities in Datastore for a single day.
	 * Groups by (workerId, hitId) and deletes all but the earliest entry per group.
	 *
	 * @param dateStr date in yyyy-MM-dd format
	 * @return number of duplicate entities deleted
	 */
	public int deduplicateDate(String dateStr) throws ParseException {
		DateFormat df = SafeDateFormat.forPattern("yyyy-MM-dd");
		Calendar dateFrom = Calendar.getInstance();
		dateFrom.setTime(df.parse(dateStr));
		CalendarUtils.truncateToDay(dateFrom);

		Calendar dateTo = (Calendar) dateFrom.clone();
		dateTo.add(Calendar.DAY_OF_MONTH, 1);

		List<UserAnswer> answers = surveyService.listAnswers("demographics", dateFrom.getTime(), dateTo.getTime());
		if (answers.isEmpty()) {
			return 0;
		}

		// Group by (workerId, hitId), keeping all entries per group
		Map<String, List<UserAnswer>> groups = new LinkedHashMap<>();
		for (UserAnswer ua : answers) {
			String key = (ua.getWorkerId() != null ? ua.getWorkerId() : "")
					+ "|" + (ua.getHitId() != null ? ua.getHitId() : "");
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(ua);
		}

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

	private int compareDates(Date a, Date b) {
		if (a == null && b == null) return 0;
		if (a == null) return 1;
		if (b == null) return -1;
		return a.compareTo(b);
	}
}
