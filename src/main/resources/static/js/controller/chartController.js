angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', '$timeout', 'dataService', 'dateFilterState',
    function ($scope, $filter, $routeParams, $timeout, dataService, dateFilterState) {

    var MONTH_ABBR = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

    $scope.from = new Date(dateFilterState.from.getTime());
    $scope.to = new Date(dateFilterState.to.getTime());
    $scope.activePill = 'dailyChartPill';
    $scope.chartIds = ['hourlyChart', 'dailyChart', 'weeklyChart', 'volumeChart'];
    $scope.drawnCharts = [];
    $scope.visibleChart = 'dailyChart';

    $scope.hourlyChart = {};
    $scope.dailyChart = {};
    $scope.weeklyChart = {};
    $scope.volumeChart = {};
    $scope.displayMode = 'bar';
    $scope.countsData = null;
    $scope.summaryStats = null;
    $scope.granularityNote = null;

    // --- Label formatting helpers ---

    // Format a Date.toString() key depending on granularity reported by the backend.
    function formatDailyLabel(periodKey, granularity) {
        var d = new Date(periodKey);
        if (granularity === 'monthly') {
            return MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear();
        }
        // 'weekly' or 'daily' — show M/d/yyyy
        return (d.getMonth()+1) + '/' + d.getDate() + '/' + d.getFullYear();
    }

    function granularityLabel(gran, numPeriods) {
        if (gran === 'weekly') return 'Showing weekly averages (' + numPeriods + ' weeks)';
        if (gran === 'monthly') return 'Showing monthly averages (' + numPeriods + ' months)';
        return null;
    }

    // Parse a counts day date string "YYYY-MM-DD" into a Date
    function parseCountsDate(dateStr) {
        var p = dateStr.split('-');
        return new Date(parseInt(p[0]), parseInt(p[1]) - 1, parseInt(p[2]));
    }

    // --- End helpers ---

    $scope.setDisplayMode = function(mode) {
        $scope.displayMode = mode;
        var name = $scope.visibleChart;
        if (name === 'volumeChart') return; // volume chart ignores display mode
        if ($scope[name] && $scope[name].labels) {
            var copy = angular.copy($scope[name]);
            copy.displayMode = mode;
            $scope[name] = copy;
        }
        var kept = [];
        for (var i = 0; i < $scope.drawnCharts.length; i++) {
            if ($scope.drawnCharts[i] === name) {
                kept.push(name);
            }
        }
        $scope.drawnCharts = kept;
    };

    $scope.load = function(){
        var fromStr = $filter('date')($scope.from, 'MM/dd/yyyy');
        var toStr = $filter('date')($scope.to, 'MM/dd/yyyy');

        // Single XHR for both percentages and counts
        dataService.loadChartData(fromStr, toStr, function(chartData){
            $scope.response = chartData.aggregated;
            $scope.countsData = chartData.counts;
            buildSummaryStats(chartData.counts);
            $scope.drawnCharts = [];
            $scope.draw($scope.visibleChart);
        }, function(error){
            console.log(error);
        });
    };

    function buildSummaryStats(counts) {
        if (!counts) return;
        var stats = {};
        stats.totalResponses = counts.totalResponses || 0;
        stats.numDays = counts.days ? counts.days.length : 0;
        stats.avgPerDay = stats.numDays > 0 ? Math.round(stats.totalResponses / stats.numDays) : 0;

        // Top country
        stats.topCountry = findTop(counts.totalCountries);
        // Top gender
        stats.topGender = findTop(counts.totalGender);
        // Top income
        stats.topIncome = findTop(counts.totalHouseholdIncome);
        // Top education
        stats.topEducation = findTop(counts.totalEducationalLevel);

        $scope.summaryStats = stats;
    }

    function findTop(map) {
        if (!map) return { label: 'N/A', count: 0, pct: 0 };
        var best = null, total = 0;
        for (var k in map) {
            total += map[k];
            if (!best || map[k] > best.count) {
                best = { label: k, count: map[k] };
            }
        }
        if (best && total > 0) {
            best.pct = Math.round(best.count / total * 1000) / 10;
        } else {
            return { label: 'N/A', count: 0, pct: 0 };
        }
        return best;
    }

    $scope.draw = function(chart){
        $scope.visibleChart = chart;
        $scope.activePill = chart + 'Pill';

        var drawn = false;
        var i = $.inArray(chart, $scope.drawnCharts);
        if(i < 0){
            $scope.drawnCharts.push(chart);
        } else {
            drawn = true;
        }

        angular.forEach($scope.chartIds, function(chartId){
            if($scope.visibleChart != chartId){
                $('#'+chartId).css({display:'none'});
            } else {
                $('#'+chartId).css({display:'block'});
            }
        });

        if(drawn == false){
            if (chart === 'volumeChart') {
                populateVolumeChart($scope);
            } else {
                populate($scope, chart, $scope.response, chart.substr(0, chart.length-5),
                        $routeParams.id);
            }
        }
    };

    $scope.load();

    var watchEnabled = false;
    $timeout(function() { watchEnabled = true; });

    $scope.$watch('from+to', function(newValue, oldValue) {
        if(!watchEnabled) return;
        if($scope.from && $scope.to){
            dateFilterState.from = $scope.from;
            dateFilterState.to = $scope.to;
            $scope.load();
        }
    });

    $scope.openFromPicker = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.openedFrom = true;
    };

    $scope.openToPicker = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.openedTo = true;
    };

    function populateVolumeChart(scope) {
        var counts = scope.countsData;
        if (!counts || !counts.days || counts.days.length === 0) return;

        var days = counts.days.slice().sort(function(a, b) {
            return a.date < b.date ? -1 : a.date > b.date ? 1 : 0;
        });

        var gran = counts.granularity || 'daily';
        scope.volumeGranularity = granularityLabel(gran, days.length);

        var labels = [];
        var data = [];
        for (var i = 0; i < days.length; i++) {
            if (gran === 'monthly') {
                var d = parseCountsDate(days[i].date);
                labels.push(MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear());
            } else {
                var parts = days[i].date.split('-');
                labels.push(parseInt(parts[1]) + '/' + parseInt(parts[2]) + '/' + parts[0]);
            }
            data.push(days[i].totalResponses);
        }

        scope.volumeChart = {
            labels: labels,
            datasets: [{ label: 'Responses', data: data }],
            displayMode: 'volume'
        };
    }

    function populate(scope, chartName, data, type, id) {
        var periodData = data[type][id];
        var labelSet = data[type].labels[id];

        if (!periodData || !labelSet) {
            return;
        }

        var periods = [];
        for (var propName in periodData) {
            periods.push(propName);
        }
        if (periods.length === 0) {
            return;
        }

        // The backend reports the granularity used for the "daily" field
        var granularity = (type === 'daily' && data.dailyGranularity) ? data.dailyGranularity : 'daily';

        if (type === 'daily') {
            periods.sort(function(a, b) { return new Date(a) - new Date(b); });
            scope.dailyGranularity = granularityLabel(granularity, periods.length);
        } else if (type === 'hourly') {
            periods.sort(function(a, b) { return parseInt(a) - parseInt(b); });
        }

        var chartLabels = [];
        for (var p = 0; p < periods.length; p++) {
            if (type === 'daily') {
                chartLabels.push(formatDailyLabel(periods[p], granularity));
            } else {
                chartLabels.push(periods[p]);
            }
        }

        var datasets = [];
        angular.forEach(labelSet, function(label) {
            var values = [];
            for (var pp = 0; pp < periods.length; pp++) {
                var entry = periodData[periods[pp]];
                values.push(entry[label] ? parseFloat(entry[label]) : 0);
            }
            datasets.push({ label: label, data: values });
        });

        // Attach counts data for tooltips if available
        var countsPerPeriod = null;
        if (scope.countsData && scope.countsData.days && type === 'daily') {
            countsPerPeriod = {};
            var days = scope.countsData.days;

            // Build a lookup from counts date to day data
            var dayCountsByDate = {};
            for (var di = 0; di < days.length; di++) {
                dayCountsByDate[days[di].date] = days[di];
            }

            // Map each chart label to its counts entry
            for (var pi = 0; pi < periods.length; pi++) {
                var periodDate = new Date(periods[pi]);
                var dateKey = periodDate.getFullYear() + '-'
                    + (periodDate.getMonth() < 9 ? '0' : '') + (periodDate.getMonth() + 1) + '-'
                    + (periodDate.getDate() < 10 ? '0' : '') + periodDate.getDate();
                var dayData = dayCountsByDate[dateKey];
                if (dayData) {
                    countsPerPeriod[chartLabels[pi]] = dayData;
                }
            }
        }

        scope[chartName] = {
            labels: chartLabels,
            datasets: datasets,
            displayMode: scope.displayMode,
            countsPerPeriod: countsPerPeriod,
            demographicField: id
        };
    }
}]);
