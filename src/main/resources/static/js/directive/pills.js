angular.module('mturk').directive('pills', function($location) {
	  return {
	    restrict: 'A',
	    link: function($scope, element, attrs, ctrl) {
	          $scope.$watch(attrs.ngModel, function(newValue, oldValue) {
	  	        element.find('li').each(function(k, li) {
	  	          var $li = angular.element(li);
	  	          var $a = $li.find("a");
  		          if($a.attr('id') == newValue) {
  		        	  $li.addClass('active');
  		          } else {
  		        	  $li.removeClass('active');
  		          }
	  	      });
	        },true); 

	    }
	 };
});