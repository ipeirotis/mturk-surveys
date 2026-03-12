/**
 * Date filter state composable
 * Replaces the dateFilterState AngularJS factory
 */
const useDateFilter = () => {
    const { ref } = Vue;
    const defaultFrom = new Date();
    defaultFrom.setFullYear(defaultFrom.getFullYear() - 2);

    const from = ref(defaultFrom);
    const to = ref(new Date());

    return { from, to };
};

// Shared singleton instance
const dateFilterState = useDateFilter();
