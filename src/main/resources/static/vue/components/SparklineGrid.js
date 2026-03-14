/**
 * SparklineGrid Component
 * Renders a grid of small individual sparkline charts, one per category.
 * Useful for fields with many categories (countries, income brackets, etc.)
 * where a single stacked chart becomes unreadable.
 */
const SparklineGrid = {
    props: {
        chartData: { type: Object, default: null }
    },
    template: `
<div ref="container" class="sparkline-grid" v-if="items && items.length > 0">
    <div class="sparkline-item" v-for="item in items" :key="item.label">
        <div class="sparkline-header">
            <span class="sparkline-label">{{item.label}}</span>
            <span class="sparkline-value">{{item.latestValue}}</span>
        </div>
        <div style="position: relative; height: 40px;"><canvas :ref="el => { if (el) canvasRefs[item.label] = el; }"></canvas></div>
    </div>
</div>
<div v-else class="text-muted small" style="padding: 20px 0;">No data available for sparklines.</div>
`,
    setup(props) {
        const { ref, watch, onMounted, onUnmounted, nextTick } = Vue;
        const container = ref(null);
        const items = ref([]);
        const canvasRefs = {};
        let charts = {};

        const palette = [
            '#4285F4', '#EA4335', '#FBBC04', '#34A853', '#FF6D01',
            '#46BDC6', '#7B1FA2', '#C2185B', '#0097A7', '#689F38',
            '#F06292', '#BA68C8', '#4DB6AC', '#FFD54F', '#A1887F',
            '#90A4AE', '#E57373', '#81C784', '#64B5F6', '#FFB74D',
            '#CE93D8', '#80DEEA', '#AED581', '#FFF176', '#BCAAA4'
        ];

        function destroyCharts() {
            for (var key in charts) {
                if (charts[key]) charts[key].destroy();
            }
            charts = {};
        }

        function buildItems(data) {
            if (!data || !data.datasets || !data.labels || data.labels.length === 0) return [];

            var result = [];
            for (var i = 0; i < data.datasets.length; i++) {
                var ds = data.datasets[i];
                var lastVal = null;
                for (var j = ds.data.length - 1; j >= 0; j--) {
                    if (ds.data[j] !== null && ds.data[j] !== undefined) {
                        lastVal = ds.data[j];
                        break;
                    }
                }
                var formatted = lastVal !== null ? (lastVal < 1 ? lastVal.toFixed(2) + '%' : lastVal.toFixed(1) + '%') : 'N/A';
                result.push({
                    label: ds.label,
                    data: ds.data,
                    latestValue: formatted,
                    color: palette[i % palette.length],
                    total: ds.data.reduce(function(sum, v) { return sum + (v || 0); }, 0)
                });
            }

            // Sort by total descending
            result.sort(function(a, b) { return b.total - a.total; });
            return result;
        }

        function renderSparklines() {
            destroyCharts();
            var data = props.chartData;
            if (!data || !data.labels) return;

            items.value = buildItems(data);

            nextTick(function() {
                for (var i = 0; i < items.value.length; i++) {
                    var item = items.value[i];
                    var canvas = canvasRefs[item.label];
                    if (!canvas) continue;

                    var ctx = canvas.getContext('2d');
                    charts[item.label] = new Chart(ctx, {
                        type: 'line',
                        data: {
                            labels: data.labels,
                            datasets: [{
                                data: item.data,
                                borderColor: item.color,
                                backgroundColor: item.color + '20',
                                borderWidth: 1.5,
                                fill: true,
                                tension: 0.4,
                                pointRadius: 0,
                                pointHitRadius: 4
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            animation: { duration: 200 },
                            plugins: {
                                legend: { display: false },
                                tooltip: {
                                    backgroundColor: 'rgba(30, 41, 59, 0.92)',
                                    titleFont: { size: 11 },
                                    bodyFont: { size: 11 },
                                    padding: 6,
                                    cornerRadius: 4,
                                    displayColors: false,
                                    callbacks: {
                                        title: function(tooltipItems) {
                                            return data.labels[tooltipItems[0].dataIndex];
                                        },
                                        label: function(context) {
                                            var val = context.parsed.y;
                                            return item.label + ': ' + (val !== null ? val.toFixed(1) + '%' : 'N/A');
                                        }
                                    }
                                }
                            },
                            scales: {
                                x: { display: false },
                                y: {
                                    display: false,
                                    min: 0
                                }
                            }
                        }
                    });
                }
            });
        }

        onMounted(function() {
            if (props.chartData && props.chartData.labels) {
                renderSparklines();
            }
        });

        watch(function() { return props.chartData; }, function(newVal) {
            if (newVal && newVal.labels && newVal.labels.length > 0) {
                nextTick(renderSparklines);
            }
        }, { deep: true });

        onUnmounted(destroyCharts);

        return { container, items, canvasRefs };
    }
};
