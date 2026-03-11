/**
 * Chart.js wrapper component - replaces ng-google-chart.js directive
 */
const ChartjsChart = {
    props: {
        chartData: { type: Object, default: null }
    },
    template: '<div ref="container" style="width:100%;height:100%;"></div>',
    setup(props) {
        const { ref, watch, onMounted, onUnmounted, nextTick } = Vue;
        const container = ref(null);
        var chart = null;
        var focusedIndex = null;

        var palette = [
            '#4285F4', '#EA4335', '#FBBC04', '#34A853', '#FF6D01',
            '#46BDC6', '#7B1FA2', '#C2185B', '#0097A7', '#689F38',
            '#F06292', '#BA68C8', '#4DB6AC', '#FFD54F', '#A1887F',
            '#90A4AE', '#E57373', '#81C784', '#64B5F6', '#FFB74D',
            '#CE93D8', '#80DEEA', '#AED581', '#FFF176', '#BCAAA4'
        ];

        function buildDonutConfig(data) {
            var colors = [];
            for (var i = 0; i < data.labels.length; i++) {
                colors.push(palette[i % palette.length]);
            }
            return {
                type: 'doughnut',
                data: {
                    labels: data.labels,
                    datasets: [{
                        data: data.datasets[0].data,
                        backgroundColor: colors,
                        borderWidth: 2,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '45%',
                    animation: { duration: 400, easing: 'easeOutQuad' },
                    plugins: {
                        legend: {
                            position: 'right',
                            labels: { font: { size: 12 }, boxWidth: 14, padding: 10 }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    var label = context.label || '';
                                    var value = context.parsed;
                                    return label + ': ' + value.toFixed(1) + '%';
                                }
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
                    animation: { duration: 300, easing: 'easeOutQuad' },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return context.parsed.y.toLocaleString() + ' responses';
                                }
                            }
                        }
                    },
                    scales: {
                        x: { display: false },
                        y: {
                            min: 0,
                            ticks: { font: { size: 10 }, color: '#999', maxTicksLimit: 3 },
                            grid: { color: '#e8e8e8' }
                        }
                    }
                }
            };
        }

        function buildConfig(data) {
            if (data.displayMode === 'donut') return buildDonutConfig(data);
            if (data.displayMode === 'volume') return buildVolumeConfig(data);

            var isArea = data.displayMode === 'area';
            var isLine = data.displayMode === 'line';
            var datasets = [];
            for (var i = 0; i < data.datasets.length; i++) {
                var ds = data.datasets[i];
                var color = palette[i % palette.length];
                if (isLine) {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        borderColor: color, backgroundColor: color,
                        borderWidth: 2, fill: false, tension: 0.3,
                        pointRadius: 2, pointHitRadius: 8, pointBackgroundColor: color
                    });
                } else if (isArea) {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        backgroundColor: color + 'B3', borderColor: color,
                        borderWidth: 1.5, fill: true, tension: 0.3,
                        pointRadius: 0, pointHitRadius: 6
                    });
                } else {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        backgroundColor: color, borderWidth: 0
                    });
                }
            }

            var countsPerPeriod = data.countsPerPeriod;
            var demographicField = data.demographicField;
            var stacked = !isLine;
            var chartType = (isArea || isLine) ? 'line' : 'bar';
            var interactionMode = isLine ? 'nearest' : 'index';

            return {
                type: chartType,
                data: { labels: data.labels, datasets: datasets },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: interactionMode, intersect: isLine },
                    animation: { duration: 300, easing: 'easeOutQuad' },
                    plugins: {
                        legend: {
                            position: 'top',
                            labels: { font: { size: 12 }, boxWidth: 12, padding: 8 },
                            onClick: function(e, legendItem, legend) {
                                var ci = legend.chart;
                                var clickedIdx = legendItem.datasetIndex;

                                if (focusedIndex === clickedIdx) {
                                    focusedIndex = null;
                                    ci.data.datasets.forEach(function(ds, i) {
                                        if (isLine) {
                                            ds.borderColor = palette[i % palette.length];
                                            ds.backgroundColor = palette[i % palette.length];
                                            ds.borderWidth = 2;
                                        } else if (isArea) {
                                            ds.backgroundColor = palette[i % palette.length] + 'B3';
                                            ds.borderColor = palette[i % palette.length];
                                        } else {
                                            ds.backgroundColor = palette[i % palette.length];
                                        }
                                        ci.setDatasetVisibility(i, true);
                                    });
                                } else {
                                    focusedIndex = clickedIdx;
                                    ci.data.datasets.forEach(function(ds, i) {
                                        ci.setDatasetVisibility(i, true);
                                        if (i === clickedIdx) {
                                            if (isLine) {
                                                ds.borderColor = palette[i % palette.length];
                                                ds.backgroundColor = palette[i % palette.length];
                                                ds.borderWidth = 3;
                                            } else if (isArea) {
                                                ds.backgroundColor = palette[i % palette.length] + 'B3';
                                                ds.borderColor = palette[i % palette.length];
                                            } else {
                                                ds.backgroundColor = palette[i % palette.length];
                                            }
                                        } else {
                                            if (isLine) {
                                                ds.borderColor = palette[i % palette.length] + '30';
                                                ds.backgroundColor = palette[i % palette.length] + '30';
                                                ds.borderWidth = 1;
                                            } else if (isArea) {
                                                ds.backgroundColor = palette[i % palette.length] + '20';
                                                ds.borderColor = palette[i % palette.length] + '40';
                                            } else {
                                                ds.backgroundColor = palette[i % palette.length] + '30';
                                            }
                                        }
                                    });
                                }
                                ci.update();
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    var pct = context.parsed.y.toFixed(2) + '%';
                                    var label = context.dataset.label;
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
                        filler: { propagate: true }
                    },
                    scales: {
                        x: {
                            stacked: stacked,
                            ticks: { font: { size: 11 }, color: '#666', maxRotation: 45, minRotation: 45 }
                        },
                        y: {
                            stacked: stacked,
                            min: 0,
                            max: data.autoScaleY ? undefined : 100,
                            ticks: {
                                font: { size: 11 }, color: '#666',
                                callback: function(value) { return value + '%'; },
                                stepSize: data.autoScaleY ? undefined : 20
                            },
                            grid: { color: '#e0e0e0' }
                        }
                    }
                }
            };
        }

        function renderChart() {
            focusedIndex = null;
            var data = props.chartData;
            if (!data || !data.labels || data.labels.length === 0) return;

            var el = container.value;
            if (!el || el.offsetParent === null) return;

            if (chart) {
                chart.destroy();
                chart = null;
            }

            var canvas = el.querySelector('canvas');
            if (!canvas) {
                canvas = document.createElement('canvas');
                el.appendChild(canvas);
            }

            var ctx = canvas.getContext('2d');
            chart = new Chart(ctx, buildConfig(data));
        }

        watch(function() { return props.chartData; }, function(newVal) {
            if (newVal && newVal.labels && newVal.labels.length > 0) {
                nextTick(renderChart);
            }
        }, { deep: true });

        onUnmounted(function() {
            if (chart) {
                chart.destroy();
                chart = null;
            }
        });

        return { container };
    }
};
