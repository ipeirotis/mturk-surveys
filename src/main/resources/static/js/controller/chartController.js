angular.module('mturk').controller('ChartController',
    ['$scope', '$filter', '$routeParams', 'dataService',
    function ($scope, $filter, $routeParams, dataService) {

    $scope.from = new Date();
    $scope.from.setMonth(new Date().getMonth() - 1);
    $scope.to = new Date();
    $scope.activePill = 'dailyChartPill';
    $scope.chartIds = ['hourlyChart', 'dailyChart', 'weeklyChart'];
    $scope.drawnCharts = [];
    $scope.visibleChart = 'dailyChart';

    $scope.hourlyChart = initChart();
    $scope.dailyChart = initChart();
    $scope.weeklyChart = initChart();

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
                if(drawn == false){
                    $('#'+chartId).css({visibility:'hidden'});
                }
                $('#'+chartId).css({display:'block'});
            }
        });

        if(drawn == false){
            populate($scope[chart], $scope.response, chart.substr(0, chart.length-5),
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

    function initChart(){
        return {
            type: 'ColumnChart',
            options: {
                bar: { groupWidth: '10%'},
                isStacked: true,
                vAxis: { minValue: 0, maxValue: 100, format: '#\'%\''}
            },
            data: {"cols": [], "rows": []}
        };
    };

    function populate(chart, data, type, country, id) {
        var rows = new Array();
        for (var propName in data[type][country][id]) {
            var row = {c:[{v: (type == 'hourly' || type == 'weekly')? propName : new Date(propName)}]};
            var i = data[type][country][id][propName];
            angular.forEach(data[type][country].labels[id], function(label){
                var val = i[label] ? parseFloat(i[label]) : 0;
                row.c.push({v: val});
                row.c.push({v: label + ': ' + val.toFixed(2) + '%'});
            });
            rows.push(row);
        };
        if(rows.length > 0) {
            var cols = new Array();
            if(type == 'hourly' || type == 'weekly') {
                cols.push({label: "Date", type: "string"});
            } else {
                cols.push({label: "Date", type: "date"});
            }
            angular.forEach(data[type][country].labels[id], function(label){
                cols.push({label: label, type: "number"});
                cols.push({type:'string', role:'tooltip'});
            });
            chart.data.cols = cols;
            chart.data.rows= rows;
        }
    };

    $scope.chartReady = function(chart) {
        $('#'+chart).css({visibility:'visible'});
        fixGoogleChartsBarsBootstrap();
    };

    function fixGoogleChartsBarsBootstrap() {
        // Google charts uses <img height="12px">, which is incompatible with Twitter
        // * bootstrap in responsive mode, which inserts a css rule for: img { height: auto; }.
        // *
        // * The fix is to use inline style width attributes, ie <img style="height: 12px;">.
        // * BUT we can't change the way Google Charts renders its bars. Nor can we change
        // * the Twitter bootstrap CSS and remain future proof.
        // *
        // * Instead, this function can be called after a Google charts render to "fix" the
        // * issue by setting the style attributes dynamically.

       $(".google-visualization-table-table img[width]").each(function(index, img) {
           $(img).css("width", $(img).attr("width")).css("height", $(img).attr("height"));
       });
    };
}]);