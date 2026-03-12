/**
 * Chart data fetching composable
 * Replaces the dataService AngularJS factory
 */
const useChartData = () => {
    const cache = new Map();

    function formatDate(d) {
        var mm = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        return mm + '/' + dd + '/' + d.getFullYear();
    }

    async function loadChartData(from, to) {
        var fromStr = formatDate(from);
        var toStr = formatDate(to);
        var key = 'chart_' + fromStr + '_' + toStr;

        if (cache.has(key)) {
            return cache.get(key);
        }

        var response = await fetch(
            '/api/survey/demographics/chartData?from=' + fromStr + '&to=' + toStr,
            { signal: AbortSignal.timeout(300000) }
        );
        if (!response.ok) {
            throw new Error('HTTP ' + response.status);
        }
        var data = await response.json();
        cache.set(key, data);
        return data;
    }

    return { loadChartData, formatDate };
};

// Shared singleton instance
const chartDataService = useChartData();
