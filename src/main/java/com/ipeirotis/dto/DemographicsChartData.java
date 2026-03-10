package com.ipeirotis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Combined response for the chart endpoint: percentages + counts in a single payload.
 * This avoids two separate Datastore queries for the same date range.
 */
@Schema(description = "Combined chart data: aggregated percentages and raw counts from a single query")
public class DemographicsChartData {

    @Schema(description = "Aggregated percentages by period (hourly, daily/weekly/monthly, day-of-week)")
    private DemographicsSurveyAnswersByPeriod aggregated;

    @Schema(description = "Raw counts per period and totals")
    private DemographicsCountsResponse counts;

    public DemographicsSurveyAnswersByPeriod getAggregated() {
        return aggregated;
    }

    public void setAggregated(DemographicsSurveyAnswersByPeriod aggregated) {
        this.aggregated = aggregated;
    }

    public DemographicsCountsResponse getCounts() {
        return counts;
    }

    public void setCounts(DemographicsCountsResponse counts) {
        this.counts = counts;
    }
}
