/**
 * ChartView component - replaces ChartController + chart.html
 */
const ChartView = {
    components: {
        'chartjs-chart': ChartjsChart,
        'choropleth-map': ChoroplethMap
    },
    props: {
        routeId: { type: String, required: true }
    },
    template: `
<!-- Summary Statistics Cards -->
<div class="row stats-cards" v-if="summaryStats">
    <div class="col-6 col-sm-3">
        <div class="stat-card">
            <div class="stat-value">
                {{summaryStats.totalResponses.toLocaleString()}}
                <span class="trend-arrow" v-if="summaryStats.totalTrend"
                    :class="{'trend-up': summaryStats.totalTrend.direction === 'up', 'trend-down': summaryStats.totalTrend.direction === 'down', 'trend-flat': summaryStats.totalTrend.direction === 'flat'}">
                    <i v-if="summaryStats.totalTrend.direction === 'up'" class="bi bi-arrow-up-short"></i>
                    <i v-if="summaryStats.totalTrend.direction === 'down'" class="bi bi-arrow-down-short"></i>
                    <i v-if="summaryStats.totalTrend.direction === 'flat'" class="bi bi-dash"></i>
                    <small v-if="summaryStats.totalTrend.pct > 0">{{summaryStats.totalTrend.pct}}%</small>
                </span>
            </div>
            <div class="stat-label">Total Responses</div>
        </div>
    </div>
    <div class="col-6 col-sm-3">
        <div class="stat-card">
            <div class="stat-value">
                {{summaryStats.avgPerPeriod.toLocaleString()}}
                <span class="trend-arrow" v-if="summaryStats.avgTrend"
                    :class="{'trend-up': summaryStats.avgTrend.direction === 'up', 'trend-down': summaryStats.avgTrend.direction === 'down', 'trend-flat': summaryStats.avgTrend.direction === 'flat'}">
                    <i v-if="summaryStats.avgTrend.direction === 'up'" class="bi bi-arrow-up-short"></i>
                    <i v-if="summaryStats.avgTrend.direction === 'down'" class="bi bi-arrow-down-short"></i>
                    <i v-if="summaryStats.avgTrend.direction === 'flat'" class="bi bi-dash"></i>
                    <small v-if="summaryStats.avgTrend.pct > 0">{{summaryStats.avgTrend.pct}}%</small>
                </span>
            </div>
            <div class="stat-label">{{summaryStats.avgLabel}}</div>
        </div>
    </div>
    <div class="col-6 col-sm-3">
        <div class="stat-card">
            <div class="stat-value">
                {{summaryStats.topCountry.label}}
                <span class="trend-arrow" v-if="summaryStats.countryTrend"
                    :class="{'trend-up': summaryStats.countryTrend.direction === 'up', 'trend-down': summaryStats.countryTrend.direction === 'down', 'trend-flat': summaryStats.countryTrend.direction === 'flat'}">
                    <i v-if="summaryStats.countryTrend.direction === 'up'" class="bi bi-arrow-up-short"></i>
                    <i v-if="summaryStats.countryTrend.direction === 'down'" class="bi bi-arrow-down-short"></i>
                    <i v-if="summaryStats.countryTrend.direction === 'flat'" class="bi bi-dash"></i>
                    <small v-if="summaryStats.countryTrend.pct > 0">{{summaryStats.countryTrend.pct}}%</small>
                </span>
            </div>
            <div class="stat-label">Top Country ({{summaryStats.topCountry.pct}}%)</div>
        </div>
    </div>
    <div class="col-6 col-sm-3">
        <div class="stat-card">
            <div class="stat-value">
                {{summaryStats.topGender.label}}
                <span class="trend-arrow" v-if="summaryStats.genderTrend"
                    :class="{'trend-up': summaryStats.genderTrend.direction === 'up', 'trend-down': summaryStats.genderTrend.direction === 'down', 'trend-flat': summaryStats.genderTrend.direction === 'flat'}">
                    <i v-if="summaryStats.genderTrend.direction === 'up'" class="bi bi-arrow-up-short"></i>
                    <i v-if="summaryStats.genderTrend.direction === 'down'" class="bi bi-arrow-down-short"></i>
                    <i v-if="summaryStats.genderTrend.direction === 'flat'" class="bi bi-dash"></i>
                    <small v-if="summaryStats.genderTrend.pct > 0">{{summaryStats.genderTrend.pct}}%</small>
                </span>
            </div>
            <div class="stat-label">Top Gender ({{summaryStats.topGender.pct}}%)</div>
        </div>
    </div>
</div>
<!-- Date Range Pickers -->
<div class="row">
    <div class="col-6 col-sm-3">
        <div class="input-group input-group-sm">
            <input type="date" class="form-control" :value="fromInput" @change="onFromChange" :min="minDateInput" :max="toInput" />
        </div>
    </div>
    <div class="col-6 col-sm-3">
        <div class="input-group input-group-sm">
            <input type="date" class="form-control" :value="toInput" @change="onToChange" :min="fromInput" :max="maxDateInput" />
        </div>
    </div>
    <div class="col-6 col-sm-3">
        <button type="button" class="btn btn-primary btn-sm" @click="applyDateRange">
            <i class="bi bi-arrow-clockwise"></i> Update
        </button>
    </div>
</div>
<!-- Display Mode (hidden for map views) -->
<div class="row" v-if="!isMapView">
    <div class="col-12 col-sm-8">
        <div class="btn-group btn-group-sm top-n-filter">
            <button type="button" class="btn" v-for="opt in topNOptions" :key="opt.value"
                :class="topN === opt.value ? 'btn-primary' : 'btn-outline-secondary'"
                @click="setTopN(opt.value)">{{opt.label}}</button>
        </div>
    </div>
    <div class="col-12 col-sm-4 text-end display-mode-btns">
        <div class="btn-group">
            <button type="button" class="btn btn-sm" :class="displayMode === 'bar' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('bar')">
                <i class="bi bi-bar-chart-fill"></i> Bars
            </button>
            <button type="button" class="btn btn-sm" :class="displayMode === 'area' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('area')">
                <i class="bi bi-graph-up"></i> Area
            </button>
            <button type="button" class="btn btn-sm" :class="displayMode === 'line' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('line')">
                <i class="bi bi-graph-down"></i> Line
            </button>
            <button type="button" class="btn btn-sm" :class="displayMode === 'donut' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('donut')">
                <i class="bi bi-pie-chart"></i> Donut
            </button>
        </div>
    </div>
</div>
<!-- Loading / Error -->
<div class="row" v-if="loading">
    <div class="col-12 text-center" style="padding: 40px 0;">
        <i class="bi bi-arrow-clockwise" style="font-size: 24px; color: #999;"></i>
        <p class="text-muted" style="margin-top: 8px;">Loading chart data...</p>
    </div>
</div>
<div class="row" v-if="loadError">
    <div class="col-12">
        <div class="alert alert-warning" style="margin-top: 10px;">
            <i class="bi bi-exclamation-triangle"></i> {{loadError}}
        </div>
    </div>
</div>
<!-- Granularity Note -->
<div class="row" v-if="dailyGranularity && !isMapView">
    <div class="col-12">
        <p class="text-muted small granularity-note">
            <i class="bi bi-info-circle"></i> {{dailyGranularity}}
        </p>
    </div>
</div>
<!-- Map View -->
<div v-if="isMapView && !loading">
    <div class="row" v-if="mapNormalized !== undefined">
        <div class="col-12 text-end" style="margin-bottom: 8px;">
            <div class="btn-group btn-group-sm" v-if="routeId === 'usStates'">
                <button type="button" class="btn" :class="mapNormalized ? 'btn-primary' : 'btn-outline-secondary'" @click="setMapNormalized(true)">Per Capita</button>
                <button type="button" class="btn" :class="!mapNormalized ? 'btn-primary' : 'btn-outline-secondary'" @click="setMapNormalized(false)">Raw Counts</button>
            </div>
        </div>
    </div>
    <choropleth-map :map-data="mapData" :map-type="mapType" :normalized="mapNormalized"></choropleth-map>
</div>
<!-- Demographics Chart Container -->
<div v-if="!isMapView">
    <div v-if="displayMode !== 'donut'">
        <div class="chart-container" style="display:block;">
            <chartjs-chart :chart-data="dailyChart"></chartjs-chart>
        </div>
        <!-- Volume Chart -->
        <div class="row" style="margin-top: 4px;">
            <div class="col-12">
                <p class="text-muted small" style="margin: 0 0 2px 0;">
                    Response Volume
                    <span v-if="volumeGranularity" class="granularity-note" style="margin-left: 6px;">
                        <i class="bi bi-info-circle"></i> {{volumeGranularity}}
                    </span>
                </p>
            </div>
        </div>
        <div class="volume-chart-container" style="display:block;">
            <chartjs-chart :chart-data="volumeChart"></chartjs-chart>
        </div>
    </div>
    <div v-if="displayMode === 'donut'">
        <p class="text-muted small" style="margin: 0 0 4px 0;">
            <i class="bi bi-info-circle"></i> Latest period: {{donutChart.periodLabel}}
        </p>
        <div class="chart-container" style="display:block;">
            <chartjs-chart :chart-data="donutChart"></chartjs-chart>
        </div>
    </div>
</div>
    `,
    setup(props) {
        const { ref, watch, onMounted, computed } = Vue;

        var MAP_VIEWS = { 'worldMap': 'world', 'usStates': 'us' };
        var MONTH_ABBR = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

        // Date filter
        const dateFilter = useDateFilter();
        const fromDate = ref(new Date(dateFilter.from.value.getTime()));
        const toDate = ref(new Date(dateFilter.to.value.getTime()));

        const fromInput = computed(function() { return dateFilter.toInputValue(fromDate.value); });
        const toInput = computed(function() { return dateFilter.toInputValue(toDate.value); });
        const minDateInput = computed(function() { return dateFilter.toInputValue(dateFilter.minDate); });
        const maxDateInput = computed(function() { return dateFilter.toInputValue(dateFilter.maxDate); });

        function onFromChange(e) {
            fromDate.value = dateFilter.fromInputValue(e.target.value);
        }
        function onToChange(e) {
            toDate.value = dateFilter.fromInputValue(e.target.value);
        }

        // Data service
        const dataService = useChartData();

        // State
        const dailyChart = ref({});
        const volumeChart = ref({});
        const donutChart = ref({});
        const displayMode = ref('bar');
        const countsData = ref(null);
        const summaryStats = ref(null);
        const dailyGranularity = ref(null);
        const volumeGranularity = ref(null);
        const topN = ref(0);
        const topNOptions = [
            { value: 0, label: 'All' },
            { value: 5, label: 'Top 5' },
            { value: 10, label: 'Top 10' },
            { value: 15, label: 'Top 15' }
        ];

        // Map state
        const isMapView = computed(function() { return !!MAP_VIEWS[props.routeId]; });
        const mapType = computed(function() { return MAP_VIEWS[props.routeId] || null; });
        const mapData = ref(null);
        const mapNormalized = ref(props.routeId === 'usStates' ? true : undefined);

        // Loading state
        const loading = ref(false);
        const loadError = ref(null);

        // Response data (kept for re-processing on display mode change)
        var responseData = null;

        // --- Label formatting helpers ---
        function formatDailyLabel(periodKey, granularity) {
            var d = new Date(periodKey);
            if (granularity === 'monthly') {
                return MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear();
            }
            return (d.getMonth()+1) + '/' + d.getDate() + '/' + d.getFullYear();
        }

        function granularityLabel(gran, numPeriods) {
            if (gran === 'weekly') return 'Showing weekly averages (' + numPeriods + ' weeks)';
            if (gran === 'monthly') return 'Showing monthly averages (' + numPeriods + ' months)';
            return null;
        }

        function parseCountsDate(dateStr) {
            var p = dateStr.split('-');
            return new Date(parseInt(p[0]), parseInt(p[1]) - 1, parseInt(p[2]));
        }

        // --- Data population functions ---
        function findTop(map) {
            if (!map) return { label: 'N/A', count: 0, pct: 0 };
            var best = null, total = 0;
            for (var k in map) {
                total += map[k];
                if (!best || map[k] > best.count) {
                    best = { label: k, count: map[k] };
                }
            }
            if (best && total > 0) {
                best.pct = Math.round(best.count / total * 1000) / 10;
            } else {
                return { label: 'N/A', count: 0, pct: 0 };
            }
            return best;
        }

        function computeTrend(current, previous) {
            if (previous === null || previous === undefined || previous === 0) return null;
            var pctChange = ((current - previous) / previous) * 100;
            if (Math.abs(pctChange) < 0.5) return { direction: 'flat', pct: 0 };
            return {
                direction: pctChange > 0 ? 'up' : 'down',
                pct: Math.round(Math.abs(pctChange))
            };
        }

        function buildSummaryStats(counts, priorCounts) {
            if (!counts) return;
            var stats = {};
            stats.totalResponses = counts.totalResponses || 0;
            var numPeriods = counts.days ? counts.days.length : 0;
            stats.avgPerPeriod = numPeriods > 0 ? Math.round(stats.totalResponses / numPeriods) : 0;

            var gran = counts.granularity || 'daily';
            if (gran === 'monthly') {
                stats.avgLabel = 'Avg / Month';
            } else if (gran === 'weekly') {
                stats.avgLabel = 'Avg / Week';
            } else {
                stats.avgLabel = 'Avg / Day';
            }

            stats.topCountry = findTop(counts.totalCountries);
            stats.topGender = findTop(counts.totalGender);

            if (priorCounts) {
                var priorTotal = priorCounts.totalResponses || 0;
                var priorNumPeriods = priorCounts.days ? priorCounts.days.length : 0;
                var priorAvg = priorNumPeriods > 0 ? Math.round(priorTotal / priorNumPeriods) : 0;

                stats.totalTrend = computeTrend(stats.totalResponses, priorTotal);
                stats.avgTrend = computeTrend(stats.avgPerPeriod, priorAvg);

                var priorTopCountry = findTop(priorCounts.totalCountries);
                if (stats.topCountry.label !== 'N/A' && priorTopCountry.label !== 'N/A') {
                    stats.countryTrend = computeTrend(stats.topCountry.pct, priorTopCountry.pct);
                }
                var priorTopGender = findTop(priorCounts.totalGender);
                if (stats.topGender.label !== 'N/A' && priorTopGender.label !== 'N/A') {
                    stats.genderTrend = computeTrend(stats.topGender.pct, priorTopGender.pct);
                }
            }

            summaryStats.value = stats;
        }

        function populateMapData(counts) {
            if (!counts) return;
            if (mapType.value === 'us') {
                mapData.value = counts.totalUsStates || {};
            } else {
                mapData.value = counts.totalCountriesDetailed || {};
            }
        }

        function populateVolumeChart() {
            var counts = countsData.value;
            if (!counts || !counts.days || counts.days.length === 0) return;

            var days = counts.days.slice().sort(function(a, b) {
                return a.date < b.date ? -1 : a.date > b.date ? 1 : 0;
            });

            var gran = counts.granularity || 'daily';
            volumeGranularity.value = granularityLabel(gran, days.length);

            var labels = [];
            var data = [];
            for (var i = 0; i < days.length; i++) {
                if (gran === 'monthly') {
                    var d = parseCountsDate(days[i].date);
                    labels.push(MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear());
                } else {
                    var parts = days[i].date.split('-');
                    labels.push(parseInt(parts[1]) + '/' + parseInt(parts[2]) + '/' + parts[0]);
                }
                data.push(days[i].totalResponses);
            }

            volumeChart.value = {
                labels: labels,
                datasets: [{ label: 'Responses', data: data }],
                displayMode: 'volume'
            };
        }

        function populateDailyChart() {
            var data = responseData;
            var id = props.routeId;
            if (!data || !data.daily || !data.daily[id]) return;

            var periodData = data.daily[id];
            var labelSet = data.daily.labels[id];
            if (!periodData || !labelSet) return;

            var periods = [];
            for (var propName in periodData) {
                periods.push(propName);
            }
            if (periods.length === 0) return;

            var granularity = data.dailyGranularity || 'daily';
            periods.sort(function(a, b) { return new Date(a) - new Date(b); });
            dailyGranularity.value = granularityLabel(granularity, periods.length);

            var chartLabels = [];
            for (var p = 0; p < periods.length; p++) {
                chartLabels.push(formatDailyLabel(periods[p], granularity));
            }

            // For languages, filter out English by default
            var filteredLabelSet = labelSet;
            if (id === 'languagesSpoken') {
                filteredLabelSet = [];
                labelSet.forEach(function(label) {
                    if (label !== 'English') filteredLabelSet.push(label);
                });
            }

            // Apply Top-N filter
            var topNVal = topN.value || 0;
            var labelsToShow = filteredLabelSet;
            if (topNVal > 0 && filteredLabelSet.length > topNVal) {
                var labelTotals = [];
                filteredLabelSet.forEach(function(label) {
                    var sum = 0;
                    for (var pp = 0; pp < periods.length; pp++) {
                        var entry = periodData[periods[pp]];
                        sum += entry[label] ? parseFloat(entry[label]) : 0;
                    }
                    labelTotals.push({ label: label, total: sum });
                });
                labelTotals.sort(function(a, b) { return b.total - a.total; });
                labelsToShow = [];
                for (var ti = 0; ti < topNVal; ti++) {
                    labelsToShow.push(labelTotals[ti].label);
                }
            }
            var otherLabels = [];
            if (topNVal > 0 && filteredLabelSet.length > topNVal) {
                filteredLabelSet.forEach(function(label) {
                    if (labelsToShow.indexOf(label) === -1) {
                        otherLabels.push(label);
                    }
                });
            }

            var datasets = [];
            labelsToShow.forEach(function(label) {
                var values = [];
                for (var pp = 0; pp < periods.length; pp++) {
                    var entry = periodData[periods[pp]];
                    values.push(entry[label] ? parseFloat(entry[label]) : 0);
                }
                datasets.push({ label: label, data: values });
            });

            // Add "Other" category if Top-N is active
            if (otherLabels.length > 0) {
                var otherValues = [];
                for (var pp = 0; pp < periods.length; pp++) {
                    var sum = 0;
                    for (var oi = 0; oi < otherLabels.length; oi++) {
                        var entry = periodData[periods[pp]];
                        sum += entry[otherLabels[oi]] ? parseFloat(entry[otherLabels[oi]]) : 0;
                    }
                    otherValues.push(sum);
                }
                datasets.push({ label: 'Other', data: otherValues });
            }

            // Attach counts data for tooltips
            var countsPerPeriod = null;
            if (countsData.value && countsData.value.days) {
                countsPerPeriod = {};
                var days = countsData.value.days;
                var dayCountsByDate = {};
                for (var di = 0; di < days.length; di++) {
                    dayCountsByDate[days[di].date] = days[di];
                }
                for (var pi = 0; pi < periods.length; pi++) {
                    var periodDate = new Date(periods[pi]);
                    var dateKey = periodDate.getFullYear() + '-'
                        + (periodDate.getMonth() < 9 ? '0' : '') + (periodDate.getMonth() + 1) + '-'
                        + (periodDate.getDate() < 10 ? '0' : '') + periodDate.getDate();
                    var dayData = dayCountsByDate[dateKey];
                    if (dayData) {
                        countsPerPeriod[chartLabels[pi]] = dayData;
                    }
                }
            }

            dailyChart.value = {
                labels: chartLabels,
                datasets: datasets,
                displayMode: displayMode.value,
                countsPerPeriod: countsPerPeriod,
                demographicField: id,
                autoScaleY: (id === 'languagesSpoken')
            };
        }

        function populateDonutChart() {
            var data = responseData;
            var id = props.routeId;
            if (!data || !data.daily || !data.daily[id]) return;

            var periodData = data.daily[id];
            var labelSet = data.daily.labels[id];
            if (!periodData || !labelSet) return;

            var periods = [];
            for (var propName in periodData) {
                periods.push(propName);
            }
            if (periods.length === 0) return;
            periods.sort(function(a, b) { return new Date(b) - new Date(a); });
            var latestPeriod = periods[0];

            var granularity = data.dailyGranularity || 'daily';
            var periodLabel = formatDailyLabel(latestPeriod, granularity);
            var entry = periodData[latestPeriod];

            var filteredLabelSet = labelSet;
            if (id === 'languagesSpoken') {
                filteredLabelSet = [];
                labelSet.forEach(function(label) {
                    if (label !== 'English') filteredLabelSet.push(label);
                });
            }

            var topNVal = topN.value || 0;
            var labelsToUse = filteredLabelSet;
            if (topNVal > 0 && filteredLabelSet.length > topNVal) {
                var sorted = filteredLabelSet.slice().sort(function(a, b) {
                    return (entry[b] || 0) - (entry[a] || 0);
                });
                labelsToUse = sorted.slice(0, topNVal);
            }

            var chartLabels = [];
            var chartData = [];
            var otherVal = 0;

            labelsToUse.forEach(function(label) {
                var val = entry[label] ? parseFloat(entry[label]) : 0;
                if (val > 0) {
                    chartLabels.push(label);
                    chartData.push(val);
                }
            });

            if (topNVal > 0 && filteredLabelSet.length > topNVal) {
                filteredLabelSet.forEach(function(label) {
                    if (labelsToUse.indexOf(label) === -1) {
                        otherVal += entry[label] ? parseFloat(entry[label]) : 0;
                    }
                });
                if (otherVal > 0) {
                    chartLabels.push('Other');
                    chartData.push(otherVal);
                }
            }

            donutChart.value = {
                labels: chartLabels,
                datasets: [{ data: chartData }],
                displayMode: 'donut',
                periodLabel: periodLabel
            };
        }

        // --- Actions ---
        function setDisplayMode(mode) {
            displayMode.value = mode;
            if (mode === 'donut' && responseData) {
                populateDonutChart();
            } else if (dailyChart.value && dailyChart.value.labels) {
                // Re-create with new display mode
                populateDailyChart();
            }
        }

        function setTopN(n) {
            topN.value = n;
            if (responseData && props.routeId) {
                populateDailyChart();
                populateVolumeChart();
                if (displayMode.value === 'donut') {
                    populateDonutChart();
                }
            }
        }

        function setMapNormalized(val) {
            mapNormalized.value = val;
        }

        function applyDateRange() {
            load();
        }

        function load() {
            var fromStr = dateFilter.formatDate(fromDate.value);
            var toStr = dateFilter.formatDate(toDate.value);

            // Compute prior period
            var rangeMs = toDate.value.getTime() - fromDate.value.getTime();
            var priorTo = new Date(fromDate.value.getTime() - 1);
            var priorFrom = new Date(priorTo.getTime() - rangeMs);
            if (priorFrom < dateFilter.minDate) priorFrom = new Date(dateFilter.minDate.getTime());
            var priorFromStr = dateFilter.formatDate(priorFrom);
            var priorToStr = dateFilter.formatDate(priorTo);

            loading.value = true;
            loadError.value = null;

            dataService.loadChartData(fromStr, toStr).then(function(chartData) {
                loading.value = false;
                responseData = chartData.aggregated;
                countsData.value = chartData.counts;

                // Load prior period for trend arrows (non-blocking)
                dataService.loadChartData(priorFromStr, priorToStr).then(function(priorData) {
                    buildSummaryStats(chartData.counts, priorData.counts);
                }).catch(function() {
                    buildSummaryStats(chartData.counts, null);
                });

                if (isMapView.value) {
                    populateMapData(chartData.counts);
                } else {
                    populateDailyChart();
                    populateVolumeChart();
                    if (displayMode.value === 'donut') {
                        populateDonutChart();
                    }
                }
            }).catch(function(error) {
                loading.value = false;
                loadError.value = 'Failed to load chart data. The date range may be too large \u2014 try a shorter period.';
                console.log(error);
            });
        }

        // Load on mount
        onMounted(load);

        // Watch for route changes (Vue reuses the component when only the param changes)
        watch(function() { return props.routeId; }, function(newId, oldId) {
            if (newId !== oldId) {
                // Reset state for new view
                dailyChart.value = {};
                volumeChart.value = {};
                donutChart.value = {};
                mapData.value = null;
                summaryStats.value = null;
                dailyGranularity.value = null;
                volumeGranularity.value = null;
                responseData = null;
                mapNormalized.value = (newId === 'usStates') ? true : undefined;
                load();
            }
        });

        return {
            // State
            summaryStats, loading, loadError, dailyGranularity, volumeGranularity,
            displayMode, topN, topNOptions, isMapView, mapType, mapData, mapNormalized,
            dailyChart, volumeChart, donutChart,
            // Date pickers
            fromInput, toInput, minDateInput, maxDateInput,
            onFromChange, onToChange,
            // Actions
            setDisplayMode, setTopN, setMapNormalized, applyDateRange,
            // Props pass-through
            routeId: Vue.toRef(props, 'routeId')
        };
    }
};
