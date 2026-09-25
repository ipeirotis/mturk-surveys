/**
 * Date filter state composable
 * Replaces the dateFilterState AngularJS factory
 */
// Last day of data collection: Amazon closed Mechanical Turk on 2026-09-30.
const SURVEY_END_DATE = new Date(2026, 8, 30);

// Latest selectable date: today, or the survey end date once it has passed.
const latestDataDate = () => {
    const today = new Date();
    return today > SURVEY_END_DATE ? new Date(SURVEY_END_DATE.getTime()) : today;
};

const useDateFilter = () => {
    const { ref } = Vue;
    const defaultTo = latestDataDate();
    const defaultFrom = new Date(defaultTo.getTime());
    defaultFrom.setFullYear(defaultFrom.getFullYear() - 2);

    const from = ref(defaultFrom);
    const to = ref(defaultTo);

    return { from, to };
};

// Shared singleton instance
const dateFilterState = useDateFilter();
