package com.ipeirotis.dao;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemographicsRollupDaoTest {

    @Test
    void alignToPeriodEnd_monthly_exclusiveEndOnFirstOfMonth_excludesThatMonth() {
        // Range ending 2015-04-30 (inclusive) is sent as to=2015-05-01
        assertEquals("2015-05-01", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2015, 5, 1), "monthly"));
    }

    @Test
    void alignToPeriodEnd_monthly_midMonth_includesPartialMonth() {
        assertEquals("2015-06-01", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2015, 5, 15), "monthly"));
    }

    @Test
    void alignToPeriodEnd_monthly_surveyEnd_stopsAtOctober() {
        assertEquals("2026-10-01", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2026, 10, 1), "monthly"));
    }

    @Test
    void alignToPeriodEnd_weekly_exclusiveEndOnMonday_excludesThatWeek() {
        // Range ending Sunday 2026-09-27 (inclusive) is sent as to=Monday 2026-09-28
        assertEquals("2026-09-28", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2026, 9, 28), "weekly"));
    }

    @Test
    void alignToPeriodEnd_weekly_midWeek_includesPartialWeek() {
        // to=Thursday 2026-10-01 keeps the week of Monday 2026-09-28
        assertEquals("2026-10-05", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2026, 10, 1), "weekly"));
    }

    @Test
    void alignToPeriodEnd_daily_unchanged() {
        assertEquals("2026-10-01", DemographicsRollupDao.alignToPeriodEnd(LocalDate.of(2026, 10, 1), "daily"));
    }
}
