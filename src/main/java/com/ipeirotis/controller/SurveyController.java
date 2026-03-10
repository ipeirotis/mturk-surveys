package com.ipeirotis.controller;

import com.ipeirotis.dto.DemographicsChartData;
import com.ipeirotis.dto.DemographicsCountsResponse;
import com.ipeirotis.dto.DemographicsSurveyAnswersByPeriod;
import com.ipeirotis.entity.Survey;
import com.ipeirotis.entity.UserAnswer;
import com.ipeirotis.exception.ResourceNotFoundException;
import com.ipeirotis.exception.ValidationException;
import com.ipeirotis.ofy.ListByCursorResult;
import com.ipeirotis.service.DemographicsSnapshotService;
import com.ipeirotis.service.SurveyService;
import com.ipeirotis.service.UserAnswerService;
import com.ipeirotis.util.CalendarUtils;
import com.ipeirotis.util.SafeDateFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;

@RestController
@RequestMapping("/api/survey")
@Tag(name = "Survey", description = "Demographics survey data and analytics")
public class SurveyController {

	@Autowired
	private SurveyService surveyService;
	@Autowired
	private UserAnswerService userAnswerService;
	@Autowired
	private DemographicsSnapshotService snapshotService;

	@GetMapping({"/demographics/answers"})
	@Operation(summary = "Get paginated survey answers",
			description = "Returns individual demographics survey responses with cursor-based pagination. "
					+ "Worker IDs are MD5-hashed and IP addresses are stripped for privacy.")
	public ListByCursorResult<UserAnswer> getSurveyAnswers(
			@Parameter(description = "Cursor token for pagination (from previous response's nextPageToken)")
			@RequestParam(required = false) String cursor,
			@Parameter(description = "Maximum number of results to return (default: 100)")
			@RequestParam(required = false, defaultValue = "100") Integer limit,
			@Parameter(description = "Start date filter in MM/dd/yyyy format (inclusive)")
			@RequestParam(required = false) String from,
			@Parameter(description = "End date filter in MM/dd/yyyy format (exclusive)")
			@RequestParam(required = false) String to) {
		Date fromDate = parseDate(from);
		Date toDate = parseDate(to);
		return userAnswerService.list(cursor, limit, fromDate, toDate);
	}

	@GetMapping({"/demographics/chartData"})
	@Operation(summary = "Get combined chart data (percentages + counts)",
			description = "Returns both aggregated percentages and raw counts from a single Datastore query. "
					+ "Preferred over separate aggregatedAnswers + counts calls. "
					+ "Auto-selects granularity: daily (≤90 days), weekly (91-730), monthly (>730).")
	public ResponseEntity<DemographicsChartData> getChartData(
			@Parameter(description = "Start date in MM/dd/yyyy format") @RequestParam String from,
			@Parameter(description = "End date in MM/dd/yyyy format") @RequestParam String to) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
				.body(snapshotService.getChartData(from, to));
	}

	@GetMapping({"/demographics/aggregatedAnswers"})
	@Operation(summary = "Get aggregated demographics percentages",
			description = "Returns demographics data aggregated by period (hourly, daily, weekly) as percentages. "
					+ "Data comes from pre-computed daily snapshots. "
					+ "Consider using /demographics/chartData instead for better performance.")
	public ResponseEntity<DemographicsSurveyAnswersByPeriod> getSurveyAggregatedAnswers(
			@Parameter(description = "Start date in MM/dd/yyyy format") @RequestParam String from,
			@Parameter(description = "End date in MM/dd/yyyy format") @RequestParam String to) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
				.body(snapshotService.getAggregatedAnswers(from, to));
	}

	@GetMapping({"/demographics/counts"})
	@Operation(summary = "Get raw demographics counts",
			description = "Returns raw count data (not percentages) from pre-computed daily snapshots. "
					+ "Includes per-day breakdowns and summed totals across the date range. "
					+ "Consider using /demographics/chartData instead for better performance.")
	public ResponseEntity<DemographicsCountsResponse> getSurveyCounts(
			@Parameter(description = "Start date in MM/dd/yyyy format") @RequestParam String from,
			@Parameter(description = "End date in MM/dd/yyyy format") @RequestParam String to) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
				.body(snapshotService.getCounts(from, to));
	}

	@GetMapping("/demographics/answers/csv")
	@Operation(summary = "Export raw answers as CSV",
			description = "Downloads individual-level demographics data as a CSV file. "
					+ "Includes all 9 survey questions plus metadata. "
					+ "Worker IDs are MD5-hashed and IPs stripped for privacy. "
					+ "Date range is capped at 366 days.")
	public ResponseEntity<StreamingResponseBody> exportAnswersCsv(
			@Parameter(description = "Start date in MM/dd/yyyy format (inclusive)") @RequestParam String from,
			@Parameter(description = "End date in MM/dd/yyyy format (exclusive)") @RequestParam String to) {

		Date fromDate = parseDate(from);
		Date toDate = parseDate(to);
		if (fromDate == null || toDate == null) {
			throw new ValidationException("Both 'from' and 'to' dates are required in MM/dd/yyyy format");
		}

		long daysBetween = TimeUnit.MILLISECONDS.toDays(toDate.getTime() - fromDate.getTime());
		if (daysBetween > 366) {
			throw new ValidationException("Date range cannot exceed 366 days");
		}
		if (daysBetween < 0) {
			throw new ValidationException("'from' date must be before 'to' date");
		}

		String filename = String.format("demographics-%s-%s.csv",
				new SimpleDateFormat("yyyyMMdd").format(fromDate),
				new SimpleDateFormat("yyyyMMdd").format(toDate));

		StreamingResponseBody body = outputStream -> {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
			writer.println("date,workerId,country,region,city,hitId,yearOfBirth,gender,maritalStatus,"
					+ "householdSize,householdIncome,educationalLevel,timeSpentOnMturk,"
					+ "weeklyIncomeFromMturk,languagesSpoken");

			List<UserAnswer> answers = userAnswerService.listByDateRange(fromDate, toDate);
			DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			for (UserAnswer ua : answers) {
				Map<String, String> a = ua.getAnswers();
				if (a == null) a = Collections.emptyMap();

				writer.print(csvEscape(ua.getDate() != null ? isoFormat.format(ua.getDate()) : ""));
				writer.print(",");
				writer.print(csvEscape(ua.getWorkerId()));
				writer.print(",");
				writer.print(csvEscape(ua.getLocationCountry()));
				writer.print(",");
				writer.print(csvEscape(ua.getLocationRegion()));
				writer.print(",");
				writer.print(csvEscape(ua.getLocationCity()));
				writer.print(",");
				writer.print(csvEscape(ua.getHitId()));
				writer.print(",");
				writer.print(csvEscape(a.get("yearOfBirth")));
				writer.print(",");
				writer.print(csvEscape(a.get("gender")));
				writer.print(",");
				writer.print(csvEscape(a.get("maritalStatus")));
				writer.print(",");
				writer.print(csvEscape(a.get("householdSize")));
				writer.print(",");
				writer.print(csvEscape(a.get("householdIncome")));
				writer.print(",");
				writer.print(csvEscape(a.get("educationalLevel")));
				writer.print(",");
				writer.print(csvEscape(a.get("timeSpentOnMturk")));
				writer.print(",");
				writer.print(csvEscape(a.get("weeklyIncomeFromMturk")));
				writer.print(",");
				writer.println(csvEscape(a.get("languagesSpoken")));
			}
			writer.flush();
		};

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(body);
	}

	@GetMapping("/{surveyId}")
	@Operation(summary = "Get survey by ID", description = "Returns survey details including questions and configuration")
	public Survey get(@PathVariable String surveyId) {
		Survey survey = surveyService.get(surveyId);
		if(survey == null) {
			throw new ResourceNotFoundException(String.format("Survey with id=%s doesn't exist", surveyId));
		} else {
			return survey;
		}
	}

	private Date parseDate(String dateStr) {
		if (dateStr == null || dateStr.isEmpty()) return null;
		try {
			DateFormat df = SafeDateFormat.forPattern("MM/dd/yyyy");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(dateStr));
			CalendarUtils.truncateToDay(cal);
			return cal.getTime();
		} catch (ParseException e) {
			throw new ValidationException("Invalid date format: " + dateStr + ". Expected MM/dd/yyyy");
		}
	}

	private static String csvEscape(String value) {
		if (value == null) return "";
		if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

}
