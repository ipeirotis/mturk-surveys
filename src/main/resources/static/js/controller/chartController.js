angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', 'dataService',
    function ($scope, $filter, $routeParams, dataService) {

    $scope.from = new Date();
    $scope.from.setMonth(new Date().getMonth() - 3);
    $scope.to = new Date();
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
    };

    $scope.$watch('from+to', function(newValue, oldValue) {
        if($scope.from && $scope.to){
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
                    $routeParams.country, $routeParams.id);
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

    function populate(scope, chartName, data, type, country, id) {
        var periodData = data[type][country][id];
        var labelSet = data[type][country].labels[id];

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

        scope[chartName] = { labels: chartLabels, datasets: datasets };
    }
}]);
