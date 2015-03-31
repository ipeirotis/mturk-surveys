angular.module('mturk').factory('interceptor',
  ['$rootScope', '$q', '$location', 'loading', function($rootScope, $q, $location, loading) {

  return {
    request: function(config) {
      loading.show();
      return config || $q.when(config);
    },
    response: function(response) {
      loading.hide();
      return response || $q.when(response);
    },
    responseError: function(rejection) {
      loading.hide();
      return $q.reject(rejection);
    }
  };
}]);
