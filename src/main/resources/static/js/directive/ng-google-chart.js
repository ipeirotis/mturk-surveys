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
                        if (data.displayMode === 'volume') {
                            return buildVolumeConfig(data);
                        }
                        var isArea = data.displayMode === 'area';
                        var isLine = data.displayMode === 'line';
                        var datasets = [];
                        for (var i = 0; i < data.datasets.length; i++) {
                            var ds = data.datasets[i];
                            var color = palette[i % palette.length];
                            if (isLine) {
                                datasets.push({
                                    label: ds.label,
                                    data: ds.data,
                                    borderColor: color,
                                    backgroundColor: color,
                                    borderWidth: 2,
                                    fill: false,
                                    tension: 0.3,
                                    pointRadius: 2,
                                    pointHitRadius: 8,
                                    pointBackgroundColor: color
                                });
                            } else if (isArea) {
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

                        // Build tooltip with counts
                        var countsPerPeriod = data.countsPerPeriod;
                        var demographicField = data.demographicField;

                        var stacked = !isLine;
                        var chartType = (isArea || isLine) ? 'line' : 'bar';

                        // For line mode, use 'nearest' interaction so hovering
                        // highlights only the closest line instead of all series.
                        var interactionMode = isLine ? 'nearest' : 'index';

                        return {
                            type: chartType,
                            data: {
                                labels: data.labels,
                                datasets: datasets
                            },
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                interaction: {
                                    mode: interactionMode,
                                    intersect: isLine ? true : false
                                },
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
                                                var pct = context.parsed.y.toFixed(2) + '%';
                                                var label = context.dataset.label;
                                                // Try to get absolute count
                                                if (countsPerPeriod && demographicField) {
                                                    var periodLabel = data.labels[context.dataIndex];
                                                    var dayData = countsPerPeriod[periodLabel];
                                                    if (dayData && dayData[demographicField]) {
                                                        var count = dayData[demographicField][label];
                                                        if (count !== undefined) {
                                                            var total = dayData.totalResponses || 0;
                                                            return label + ': ' + pct + ' (' + count.toLocaleString() + ' of ' + total.toLocaleString() + ')';
                                                        }
                                                    }
                                                }
                                                return label + ': ' + pct;
                                            },
                                            footer: function(tooltipItems) {
                                                if (countsPerPeriod && tooltipItems.length > 0) {
                                                    var periodLabel = data.labels[tooltipItems[0].dataIndex];
                                                    var dayData = countsPerPeriod[periodLabel];
                                                    if (dayData) {
                                                        return 'Total: ' + (dayData.totalResponses || 0).toLocaleString() + ' responses';
                                                    }
                                                }
                                                return '';
                                            }
                                        }
                                    },
                                    filler: {
                                        propagate: true
                                    }
                                },
                                scales: {
                                    x: {
                                        stacked: stacked,
                                        ticks: {
                                            font: { size: 11 },
                                            color: '#666',
                                            maxRotation: 45,
                                            minRotation: 45
                                        }
                                    },
                                    y: {
                                        stacked: stacked,
                                        min: 0,
                                        max: data.autoScaleY ? undefined : 100,
                                        ticks: {
                                            font: { size: 11 },
                                            color: '#666',
                                            callback: function (value) {
                                                return value + '%';
                                            },
                                            stepSize: data.autoScaleY ? undefined : 20
                                        },
                                        grid: {
                                            color: '#e0e0e0'
                                        }
                                    }
                                }
                            }
                        };
                    }

                    function buildVolumeConfig(data) {
                        var ds = data.datasets[0];
                        return {
                            type: 'bar',
                            data: {
                                labels: data.labels,
                                datasets: [{
                                    label: ds.label,
                                    data: ds.data,
                                    backgroundColor: '#4285F4' + '99',
                                    borderColor: '#4285F4',
                                    borderWidth: 0,
                                    barPercentage: 1.0,
                                    categoryPercentage: 1.0
                                }]
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
                                        display: false
                                    },
                                    tooltip: {
                                        callbacks: {
                                            label: function (context) {
                                                return context.parsed.y.toLocaleString() + ' responses';
                                            }
                                        }
                                    }
                                },
                                scales: {
                                    x: {
                                        display: false
                                    },
                                    y: {
                                        min: 0,
                                        ticks: {
                                            font: { size: 10 },
                                            color: '#999',
                                            maxTicksLimit: 3
                                        },
                                        grid: {
                                            color: '#e8e8e8'
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

                        // Skip rendering when the container is hidden;
                        // Chart.js cannot measure a display:none element.
                        if ($elm[0].offsetParent === null) {
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
