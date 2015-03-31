angular.module('mturk').controller('CountriesController', ['$scope', '$filter', 'dataService', 'chartUtils',
    function ($scope, $filter, dataService, chartUtils) {

    $scope.from = new Date();
    $scope.from.setMonth(new Date().getMonth() - 1);
    $scope.to = new Date();

    $scope.chart = {
            type: 'ColumnChart',
            options: {
                bar: { groupWidth: '10%'},
                isStacked: true,
                vAxis: { minValue: 0, maxValue: 100, format: '#\'%\''} 
            },
            data: {"cols": [], "rows": []}
    };

    $scope.$watch('from+to', function(newValue, oldValue) {
        if($scope.from && $scope.to){
            $scope.load();
        }
    });

    $scope.load = function(){
        dataService.loadDemographicsSurvey($filter('date')($scope.from, 'MM/dd/yyyy'), 
                $filter('date')($scope.to, 'MM/dd/yyyy'), function(response){
            chartUtils.populate($scope.chart, response.labels.countries, response.byCountry);

        }, function(error){
            console.log(error);
        });
    };

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