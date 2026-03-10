angular.module('mturk').factory('dateFilterState', function() {
    var defaultFrom = new Date();
    defaultFrom.setMonth(defaultFrom.getMonth() - 3);
    return {
        from: defaultFrom,
        to: new Date()
    };
});

angular.module('mturk').factory('dataService', ['$http', '$cacheFactory', function($http, $cacheFactory) {

    var chartDataCache = $cacheFactory('chartDataCache');

	return {
	    /**
	     * Combined endpoint: loads both aggregated percentages and raw counts
	     * in a single XHR (halves Datastore reads).
	     * Callback receives { aggregated: {...}, counts: {...} }.
	     */
	    loadChartData: function(from, to, success, error) {
	        var key = 'chart_' + from + '_' + to;
	        var fromCache = chartDataCache.get(key);
            if(!fromCache) {
                $http.get(this.getApiUrl() + '/survey/demographics/chartData?from=' + from + '&to=' + to)
                .success(function(response) {
                    chartDataCache.put(key, response);
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