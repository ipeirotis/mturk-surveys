/**
 * @description Chart.js Directive for AngularJS
 * Replaces the former Google Charts directive with Chart.js
 */
(function (window, angular) {
    'use strict';

    angular.module('googlechart', [])

        .directive('chartjsChart', ['$timeout', function ($timeout) {
            return {
                restrict: 'A',
                scope: {
                    chartData: '=chartjsChart'
                },
                link: function ($scope, $elm) {
                    var chart = null;

                    // A curated palette for stacked bar charts
                    var palette = [
                        '#4285F4', '#EA4335', '#FBBC04', '#34A853', '#FF6D01',
                        '#46BDC6', '#7B1FA2', '#C2185B', '#0097A7', '#689F38',
                        '#F06292', '#BA68C8', '#4DB6AC', '#FFD54F', '#A1887F',
                        '#90A4AE', '#E57373', '#81C784', '#64B5F6', '#FFB74D',
                        '#CE93D8', '#80DEEA', '#AED581', '#FFF176', '#BCAAA4'
                    ];

                    function buildConfig(data) {
                        var isArea = data.displayMode === 'area';
                        var datasets = [];
                        for (var i = 0; i < data.datasets.length; i++) {
                            var ds = data.datasets[i];
                            var color = palette[i % palette.length];
                            if (isArea) {
                                datasets.push({
                                    label: ds.label,
                                    data: ds.data,
                                    backgroundColor: color + 'B3',
                                    borderColor: color,
                                    borderWidth: 1.5,
                                    fill: true,
                                    tension: 0.3,
                                    pointRadius: 0,
                                    pointHitRadius: 6
                                });
                            } else {
                                datasets.push({
                                    label: ds.label,
                                    data: ds.data,
                                    backgroundColor: color,
                                    borderWidth: 0
                                });
                            }
                        }

                        return {
                            type: isArea ? 'line' : 'bar',
                            data: {
                                labels: data.labels,
                                datasets: datasets
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                animation: {
                                    duration: 300,
                                    easing: 'easeOutQuad'
                                },
                                plugins: {
                                    legend: {
                                        position: 'top',
                                        labels: {
                                            font: { size: 12 },
                                            boxWidth: 12,
                                            padding: 8
                                        }
                                    },
                                    tooltip: {
                                        callbacks: {
                                            label: function (context) {
                                                return context.dataset.label + ': ' + context.parsed.y.toFixed(2) + '%';
                                            }
                                        }
                                    },
                                    filler: {
                                        propagate: true
                                    }
                                },
                                scales: {
                                    x: {
                                        stacked: true,
                                        ticks: {
                                            font: { size: 11 },
                                            color: '#666',
                                            maxRotation: 45,
                                            minRotation: 45
                                        }
                                    },
                                    y: {
                                        stacked: true,
                                        min: 0,
                                        max: 100,
                                        ticks: {
                                            font: { size: 11 },
                                            color: '#666',
                                            callback: function (value) {
                                                return value + '%';
                                            },
                                            stepSize: 20
                                        },
                                        grid: {
                                            color: '#e0e0e0'
                                        }
                                    }
                                }
                            }
                        };
                    }

                    function renderChart() {
                        var data = $scope.chartData;
                        if (!data || !data.labels || data.labels.length === 0) {
                            return;
                        }

                        if (chart) {
                            chart.destroy();
                        }

                        var canvas = $elm[0].querySelector('canvas');
                        if (!canvas) {
                            canvas = document.createElement('canvas');
                            $elm[0].appendChild(canvas);
                        }

                        var ctx = canvas.getContext('2d');
                        chart = new Chart(ctx, buildConfig(data));
                    }

                    $scope.$watch('chartData', function (newVal) {
                        if (newVal && newVal.labels && newVal.labels.length > 0) {
                            $timeout(renderChart, 0);
                        }
                    }, true);

                    $scope.$on('$destroy', function () {
                        if (chart) {
                            chart.destroy();
                            chart = null;
                        }
                    });
                }
            };
        }]);

})(window, window.angular);
