angular.module('mturk').factory('loading', function() {
    var service = {
            requestCount: 0,
            message: $('<div id="loading"/>'),
            show: function() {
                this.requestCount++;
                var width = $(window).width();

                if ($("#loading").length === 0) {
                    this.message.appendTo($(document.body));
                }

                this.message
                .addClass('notification')
                .addClass('inf')
                .css('left', width / 2 - this.message.width() / 2)
                .text('Loading ...')
                .show();
            },
            hide: function() {
                this.requestCount--;
                if (this.requestCount === 0) {
                    this.message.hide();
                }
            },
            isLoading: function() {
                return this.requestCount > 0;
            }
    };
    return service;
});
