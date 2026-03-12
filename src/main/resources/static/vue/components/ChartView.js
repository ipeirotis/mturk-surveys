/**
 * ChartView Component
 * Replaces ChartController + chart.html
 */
const ChartView = {
    components: {
        'chartjs-chart': ChartjsChart,
        'choropleth-map': ChoroplethMap
    },
    props: {
        viewId: { type: String, required: true }
    },
    template: `
<!-- Skeleton stat cards while loading -->
<div class="row stats-cards" v-if="!summaryStats">
    <div class="col-6" v-for="n in 2" :key="n">
        <div class="stat-card stat-card-skeleton">
            <div class="stat-value">&nbsp;</div>
            <div class="stat-label">&nbsp;</div>
        </div>
    </div>
</div>
<!-- Summary Statistics Cards -->
<div class="row stats-cards" v-if="summaryStats">
    <div class="col-6">
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
    <div class="col-6">
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
</div>
<!-- Toolbar: date range, presets, chart controls -->
<div class="chart-toolbar">
    <div class="toolbar-row">
        <div class="toolbar-group">
            <input type="date" class="form-control form-control-sm" v-model="fromStr" :min="minDateStr" :max="toStr" style="max-width:140px;" />
            <input type="date" class="form-control form-control-sm" v-model="toStr" :min="fromStr" :max="maxDateStr" style="max-width:140px;" />
            <button type="button" class="btn btn-primary btn-sm" @click="applyDateRange()">
                <i class="bi bi-arrow-clockwise"></i> Update
            </button>
        </div>
        <div class="toolbar-separator"></div>
        <div class="toolbar-group date-presets">
            <button v-for="p in datePresets" :key="p.label" type="button" class="btn btn-sm"
                :class="activePreset === p.label ? 'btn-primary' : 'btn-outline-secondary'"
                @click="applyPreset(p)">{{p.label}}</button>
        </div>
        <template v-if="!isMapView && !isResponseTimeView">
            <div class="toolbar-separator"></div>
            <div class="toolbar-group">
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn" v-for="opt in topNOptions" :key="opt.value"
                        :class="topN === opt.value ? 'btn-primary' : 'btn-outline-secondary'"
                        @click="setTopN(opt.value)">{{opt.label}}</button>
                </div>
            </div>
            <div class="toolbar-spacer"></div>
            <div class="toolbar-group">
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn" :class="displayMode === 'bar' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('bar')">
                        <i class="bi bi-bar-chart-fill"></i> Bars
                    </button>
                    <button type="button" class="btn" :class="displayMode === 'area' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('area')">
                        <i class="bi bi-graph-up"></i> Area
                    </button>
                    <button type="button" class="btn" :class="displayMode === 'line' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('line')">
                        <i class="bi bi-activity"></i> Line
                    </button>
                    <button type="button" class="btn" :class="displayMode === 'donut' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('donut')">
                        <i class="bi bi-pie-chart"></i> Donut
                    </button>
                    <button type="button" class="btn" :class="displayMode === 'sparklines' ? 'btn-primary' : 'btn-outline-secondary'" @click="setDisplayMode('sparklines')">
                        <i class="bi bi-grid-3x3-gap"></i> Grid
                    </button>
                </div>
            </div>
        </template>
    </div>
</div>
<!-- Loading / Error -->
<div class="row" v-if="loading">
    <div class="col-12 text-center" style="padding: 60px 0;">
        <div class="spinner-border text-primary" role="status" style="width: 2rem; height: 2rem;">
            <span class="visually-hidden">Loading...</span>
        </div>
        <p class="text-muted" style="margin-top: 12px; font-size: 13px;">Loading chart data...</p>
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
    <div class="row" v-if="mapNormalized !== null">
        <div class="col-12 text-end" style="margin-bottom: 8px;">
            <div class="btn-group btn-group-sm" v-if="viewId === 'usStates'">
                <button type="button" class="btn" :class="mapNormalized ? 'btn-primary' : 'btn-outline-secondary'" @click="mapNormalized = true">Per Capita</button>
                <button type="button" class="btn" :class="!mapNormalized ? 'btn-primary' : 'btn-outline-secondary'" @click="mapNormalized = false">Raw Counts</button>
            </div>
        </div>
    </div>
    <choropleth-map :map-data="mapData" :map-type="mapType" :normalized="mapNormalized"></choropleth-map>
</div>
<!-- Response Time Chart -->
<div v-if="isResponseTimeView && !loading">
    <chartjs-chart :chart-data="dailyChart" class="chart-container" style="display:block;"></chartjs-chart>
    <p class="text-muted small" style="margin-top: 6px;">
        <i class="bi bi-info-circle"></i> Time in minutes between HIT creation and worker response submission. Shaded band shows 25th&ndash;75th percentile range.
    </p>
</div>
<!-- Demographics Chart Container -->
<div v-if="!isMapView && !isResponseTimeView">
    <div v-if="displayMode === 'sparklines'">
        <sparkline-grid :chart-data="dailyChart"></sparkline-grid>
    </div>
    <div v-if="displayMode !== 'donut' && displayMode !== 'sparklines'">
        <chartjs-chart :chart-data="dailyChart" class="chart-container" style="display:block;"></chartjs-chart>
        <!-- Volume Chart -->
        <div class="row" style="margin-top: 8px;">
            <div class="col-12">
                <p class="volume-label">
                    Response Volume
                    <span v-if="volumeGranularity" style="font-weight: 400; text-transform: none; letter-spacing: 0; margin-left: 6px; font-size: 10px;">
                        <i class="bi bi-info-circle"></i> {{volumeGranularity}}
                    </span>
                </p>
            </div>
        </div>
        <chartjs-chart :chart-data="volumeChart" class="volume-chart-container" style="display:block;"></chartjs-chart>
    </div>
    <div v-if="displayMode === 'donut'">
        <p class="text-muted small" style="margin: 0 0 4px 0;">
            <i class="bi bi-info-circle"></i> Latest period: {{donutChart.periodLabel}}
        </p>
        <chartjs-chart :chart-data="donutChart" class="chart-container" style="display:block;"></chartjs-chart>
    </div>
</div>
`,
    setup(props) {
        const { ref, watch, onMounted } = Vue;

        var MAP_VIEWS = { 'worldMap': 'world', 'usStates': 'us' };
        var RESPONSE_TIME_VIEW = 'responseTime';
        var MONTH_ABBR = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

        // Date validation limits
        var minDate = new Date(2015, 2, 26);
        var maxDate = new Date();

        // Clamp persisted dates to valid range
        var initFrom = new Date(dateFilterState.from.value.getTime());
        var initTo = new Date(dateFilterState.to.value.getTime());
        if (initFrom < minDate) initFrom = new Date(minDate.getTime());
        if (initTo > maxDate) initTo = new Date(maxDate.getTime());
        if (initFrom > initTo) initFrom = new Date(initTo.getTime());

        function toDateStr(d) {
            return d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0');
        }
        function fromDateStr(s) {
            var p = s.split('-');
            return new Date(parseInt(p[0]), parseInt(p[1])-1, parseInt(p[2]));
        }

        var fromStr = ref(toDateStr(initFrom));
        var toStr = ref(toDateStr(initTo));
        var minDateStr = toDateStr(minDate);
        var maxDateStr = toDateStr(maxDate);

        var dailyChart = ref({});
        var volumeChart = ref({});
        var donutChart = ref({});
        var displayMode = ref('line');
        var countsData = ref(null);
        var summaryStats = ref(null);
        var dailyGranularity = ref(null);
        var volumeGranularity = ref(null);
        var topN = ref(0);
        var topNOptions = [
            { value: 0, label: 'All' },
            { value: 5, label: 'Top 5' },
            { value: 10, label: 'Top 10' },
            { value: 15, label: 'Top 15' }
        ];

        // Date range presets
        var datePresets = [
            { label: '3M', months: 3 },
            { label: '6M', months: 6 },
            { label: '1Y', years: 1 },
            { label: '2Y', years: 2 },
            { label: '5Y', years: 5 },
            { label: 'All', years: null }
        ];
        var activePreset = ref('2Y');

        function applyPreset(preset) {
            var to = new Date();
            var from;
            if (preset.years === null && !preset.months) {
                from = new Date(minDate.getTime());
            } else if (preset.months) {
                from = new Date();
                from.setMonth(from.getMonth() - preset.months);
                if (from < minDate) from = new Date(minDate.getTime());
            } else {
                from = new Date();
                from.setFullYear(from.getFullYear() - preset.years);
                if (from < minDate) from = new Date(minDate.getTime());
            }
            fromStr.value = toDateStr(from);
            toStr.value = toDateStr(to);
            dateFilterState.from.value = from;
            dateFilterState.to.value = to;
            activePreset.value = preset.label;
            setHashParams({ from: fromStr.value, to: toStr.value, preset: preset.label });
            load();
        }

        // --- URL state persistence ---
        function getHashParams() {
            var hash = window.location.hash || '';
            var qIdx = hash.indexOf('?');
            if (qIdx === -1) return {};
            var qs = hash.substring(qIdx + 1);
            var params = {};
            qs.split('&').forEach(function(pair) {
                var parts = pair.split('=');
                if (parts.length === 2) params[decodeURIComponent(parts[0])] = decodeURIComponent(parts[1]);
            });
            return params;
        }

        function setHashParams(updates) {
            var hash = window.location.hash || '';
            var baseHash = hash.split('?')[0];
            var params = getHashParams();
            for (var k in updates) {
                if (updates[k] === null || updates[k] === undefined) {
                    delete params[k];
                } else {
                    params[k] = updates[k];
                }
            }
            var pairs = [];
            for (var key in params) {
                pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
            }
            var newHash = baseHash + (pairs.length > 0 ? '?' + pairs.join('&') : '');
            if (newHash !== window.location.hash) {
                history.replaceState(null, '', newHash);
            }
        }

        // Restore state from URL on init
        var hashParams = getHashParams();
        if (hashParams.from) {
            fromStr.value = hashParams.from;
            dateFilterState.from.value = fromDateStr(hashParams.from);
        }
        if (hashParams.to) {
            toStr.value = hashParams.to;
            dateFilterState.to.value = fromDateStr(hashParams.to);
        }
        if (hashParams.mode && ['bar', 'area', 'line', 'donut', 'sparklines'].indexOf(hashParams.mode) >= 0) {
            displayMode.value = hashParams.mode;
        }
        if (hashParams.topN) {
            var parsedN = parseInt(hashParams.topN);
            if ([0, 5, 10, 15].indexOf(parsedN) >= 0) topN.value = parsedN;
        }
        if (hashParams.preset) {
            activePreset.value = hashParams.preset;
        }

        var isMapView = ref(!!MAP_VIEWS[props.viewId]);
        var isResponseTimeView = ref(props.viewId === RESPONSE_TIME_VIEW);
        var mapType = ref(MAP_VIEWS[props.viewId] || null);
        var mapData = ref(null);
        var mapNormalized = ref(props.viewId === 'usStates' ? true : null);

        var response = ref(null);
        var loading = ref(false);
        var loadError = ref(null);

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

            if (priorCounts) {
                var priorTotal = priorCounts.totalResponses || 0;
                var priorNumPeriods = priorCounts.days ? priorCounts.days.length : 0;
                var priorAvg = priorNumPeriods > 0 ? Math.round(priorTotal / priorNumPeriods) : 0;

                stats.totalTrend = computeTrend(stats.totalResponses, priorTotal);
                stats.avgTrend = computeTrend(stats.avgPerPeriod, priorAvg);
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
                displayMode: 'volumeLine'
            };
        }

        function populateResponseTimeChart() {
            var counts = countsData.value;
            if (!counts || !counts.days || counts.days.length === 0) return;

            var days = counts.days.slice().sort(function(a, b) {
                return a.date < b.date ? -1 : a.date > b.date ? 1 : 0;
            });

            var gran = counts.granularity || 'daily';
            dailyGranularity.value = granularityLabel(gran, days.length);

            var labels = [];
            var medianData = [];
            var p25Data = [];
            var p75Data = [];
            for (var i = 0; i < days.length; i++) {
                if (gran === 'monthly') {
                    var d = parseCountsDate(days[i].date);
                    labels.push(MONTH_ABBR[d.getMonth()] + ' ' + d.getFullYear());
                } else {
                    var parts = days[i].date.split('-');
                    labels.push(parseInt(parts[1]) + '/' + parseInt(parts[2]) + '/' + parts[0]);
                }
                medianData.push(days[i].medianResponseTimeMinutes != null ? days[i].medianResponseTimeMinutes : null);
                p25Data.push(days[i].p25ResponseTimeMinutes != null ? days[i].p25ResponseTimeMinutes : null);
                p75Data.push(days[i].p75ResponseTimeMinutes != null ? days[i].p75ResponseTimeMinutes : null);
            }

            dailyChart.value = {
                labels: labels,
                datasets: [
                    { label: '75th Percentile', data: p75Data },
                    { label: 'Median', data: medianData },
                    { label: '25th Percentile', data: p25Data }
                ],
                displayMode: 'responseTime'
            };
        }

        function populateDailyChart(id) {
            var data = response.value;
            var periodData = data.daily[id];
            var labelSet = data.daily.labels[id];

            if (!periodData || !labelSet) return;

            var periods = Object.keys(periodData);
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
                filteredLabelSet = labelSet.filter(function(label) { return label !== 'English'; });
            }

            // Apply Top-N filter
            var currentTopN = topN.value || 0;
            var labelsToShow = filteredLabelSet;
            if (currentTopN > 0 && filteredLabelSet.length > currentTopN) {
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
                for (var ti = 0; ti < currentTopN; ti++) {
                    labelsToShow.push(labelTotals[ti].label);
                }
            }
            var otherLabels = [];
            if (currentTopN > 0 && filteredLabelSet.length > currentTopN) {
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

        function populateDonutChart(id) {
            var data = response.value;
            var periodData = data.daily[id];
            var labelSet = data.daily.labels[id];
            if (!periodData || !labelSet) return;

            var periods = Object.keys(periodData);
            if (periods.length === 0) return;
            periods.sort(function(a, b) { return new Date(b) - new Date(a); });
            var latestPeriod = periods[0];

            var granularity = data.dailyGranularity || 'daily';
            var periodLabel = formatDailyLabel(latestPeriod, granularity);

            var entry = periodData[latestPeriod];

            var filteredLabelSet = labelSet;
            if (id === 'languagesSpoken') {
                filteredLabelSet = labelSet.filter(function(label) { return label !== 'English'; });
            }

            var currentTopN = topN.value || 0;
            var labelsToUse = filteredLabelSet;
            if (currentTopN > 0 && filteredLabelSet.length > currentTopN) {
                var sorted = filteredLabelSet.slice().sort(function(a, b) {
                    return (entry[b] || 0) - (entry[a] || 0);
                });
                labelsToUse = sorted.slice(0, currentTopN);
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

            if (currentTopN > 0 && filteredLabelSet.length > currentTopN) {
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

        function setDisplayMode(mode) {
            displayMode.value = mode;
            setHashParams({ mode: mode });
            if (mode === 'donut' && response.value) {
                populateDonutChart(props.viewId);
            } else if (mode === 'sparklines' && response.value) {
                populateDailyChart(props.viewId);
            } else if (dailyChart.value && dailyChart.value.labels) {
                dailyChart.value = Object.assign({}, dailyChart.value, { displayMode: mode });
            }
        }

        function setTopN(n) {
            topN.value = n;
            setHashParams({ topN: n > 0 ? String(n) : null });
            if (response.value && props.viewId) {
                if (displayMode.value === 'donut') {
                    populateDonutChart(props.viewId);
                } else {
                    populateDailyChart(props.viewId);
                    populateVolumeChart();
                }
            }
        }

        function applyDateRange() {
            activePreset.value = null;
            dateFilterState.from.value = fromDateStr(fromStr.value);
            dateFilterState.to.value = fromDateStr(toStr.value);
            setHashParams({ from: fromStr.value, to: toStr.value, preset: null });
            load();
        }

        async function load() {
            var from = fromDateStr(fromStr.value);
            var to = fromDateStr(toStr.value);

            // Compute prior period for trend comparison
            var rangeMs = to.getTime() - from.getTime();
            var priorTo = new Date(from.getTime() - 1);
            var priorFrom = new Date(priorTo.getTime() - rangeMs);
            if (priorFrom < minDate) priorFrom = new Date(minDate.getTime());

            loading.value = true;
            loadError.value = null;

            try {
                var chartData = await chartDataService.loadChartData(from, to);
                loading.value = false;
                response.value = chartData.aggregated;
                countsData.value = chartData.counts;

                // Load prior period for trend arrows (non-blocking)
                chartDataService.loadChartData(priorFrom, priorTo).then(function(priorData) {
                    buildSummaryStats(chartData.counts, priorData.counts);
                }).catch(function() {
                    buildSummaryStats(chartData.counts, null);
                });

                if (isMapView.value) {
                    populateMapData(chartData.counts);
                } else if (props.viewId === RESPONSE_TIME_VIEW) {
                    populateResponseTimeChart();
                } else {
                    populateDailyChart(props.viewId);
                    populateVolumeChart();
                }
            } catch(error) {
                loading.value = false;
                loadError.value = 'Failed to load chart data. The date range may be too large \u2014 try a shorter period.';
                console.log(error);
            }
        }

        // Watch for viewId changes (route param changes)
        watch(() => props.viewId, (newId) => {
            isMapView.value = !!MAP_VIEWS[newId];
            isResponseTimeView.value = (newId === RESPONSE_TIME_VIEW);
            mapType.value = MAP_VIEWS[newId] || null;
            mapData.value = null;
            mapNormalized.value = (newId === 'usStates') ? true : null;
            displayMode.value = 'line';
            dailyChart.value = {};
            volumeChart.value = {};
            donutChart.value = {};
            load();
        });

        // Initial load
        onMounted(load);

        return {
            fromStr, toStr, minDateStr, maxDateStr,
            dailyChart, volumeChart, donutChart,
            displayMode, summaryStats, dailyGranularity, volumeGranularity,
            topN, topNOptions,
            datePresets, activePreset, applyPreset,
            isMapView, isResponseTimeView, mapType, mapData, mapNormalized,
            loading, loadError,
            setDisplayMode, setTopN, applyDateRange
        };
    }
};
