angular.module('mturk', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'googlechart'])
.config(['$routeProvider', 'views', function($routeProvider, views) {
    $routeProvider
    .when('/:id', {templateUrl: views.chart, controller: 'ChartController'})
    .otherwise({redirectTo: '/gender'});
}])

.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('interceptor');
}]);
