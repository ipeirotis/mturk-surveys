angular.module('mturk').factory('chartUtils', function() {

	return {
		populate: function(chart, labels, data) {
            var rows = new Array();
            for (var propName in data) {
                var row = {c:[{v: new Date(propName)}]};
                var i = data[propName];
                angular.forEach(labels, function(label){
                    var val = i[label] ? i[label] : 0;
                    row.c.push({v: parseFloat(val).toFixed(2)});
                    row.c.push({v: label + ': ' + parseFloat(val).toFixed(2) + '%'});
                });
                rows.push(row);
            };

            var cols = [{label: "Date", type: "date"}];
            angular.forEach(labels, function(label){
                cols.push({label: label, type: "number"});
                cols.push({type:'string', role:'tooltip'});
            });

            chart.data.cols = cols;
            chart.data.rows= rows;
		}
	};
});
