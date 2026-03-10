angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', 'dataService', 'dateFilterState',
    function ($scope, $filter, $routeParams, dataService, dateFilterState) {

    $scope.from = dateFilterState.from;
    $scope.to = dateFilterState.to;
    $scope.activePill = 'dailyChartPill';
    $scope.chartIds = ['hourlyChart', 'dailyChart', 'weeklyChart'];
    $scope.drawnCharts = [];
    $scope.visibleChart = 'dailyChart';

    $scope.hourlyChart = {};
    $scope.dailyChart = {};
    $scope.weeklyChart = {};
    $scope.displayMode = 'bar';

    $scope.setDisplayMode = function(mode) {
        $scope.displayMode = mode;
        // Re-render the currently visible chart by assigning a new object
        // reference so AngularJS change detection reliably fires the
        // directive's deep watch (mutating a property on the same reference
        // can be missed by the = binding watcher in AngularJS 1.2.x).
        var name = $scope.visibleChart;
        if ($scope[name] && $scope[name].labels) {
            var copy = angular.copy($scope[name]);
            copy.displayMode = mode;
            $scope[name] = copy;
        }
        // Mark non-visible charts as not-drawn so they pick up the new
        // displayMode when the user switches tabs.
        var kept = [];
        for (var i = 0; i < $scope.drawnCharts.length; i++) {
            if ($scope.drawnCharts[i] === name) {
                kept.push(name);
            }
        }
        $scope.drawnCharts = kept;
    };

    $scope.$watch('from+to', function(newValue, oldValue) {
        if($scope.from && $scope.to){
            dateFilterState.from = $scope.from;
            dateFilterState.to = $scope.to;
            $scope.load();
        }
    });

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
            populate($scope, chart, $scope.response, chart.substr(0, chart.length-5),
                    $routeParams.id);
        }
    };

    $scope.load = function(){
        dataService.loadDemographicsSurvey($filter('date')($scope.from, 'MM/dd/yyyy'),
                $filter('date')($scope.to, 'MM/dd/yyyy'), function(response){
            $scope.response = response;
            $scope.drawnCharts = [];
            $scope.draw($scope.visibleChart);
        }, function(error){
            console.log(error);
        });
    };

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

    function populate(scope, chartName, data, type, id) {
        var periodData = data[type][id];
        var labelSet = data[type].labels[id];

        if (!periodData || !labelSet) {
            return;
        }

        // Build sorted list of period keys
        var periods = [];
        for (var propName in periodData) {
            periods.push(propName);
        }
        if (periods.length === 0) {
            return;
        }

        // Sort periods
        if (type === 'daily') {
            periods.sort(function(a, b) { return new Date(a) - new Date(b); });
        } else if (type === 'hourly') {
            periods.sort(function(a, b) { return parseInt(a) - parseInt(b); });
        }
        // weekly: keep original order (Sun-Sat)

        // Format labels for display
        var chartLabels = [];
        for (var p = 0; p < periods.length; p++) {
            if (type === 'daily') {
                var d = new Date(periods[p]);
                chartLabels.push((d.getMonth()+1) + '/' + d.getDate() + '/' + d.getFullYear());
            } else {
                chartLabels.push(periods[p]);
            }
        }

        // Build datasets: one per demographic label
        var datasets = [];
        angular.forEach(labelSet, function(label) {
            var values = [];
            for (var pp = 0; pp < periods.length; pp++) {
                var entry = periodData[periods[pp]];
                values.push(entry[label] ? parseFloat(entry[label]) : 0);
            }
            datasets.push({ label: label, data: values });
        });

        scope[chartName] = { labels: chartLabels, datasets: datasets, displayMode: scope.displayMode };
    }
}]);
