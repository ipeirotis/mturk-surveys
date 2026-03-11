/**
 * Chart data fetching composable - replaces dataService factory
 */
const useChartData = () => {
    const chartDataCache = new Map();

    function loadChartData(from, to) {
        var key = 'chart_' + from + '_' + to;
        var cached = chartDataCache.get(key);
        if (cached) {
            return Promise.resolve(cached);
        }
        return fetch('/api/survey/demographics/chartData?from=' + from + '&to=' + to, {
            signal: AbortSignal.timeout(300000)
        })
        .then(function(response) {
            if (!response.ok) throw new Error('HTTP ' + response.status);
            return response.json();
        })
        .then(function(data) {
            chartDataCache.set(key, data);
            return data;
        });
    }

    return { loadChartData };
};
