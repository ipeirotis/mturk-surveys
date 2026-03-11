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
                $http.get(this.getApiUrl() + '/survey/demographics/chartData?from=' + from + '&to=' + to, { timeout: 300000 })
                .then(function(response) {
                    chartDataCache.put(key, response.data);
                    if(angular.isFunction(success)){
                        success(response.data);
                    }
                }, function(resp) {
                    if(angular.isFunction(error)){
                        error(resp.data, resp.status, resp.headers, resp.config);
                    }
                });
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