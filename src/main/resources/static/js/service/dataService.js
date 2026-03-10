angular.module('mturk').factory('dateFilterState', function() {
    var defaultFrom = new Date();
    defaultFrom.setMonth(defaultFrom.getMonth() - 3);
    return {
        from: defaultFrom,
        to: new Date()
    };
});

angular.module('mturk').factory('dataService', ['$http', '$cacheFactory', function($http, $cacheFactory) {

    var cache = $cacheFactory('demographicsCache');
    var countsCache = $cacheFactory('countsCache');

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
	    loadDemographicsCounts: function(from, to, success, error) {
	        var key = 'counts_' + from + '_' + to;
	        var fromCache = countsCache.get(key);
            if(!fromCache) {
                $http.get(this.getApiUrl() + '/survey/demographics/counts?from=' + from + '&to=' + to)
                .success(function(response) {
                    countsCache.put(key, response);
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
			return '/api';
	    }
	};
}]);