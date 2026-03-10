angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', '$timeout', 'dataService', 'dateFilterState',
    function ($scope, $filter, $routeParams, $timeout, dataService, dateFilterState) {

    // Thresholds for auto-downsampling daily data
    var WEEKLY_THRESHOLD = 90;   // >90 days -> group by week
    var MONTHLY_THRESHOLD = 365; // >365 days -> group by month

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

    // --- Downsampling helpers ---

    // Returns a group key for the given date at the specified granularity.
    // For 'weekly': key is the Monday date as "YYYY-MM-DD".
    // For 'monthly': key is "YYYY-MM".
    function getGroupKey(date, granularity) {
        if (granularity === 'monthly') {
            return date.getFullYear() + '-' + (date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1);
        }
        // weekly: snap to Monday
        var d = new Date(date.getTime());
        var day = d.getDay() || 7; // Mon=1 ... Sun=7
        d.setDate(d.getDate() - day + 1);
        return d.getFullYear() + '-'
            + (d.getMonth() < 9 ? '0' : '') + (d.getMonth() + 1) + '-'
            + (d.getDate() < 10 ? '0' : '') + d.getDate();
    }

    function formatGroupLabel(key, granularity) {
        if (granularity === 'monthly') {
            var parts = key.split('-');
            return MONTH_ABBR[parseInt(parts[1]) - 1] + ' ' + parts[0];
        }
        // weekly: key is YYYY-MM-DD (Monday of the week)
        var parts = key.split('-');
        return parseInt(parts[1]) + '/' + parseInt(parts[2]) + '/' + parts[0];
    }

    function chooseGranularity(numPeriods) {
        if (numPeriods > MONTHLY_THRESHOLD) return 'monthly';
        if (numPeriods > WEEKLY_THRESHOLD) return 'weekly';
        return 'daily';
    }

    function granularityLabel(gran, numDays) {
        if (gran === 'weekly') return 'Showing weekly averages (' + numDays + ' days)';
        if (gran === 'monthly') return 'Showing monthly averages (' + numDays + ' days)';
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

        dataService.loadDemographicsSurvey(fromStr, toStr, function(response){
            $scope.response = response;
            $scope.drawnCharts = [];
            $scope.draw($scope.visibleChart);
        }, function(error){
            console.log(error);
        });

        dataService.loadDemographicsCounts(fromStr, toStr, function(counts){
            $scope.countsData = counts;
            buildSummaryStats(counts);
            // If volume chart is visible, redraw it
            var idx = $scope.drawnCharts.indexOf('volumeChart');
            if (idx >= 0) {
                $scope.drawnCharts.splice(idx, 1);
            }
            if ($scope.visibleChart === 'volumeChart') {
                $scope.draw('volumeChart');
            }
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

        var granularity = chooseGranularity(days.length);
        scope.volumeGranularity = granularityLabel(granularity, days.length);

        if (granularity !== 'daily') {
            var groups = {};
            var groupOrder = [];

            for (var i = 0; i < days.length; i++) {
                var date = parseCountsDate(days[i].date);
                var key = getGroupKey(date, granularity);
                if (!groups[key]) {
                    groups[key] = 0;
                    groupOrder.push(key);
                }
                groups[key] += days[i].totalResponses;
            }

            var labels = [];
            var data = [];
            for (var g = 0; g < groupOrder.length; g++) {
                labels.push(formatGroupLabel(groupOrder[g], granularity));
                data.push(groups[groupOrder[g]]);
            }

            scope.volumeChart = {
                labels: labels,
                datasets: [{ label: 'Responses', data: data }],
                displayMode: 'volume'
            };
        } else {
            var labels = [];
            var data = [];
            for (var i = 0; i < days.length; i++) {
                var parts = days[i].date.split('-');
                labels.push(parseInt(parts[1]) + '/' + parseInt(parts[2]) + '/' + parts[0]);
                data.push(days[i].totalResponses);
            }

            scope.volumeChart = {
                labels: labels,
                datasets: [{ label: 'Responses', data: data }],
                displayMode: 'volume'
            };
        }
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

        var granularity = 'daily';
        var groups = null;
        var groupOrder = null;

        if (type === 'daily') {
            periods.sort(function(a, b) { return new Date(a) - new Date(b); });

            granularity = chooseGranularity(periods.length);
            scope.dailyGranularity = granularityLabel(granularity, periods.length);

            if (granularity !== 'daily') {
                groups = {};
                groupOrder = [];

                for (var p = 0; p < periods.length; p++) {
                    var date = new Date(periods[p]);
                    var key = getGroupKey(date, granularity);
                    if (!groups[key]) {
                        groups[key] = [];
                        groupOrder.push(key);
                    }
                    groups[key].push(periods[p]);
                }

                // Build new periodData by averaging percentages
                var newPeriodData = {};
                for (var g = 0; g < groupOrder.length; g++) {
                    var gKey = groupOrder[g];
                    var members = groups[gKey];
                    var avgMap = {};

                    angular.forEach(labelSet, function(label) {
                        var sum = 0;
                        var count = 0;
                        for (var m = 0; m < members.length; m++) {
                            var entry = periodData[members[m]];
                            if (entry && entry[label] !== undefined) {
                                sum += parseFloat(entry[label]);
                                count++;
                            }
                        }
                        avgMap[label] = count > 0 ? sum / count : 0;
                    });

                    newPeriodData[gKey] = avgMap;
                }

                periodData = newPeriodData;
                periods = groupOrder;
            }
        } else if (type === 'hourly') {
            periods.sort(function(a, b) { return parseInt(a) - parseInt(b); });
        }

        var chartLabels = [];
        for (var p = 0; p < periods.length; p++) {
            if (type === 'daily') {
                if (granularity === 'monthly') {
                    chartLabels.push(formatGroupLabel(periods[p], 'monthly'));
                } else if (granularity === 'weekly') {
                    chartLabels.push(formatGroupLabel(periods[p], 'weekly'));
                } else {
                    var d = new Date(periods[p]);
                    chartLabels.push((d.getMonth()+1) + '/' + d.getDate() + '/' + d.getFullYear());
                }
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

            if (granularity !== 'daily' && groups && groupOrder) {
                // Build a lookup from yyyy-MM-dd date to day counts
                var dayCountsByDate = {};
                for (var di = 0; di < days.length; di++) {
                    dayCountsByDate[days[di].date] = days[di];
                }

                var demoFields = ['countries', 'yearOfBirth', 'gender', 'maritalStatus',
                    'householdSize', 'householdIncome', 'educationalLevel',
                    'timeSpentOnMturk', 'weeklyIncomeFromMturk', 'languagesSpoken'];

                for (var gi = 0; gi < groupOrder.length; gi++) {
                    var gKey = groupOrder[gi];
                    var members = groups[gKey];
                    var aggDay = { totalResponses: 0 };

                    for (var fi = 0; fi < demoFields.length; fi++) {
                        aggDay[demoFields[fi]] = {};
                    }

                    for (var mi = 0; mi < members.length; mi++) {
                        // Convert Java date string back to yyyy-MM-dd for lookup
                        var memberDate = new Date(members[mi]);
                        var dateKey = memberDate.getFullYear() + '-'
                            + (memberDate.getMonth() < 9 ? '0' : '') + (memberDate.getMonth() + 1) + '-'
                            + (memberDate.getDate() < 10 ? '0' : '') + memberDate.getDate();
                        var dayData = dayCountsByDate[dateKey];
                        if (dayData) {
                            aggDay.totalResponses += dayData.totalResponses || 0;
                            for (var fi = 0; fi < demoFields.length; fi++) {
                                var field = demoFields[fi];
                                if (dayData[field]) {
                                    for (var k in dayData[field]) {
                                        aggDay[field][k] = (aggDay[field][k] || 0) + dayData[field][k];
                                    }
                                }
                            }
                        }
                    }

                    countsPerPeriod[chartLabels[gi]] = aggDay;
                }
            } else {
                // Original daily logic
                for (var di = 0; di < days.length; di++) {
                    var day = days[di];
                    var dp = day.date.split('-');
                    var labelKey = parseInt(dp[1]) + '/' + parseInt(dp[2]) + '/' + dp[0];
                    countsPerPeriod[labelKey] = day;
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
