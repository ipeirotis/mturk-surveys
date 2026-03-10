angular.module('mturk').directive('navpills', ['$location',
	function ($location) {
	  return {
	    restrict: 'A',
	    link: function postLink(scope, element, attrs, controller) {
	      scope.$watch(function () {
	        return $location.path();
	      }, function (newValue, oldValue) {
	        $('li[data-match-route]', element).each(function (k, li) {
	          var $li = angular.element(li), pattern = $li.attr('data-match-route'), regexp = new RegExp('^' + pattern + '$', ['i']);
	          var $a = $li.find('a');
	          if (regexp.test(newValue)) {
	            $a.addClass('active');
	          } else {
	            $a.removeClass('active');
	          }
	        });
	      });
	    }
	  };
	}
]);
