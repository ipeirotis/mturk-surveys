angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', '$timeout', 'dataService', 'dateFilterState',
    function ($scope, $filter, $routeParams, $timeout, dataService, dateFilterState) {

    var MAP_VIEWS = { 'worldMap': 'world', 'usStates': 'us' };
    var MONTH_ABBR = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

    // Date validation limits
    $scope.minDate = new Date(2015, 2, 26); // March 26, 2015
    $scope.maxDate = new Date(); // today

    // Clamp persisted dates to valid range
    var initFrom = new Date(dateFilterState.from.getTime());
    var initTo = new Date(dateFilterState.to.getTime());
    if (initFrom < $scope.minDate) initFrom = new Date($scope.minDate.getTime());
    if (initTo > $scope.maxDate) initTo = new Date($scope.maxDate.getTime());
    if (initFrom > initTo) initFrom = new Date(initTo.getTime());

    $scope.from = initFrom;
    $scope.to = initTo;
    $scope.dailyChart = {};
    $scope.volumeChart = {};
    $scope.displayMode = 'bar';
    $scope.countsData = null;
    $scope.summaryStats = null;
    $scope.granularityNote = null;
    $scope.topN = 0; // 0 = show all
    $scope.topNOptions = [
        { value: 0, label: 'All' },
        { value: 5, label: 'Top 5' },
        { value: 10, label: 'Top 10' },
        { value: 15, label: 'Top 15' }
    ];

    // Map view state
    $scope.routeId = $routeParams.id;
    $scope.isMapView = !!MAP_VIEWS[$routeParams.id];
    $scope.mapType = MAP_VIEWS[$routeParams.id] || null;
    $scope.mapData = null;
    $scope.mapNormalized = ($routeParams.id === 'usStates') ? true : undefined;

    // --- Label formatting helpers ---

    function formatDailyLabel(periodKey, granularity) {
        var d = new Date(periodKey);
        if (granularity === 'monthly') {
            return MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear();
        }
        return (d.getMonth()+1) + '/' + d.getDate() + '/' + d.getFullYear();
    }

    function granularityLabel(gran, numPeriods) {
        if (gran === 'weekly') return 'Showing weekly averages (' + numPeriods + ' weeks)';
        if (gran === 'monthly') return 'Showing monthly averages (' + numPeriods + ' months)';
        return null;
    }

    function parseCountsDate(dateStr) {
        var p = dateStr.split('-');
        return new Date(parseInt(p[0]), parseInt(p[1]) - 1, parseInt(p[2]));
    }

    // --- End helpers ---

    $scope.donutChart = {};

    $scope.setDisplayMode = function(mode) {
        $scope.displayMode = mode;
        if (mode === 'donut' && $scope.response) {
            populateDonutChart($scope, $scope.response, $routeParams.id);
        } else if ($scope.dailyChart && $scope.dailyChart.labels) {
            var copy = angular.copy($scope.dailyChart);
            copy.displayMode = mode;
            $scope.dailyChart = copy;
        }
    };

    $scope.setTopN = function(n) {
        $scope.topN = n;
        if ($scope.response && $routeParams.id) {
            populateDailyChart($scope, $scope.response, $routeParams.id);
            populateVolumeChart($scope);
        }
    };

    $scope.setMapNormalized = function(val) {
        $scope.mapNormalized = val;
    };

    // Apply date range manually (Update button)
    $scope.applyDateRange = function() {
        dateFilterState.from = $scope.from;
        dateFilterState.to = $scope.to;
        $scope.load();
    };

    $scope.loading = false;
    $scope.loadError = null;

    $scope.load = function(){
        var fromStr = $filter('date')($scope.from, 'MM/dd/yyyy');
        var toStr = $filter('date')($scope.to, 'MM/dd/yyyy');

        // Compute prior period of same length for trend comparison
        var rangeMs = $scope.to.getTime() - $scope.from.getTime();
        var priorTo = new Date($scope.from.getTime() - 1); // day before current "from"
        var priorFrom = new Date(priorTo.getTime() - rangeMs);
        if (priorFrom < $scope.minDate) priorFrom = new Date($scope.minDate.getTime());
        var priorFromStr = $filter('date')(priorFrom, 'MM/dd/yyyy');
        var priorToStr = $filter('date')(priorTo, 'MM/dd/yyyy');

        $scope.loading = true;
        $scope.loadError = null;

        dataService.loadChartData(fromStr, toStr, function(chartData){
            $scope.loading = false;
            $scope.response = chartData.aggregated;
            $scope.countsData = chartData.counts;

            // Load prior period for trend arrows (non-blocking)
            dataService.loadChartData(priorFromStr, priorToStr, function(priorData) {
                buildSummaryStats(chartData.counts, priorData.counts);
            }, function() {
                // If prior load fails, show stats without trends
                buildSummaryStats(chartData.counts, null);
            });

            if ($scope.isMapView) {
                populateMapData(chartData.counts);
            } else {
                populateDailyChart($scope, $scope.response, $routeParams.id);
                populateVolumeChart($scope);
            }
        }, function(error){
            $scope.loading = false;
            $scope.loadError = 'Failed to load chart data. The date range may be too large \u2014 try a shorter period.';
            console.log(error);
        });
    };

    function populateMapData(counts) {
        if (!counts) return;
        if ($scope.mapType === 'us') {
            $scope.mapData = counts.totalUsStates || {};
        } else {
            $scope.mapData = counts.totalCountriesDetailed || {};
        }
    }

    function computeTrend(current, previous) {
        if (previous === null || previous === undefined || previous === 0) return null;
        var pctChange = ((current - previous) / previous) * 100;
        if (Math.abs(pctChange) < 0.5) return { direction: 'flat', pct: 0 };
        return {
            direction: pctChange > 0 ? 'up' : 'down',
            pct: Math.round(Math.abs(pctChange))
        };
    }

    function buildSummaryStats(counts, priorCounts) {
        if (!counts) return;
        var stats = {};
        stats.totalResponses = counts.totalResponses || 0;
        var numPeriods = counts.days ? counts.days.length : 0;
        stats.avgPerPeriod = numPeriods > 0 ? Math.round(stats.totalResponses / numPeriods) : 0;

        var gran = counts.granularity || 'daily';
        if (gran === 'monthly') {
            stats.avgLabel = 'Avg / Month';
        } else if (gran === 'weekly') {
            stats.avgLabel = 'Avg / Week';
        } else {
            stats.avgLabel = 'Avg / Day';
        }

        stats.topCountry = findTop(counts.totalCountries);
        stats.topGender = findTop(counts.totalGender);
        stats.topIncome = findTop(counts.totalHouseholdIncome);
        stats.topEducation = findTop(counts.totalEducationalLevel);

        // Compute trends from prior period
        if (priorCounts) {
            var priorTotal = priorCounts.totalResponses || 0;
            var priorNumPeriods = priorCounts.days ? priorCounts.days.length : 0;
            var priorAvg = priorNumPeriods > 0 ? Math.round(priorTotal / priorNumPeriods) : 0;

            stats.totalTrend = computeTrend(stats.totalResponses, priorTotal);
            stats.avgTrend = computeTrend(stats.avgPerPeriod, priorAvg);

            var priorTopCountry = findTop(priorCounts.totalCountries);
            if (stats.topCountry.label !== 'N/A' && priorTopCountry.label !== 'N/A') {
                stats.countryTrend = computeTrend(stats.topCountry.pct, priorTopCountry.pct);
            }
            var priorTopGender = findTop(priorCounts.totalGender);
            if (stats.topGender.label !== 'N/A' && priorTopGender.label !== 'N/A') {
                stats.genderTrend = computeTrend(stats.topGender.pct, priorTopGender.pct);
            }
        }

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

    $scope.load();

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

    function populateDailyChart(scope, data, id) {
        var periodData = data.daily[id];
        var labelSet = data.daily.labels[id];

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

        var granularity = data.dailyGranularity || 'daily';
        periods.sort(function(a, b) { return new Date(a) - new Date(b); });
        scope.dailyGranularity = granularityLabel(granularity, periods.length);

        var chartLabels = [];
        for (var p = 0; p < periods.length; p++) {
            chartLabels.push(formatDailyLabel(periods[p], granularity));
        }

        // For languages, filter out English by default
        var filteredLabelSet = labelSet;
        if (id === 'languagesSpoken') {
            filteredLabelSet = [];
            angular.forEach(labelSet, function(label) {
                if (label !== 'English') {
                    filteredLabelSet.push(label);
                }
            });
        }

        // Apply Top-N filter: rank categories by total value, group rest into "Other"
        var topN = scope.topN || 0;
        var labelsToShow = filteredLabelSet;
        if (topN > 0 && filteredLabelSet.length > topN) {
            var labelTotals = [];
            angular.forEach(filteredLabelSet, function(label) {
                var sum = 0;
                for (var pp = 0; pp < periods.length; pp++) {
                    var entry = periodData[periods[pp]];
                    sum += entry[label] ? parseFloat(entry[label]) : 0;
                }
                labelTotals.push({ label: label, total: sum });
            });
            labelTotals.sort(function(a, b) { return b.total - a.total; });
            labelsToShow = [];
            for (var ti = 0; ti < topN; ti++) {
                labelsToShow.push(labelTotals[ti].label);
            }
        }
        var otherLabels = [];
        if (topN > 0 && filteredLabelSet.length > topN) {
            angular.forEach(filteredLabelSet, function(label) {
                if (labelsToShow.indexOf(label) === -1) {
                    otherLabels.push(label);
                }
            });
        }

        var datasets = [];
        angular.forEach(labelsToShow, function(label) {
            var values = [];
            for (var pp = 0; pp < periods.length; pp++) {
                var entry = periodData[periods[pp]];
                values.push(entry[label] ? parseFloat(entry[label]) : 0);
            }
            datasets.push({ label: label, data: values });
        });

        // Add "Other" category if Top-N is active
        if (otherLabels.length > 0) {
            var otherValues = [];
            for (var pp = 0; pp < periods.length; pp++) {
                var sum = 0;
                for (var oi = 0; oi < otherLabels.length; oi++) {
                    var entry = periodData[periods[pp]];
                    sum += entry[otherLabels[oi]] ? parseFloat(entry[otherLabels[oi]]) : 0;
                }
                otherValues.push(sum);
            }
            datasets.push({ label: 'Other', data: otherValues });
        }

        // Attach counts data for tooltips if available
        var countsPerPeriod = null;
        if (scope.countsData && scope.countsData.days) {
            countsPerPeriod = {};
            var days = scope.countsData.days;

            var dayCountsByDate = {};
            for (var di = 0; di < days.length; di++) {
                dayCountsByDate[days[di].date] = days[di];
            }

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

        scope.dailyChart = {
            labels: chartLabels,
            datasets: datasets,
            displayMode: scope.displayMode,
            countsPerPeriod: countsPerPeriod,
            demographicField: id,
            autoScaleY: (id === 'languagesSpoken')
        };
    }

    function populateDonutChart(scope, data, id) {
        var periodData = data.daily[id];
        var labelSet = data.daily.labels[id];
        if (!periodData || !labelSet) return;

        // Find the latest period
        var periods = [];
        for (var propName in periodData) {
            periods.push(propName);
        }
        if (periods.length === 0) return;
        periods.sort(function(a, b) { return new Date(b) - new Date(a); });
        var latestPeriod = periods[0];

        var granularity = data.dailyGranularity || 'daily';
        var periodLabel = formatDailyLabel(latestPeriod, granularity);

        var entry = periodData[latestPeriod];

        // Filter labels same as daily chart
        var filteredLabelSet = labelSet;
        if (id === 'languagesSpoken') {
            filteredLabelSet = [];
            angular.forEach(labelSet, function(label) {
                if (label !== 'English') filteredLabelSet.push(label);
            });
        }

        // Apply Top-N filter
        var topN = scope.topN || 0;
        var labelsToUse = filteredLabelSet;
        if (topN > 0 && filteredLabelSet.length > topN) {
            var sorted = filteredLabelSet.slice().sort(function(a, b) {
                return (entry[b] || 0) - (entry[a] || 0);
            });
            labelsToUse = sorted.slice(0, topN);
        }

        var chartLabels = [];
        var chartData = [];
        var otherVal = 0;

        angular.forEach(labelsToUse, function(label) {
            var val = entry[label] ? parseFloat(entry[label]) : 0;
            if (val > 0) {
                chartLabels.push(label);
                chartData.push(val);
            }
        });

        // Sum "Other" if Top-N is active
        if (topN > 0 && filteredLabelSet.length > topN) {
            angular.forEach(filteredLabelSet, function(label) {
                if (labelsToUse.indexOf(label) === -1) {
                    otherVal += entry[label] ? parseFloat(entry[label]) : 0;
                }
            });
            if (otherVal > 0) {
                chartLabels.push('Other');
                chartData.push(otherVal);
            }
        }

        scope.donutChart = {
            labels: chartLabels,
            datasets: [{ data: chartData }],
            displayMode: 'donut',
            periodLabel: periodLabel
        };
    }
}]);
