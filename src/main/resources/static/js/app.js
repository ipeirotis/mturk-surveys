angular.module('mturk', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'googlechart'])
.config(['$routeProvider', 'views', function($routeProvider, views) {
    $routeProvider
    .when('/:id/:country', {templateUrl: views.chart, controller: 'ChartController'})
    .otherwise({redirectTo: '/gender/all'});
}])

.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('interceptor');
}]);
