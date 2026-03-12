/**
 * Chart.js Vue Component
 * Replaces the chartjsChart AngularJS directive
 */
const ChartjsChart = {
    props: {
        chartData: { type: Object, default: null }
    },
    template: '<div ref="container" style="display:block;"></div>',
    setup(props) {
        const { ref, watch, onMounted, onUnmounted, nextTick } = Vue;
        const container = ref(null);
        let chart = null;

        const palette = [
            '#4285F4', '#EA4335', '#FBBC04', '#34A853', '#FF6D01',
            '#46BDC6', '#7B1FA2', '#C2185B', '#0097A7', '#689F38',
            '#F06292', '#BA68C8', '#4DB6AC', '#FFD54F', '#A1887F',
            '#90A4AE', '#E57373', '#81C784', '#64B5F6', '#FFB74D',
            '#CE93D8', '#80DEEA', '#AED581', '#FFF176', '#BCAAA4'
        ];

        let focusedIndex = null;

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
                        borderColor: '#fff',
                        hoverBorderWidth: 3,
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '50%',
                    animation: { duration: 500, easing: 'easeOutQuart' },
                    plugins: {
                        legend: {
                            position: 'right',
                            labels: { font: { size: 12, weight: '500' }, boxWidth: 14, padding: 12, usePointStyle: true, pointStyle: 'rectRounded' }
                        },
                        tooltip: {
                            backgroundColor: 'rgba(30, 41, 59, 0.92)',
                            titleFont: { size: 13, weight: '600' },
                            bodyFont: { size: 12 },
                            padding: 10,
                            cornerRadius: 6,
                            callbacks: {
                                label: function(context) {
                                    return (context.label || '') + ': ' + context.parsed.toFixed(1) + '%';
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
                        backgroundColor: function(context) {
                            var chart = context.chart;
                            var ctx = chart.ctx;
                            var area = chart.chartArea;
                            if (!area) return '#4285F466';
                            var gradient = ctx.createLinearGradient(0, area.top, 0, area.bottom);
                            gradient.addColorStop(0, '#4285F4AA');
                            gradient.addColorStop(1, '#4285F430');
                            return gradient;
                        },
                        borderColor: '#4285F4',
                        borderWidth: 0,
                        borderRadius: 1,
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
                            backgroundColor: 'rgba(30, 41, 59, 0.92)',
                            titleFont: { size: 12, weight: '600' },
                            bodyFont: { size: 11 },
                            padding: 8,
                            cornerRadius: 6,
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
                            ticks: { font: { size: 10 }, color: '#a0aec0', maxTicksLimit: 3 },
                            grid: { color: '#f0f0f0', drawBorder: false },
                            border: { display: false }
                        }
                    }
                }
            };
        }

        function buildVolumeLineConfig(data) {
            var ds = data.datasets[0];
            return {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: ds.label,
                        data: ds.data,
                        borderColor: '#4285F4',
                        backgroundColor: function(context) {
                            var chart = context.chart;
                            var ctx = chart.ctx;
                            var area = chart.chartArea;
                            if (!area) return '#4285F420';
                            var gradient = ctx.createLinearGradient(0, area.top, 0, area.bottom);
                            gradient.addColorStop(0, '#4285F430');
                            gradient.addColorStop(1, '#4285F405');
                            return gradient;
                        },
                        borderWidth: 1.5,
                        fill: true,
                        tension: 0.3,
                        pointRadius: 0,
                        pointHitRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: { duration: 300, easing: 'easeOutQuad' },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: 'rgba(30, 41, 59, 0.92)',
                            titleFont: { size: 12, weight: '600' },
                            bodyFont: { size: 11 },
                            padding: 8,
                            cornerRadius: 6,
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
                            ticks: { font: { size: 10 }, color: '#a0aec0', maxTicksLimit: 3 },
                            grid: { color: '#f0f0f0', drawBorder: false },
                            border: { display: false }
                        }
                    }
                }
            };
        }

        function buildConfig(data) {
            if (data.displayMode === 'donut') return buildDonutConfig(data);
            if (data.displayMode === 'volume') return buildVolumeConfig(data);
            if (data.displayMode === 'volumeLine') return buildVolumeLineConfig(data);

            var isArea = data.displayMode === 'area';
            var isLine = data.displayMode === 'line';
            var datasets = [];
            var numLabels = data.labels ? data.labels.length : 0;

            for (var i = 0; i < data.datasets.length; i++) {
                var ds = data.datasets[i];
                var color = palette[i % palette.length];
                if (isLine) {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        borderColor: color, backgroundColor: color,
                        borderWidth: 2.5, fill: false, tension: 0.35,
                        pointRadius: numLabels > 60 ? 0 : 2,
                        pointHoverRadius: 5,
                        pointHitRadius: 10,
                        pointBackgroundColor: '#fff',
                        pointBorderColor: color,
                        pointBorderWidth: 2
                    });
                } else if (isArea) {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        backgroundColor: color + 'B3', borderColor: color,
                        borderWidth: 1.5, fill: true, tension: 0.35,
                        pointRadius: 0, pointHitRadius: 6
                    });
                } else {
                    datasets.push({
                        label: ds.label, data: ds.data,
                        backgroundColor: color, borderWidth: 0,
                        borderRadius: numLabels <= 30 ? 2 : 0
                    });
                }
            }

            var countsPerPeriod = data.countsPerPeriod;
            var demographicField = data.demographicField;
            var stacked = !isLine;
            var chartType = (isArea || isLine) ? 'line' : 'bar';
            var interactionMode = isLine ? 'nearest' : 'index';

            // Smart tick skipping for x-axis
            var maxTicks = Math.min(numLabels, 20);

            return {
                type: chartType,
                data: { labels: data.labels, datasets: datasets },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: { mode: interactionMode, intersect: isLine },
                    animation: { duration: 400, easing: 'easeOutQuart' },
                    hover: { mode: interactionMode, intersect: isLine },
                    plugins: {
                        legend: {
                            position: 'top',
                            labels: {
                                font: { size: 12, weight: '500' },
                                boxWidth: 12, padding: 12,
                                usePointStyle: true,
                                pointStyle: isLine ? 'line' : 'rectRounded'
                            },
                            onClick: function(e, legendItem, legend) {
                                var ci = legend.chart;
                                var clickedIdx = legendItem.datasetIndex;

                                if (focusedIndex === clickedIdx) {
                                    focusedIndex = null;
                                    ci.data.datasets.forEach(function(ds, i) {
                                        if (isLine) {
                                            ds.borderColor = palette[i % palette.length];
                                            ds.backgroundColor = palette[i % palette.length];
                                            ds.pointBorderColor = palette[i % palette.length];
                                            ds.borderWidth = 2.5;
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
                                                ds.pointBorderColor = palette[i % palette.length];
                                                ds.borderWidth = 3.5;
                                            } else if (isArea) {
                                                ds.backgroundColor = palette[i % palette.length] + 'B3';
                                                ds.borderColor = palette[i % palette.length];
                                            } else {
                                                ds.backgroundColor = palette[i % palette.length];
                                            }
                                        } else {
                                            if (isLine) {
                                                ds.borderColor = palette[i % palette.length] + '25';
                                                ds.backgroundColor = palette[i % palette.length] + '25';
                                                ds.pointBorderColor = palette[i % palette.length] + '25';
                                                ds.borderWidth = 1;
                                            } else if (isArea) {
                                                ds.backgroundColor = palette[i % palette.length] + '18';
                                                ds.borderColor = palette[i % palette.length] + '30';
                                            } else {
                                                ds.backgroundColor = palette[i % palette.length] + '25';
                                            }
                                        }
                                    });
                                }
                                ci.update();
                            }
                        },
                        tooltip: {
                            backgroundColor: 'rgba(30, 41, 59, 0.92)',
                            titleFont: { size: 13, weight: '600' },
                            bodyFont: { size: 12 },
                            footerFont: { size: 11, weight: '400', style: 'italic' },
                            padding: 10,
                            cornerRadius: 6,
                            boxPadding: 4,
                            callbacks: {
                                label: function(context) {
                                    var pct = context.parsed.y.toFixed(1) + '%';
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
                            ticks: {
                                font: { size: 11 },
                                color: '#718096',
                                maxRotation: 45,
                                minRotation: 0,
                                autoSkip: true,
                                maxTicksLimit: maxTicks
                            },
                            grid: { display: false },
                            border: { color: '#e2e8f0' }
                        },
                        y: {
                            stacked: stacked,
                            min: 0,
                            max: data.autoScaleY ? undefined : 100,
                            ticks: {
                                font: { size: 11 }, color: '#718096',
                                callback: function(value) { return value + '%'; },
                                stepSize: data.autoScaleY ? undefined : 20
                            },
                            grid: { color: '#f0f4f8', drawBorder: false },
                            border: { display: false }
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

        onMounted(() => {
            if (props.chartData && props.chartData.labels && props.chartData.labels.length > 0) {
                renderChart();
            }
        });

        watch(() => props.chartData, (newVal) => {
            if (newVal && newVal.labels && newVal.labels.length > 0) {
                nextTick(renderChart);
            }
        }, { deep: true });

        onUnmounted(() => {
            if (chart) {
                chart.destroy();
                chart = null;
            }
        });

        return { container };
    }
};
