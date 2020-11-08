angular.module('mturk').factory('dataService', ['$http', '$cacheFactory', function($http, $cacheFactory) {

    var cache = $cacheFactory('demographicsCache');

	return {
	    loadDemographicsSurvey: function(from, to, success, error) {
	        var key = from + '_' + to;
	        var fromCache = cache.get(key);
            if(!fromCache) {
                $http.get(this.getApiUrl() + '/survey/demographics/aggregatedAnswers?from=' + from + '&to=' + to)
                .success(function(response) {
                    cache.put(key, response);
                
                    if(angular.isFunction(success)){
                        success(response);
                    }
                }).error(error);
            } else {
                if(angular.isFunction(success)){
                    success(fromCache);
                }
            }
	    },
	    getApiUrl: function(){
			return window.location.host.indexOf('localhost', 0) == 0 ? 
			    'http://localhost:8080/_ah/api/survey/v1' : 
			    'https://mturk-demographics.appspot.com/_ah/api/survey/v1';
	    }
	};
}]);