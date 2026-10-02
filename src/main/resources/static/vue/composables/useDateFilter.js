/**
 * Date filter state composable
 * Replaces the dateFilterState AngularJS factory
 */
// First day of the demographics survey.
const SURVEY_START_DATE = new Date(2015, 2, 26);

// Amazon closed Mechanical Turk on 2026-09-30 (MturkService.CLOSURE_DATE), the
// last day of data collection. The date inputs show inclusive end dates; the
// chart API treats "to" as exclusive, so ChartView adds a day when querying.
const SURVEY_END_DATE = new Date(2026, 8, 30);

// Latest selectable end date: today, or the last survey day once it has passed.
const latestDataDate = () => {
    const today = new Date();
    return today > SURVEY_END_DATE ? new Date(SURVEY_END_DATE.getTime()) : today;
};

const useDateFilter = () => {
    const { ref } = Vue;
    // Collection has ended, so the default range is the whole survey.
    const defaultTo = latestDataDate();
    const defaultFrom = new Date(SURVEY_START_DATE.getTime());

    const from = ref(defaultFrom);
    const to = ref(defaultTo);

    return { from, to };
};

// Shared singleton instance
const dateFilterState = useDateFilter();
