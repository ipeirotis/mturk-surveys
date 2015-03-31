angular.module('mturk', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'googlechart'])
.config(['$routeProvider', 'views', function($routeProvider, views) {
    $routeProvider
    .when('/gender', {templateUrl: views.gender, controller: 'GenderSurveyController'})
    .when('/birth', {templateUrl: views.birth, controller: 'BirthSurveyController'})
    .when('/countries', {templateUrl: views.gender, controller: 'CountriesController'})
    .when('/maritalStatus', {templateUrl: views.gender, controller: 'MaritalStatusController'})
    .when('/householdSize', {templateUrl: views.gender, controller: 'HouseholdSizeController'})
    .when('/householdIncome', {templateUrl: views.gender, controller: 'HouseholdIncomeController'})
    .otherwise({redirectTo: '/gender'});
}])

.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('interceptor');
}]);
