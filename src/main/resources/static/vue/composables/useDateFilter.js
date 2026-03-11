/**
 * Date filter state composable - replaces dateFilterState factory
 */
const useDateFilter = () => {
    const { ref } = Vue;

    const minDate = new Date(2015, 2, 26); // March 26, 2015
    const maxDate = new Date();

    const defaultFrom = new Date();
    defaultFrom.setMonth(defaultFrom.getMonth() - 3);

    // Clamp to valid range
    const initFrom = defaultFrom < minDate ? new Date(minDate.getTime()) : defaultFrom;
    const initTo = maxDate;

    const from = ref(initFrom);
    const to = ref(new Date(initTo.getTime()));

    function formatDate(d) {
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        var yyyy = d.getFullYear();
        return mm + '/' + dd + '/' + yyyy;
    }

    function toInputValue(d) {
        var yyyy = d.getFullYear();
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        return yyyy + '-' + mm + '-' + dd;
    }

    function fromInputValue(str) {
        var parts = str.split('-');
        return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
    }

    return { from, to, minDate, maxDate, formatDate, toInputValue, fromInputValue };
};
