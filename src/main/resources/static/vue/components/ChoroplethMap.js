/**
 * Choropleth Map component - replaces choropleth.js directive
 */
const ChoroplethMap = {
    props: {
        mapData: { type: Object, default: null },
        mapType: { type: String, default: null },
        normalized: { type: Boolean, default: false }
    },
    template: '<div ref="container" class="choropleth-container"></div>',
    setup(props) {
        const { ref, watch, onMounted, nextTick } = Vue;
        const container = ref(null);

        var US_STATE_POP = {
            'AL':5024279,'AK':733391,'AZ':7151502,'AR':3011524,'CA':39538223,
            'CO':5773714,'CT':3605944,'DE':989948,'FL':21538187,'GA':10711908,
            'HI':1455271,'ID':1839106,'IL':12812508,'IN':6785528,'IA':3190369,
            'KS':2937880,'KY':4505836,'LA':4657757,'ME':1362359,'MD':6177224,
            'MA':7029917,'MI':10077331,'MN':5706494,'MS':2961279,'MO':6154913,
            'MT':1084225,'NE':1961504,'NV':3104614,'NH':1377529,'NJ':9288994,
            'NM':2117522,'NY':20201249,'NC':10439388,'ND':779094,'OH':11799448,
            'OK':3959353,'OR':4237256,'PA':13002700,'RI':1097379,'SC':5118425,
            'SD':886667,'TN':6910840,'TX':29145505,'UT':3271616,'VT':643077,
            'VA':8631393,'WA':7614893,'WV':1793716,'WI':5893718,'WY':576851,
            'DC':689545
        };

        var FIPS_TO_STATE = {
            '01':'AL','02':'AK','04':'AZ','05':'AR','06':'CA','08':'CO','09':'CT',
            '10':'DE','11':'DC','12':'FL','13':'GA','15':'HI','16':'ID','17':'IL',
            '18':'IN','19':'IA','20':'KS','21':'KY','22':'LA','23':'ME','24':'MD',
            '25':'MA','26':'MI','27':'MN','28':'MS','29':'MO','30':'MT','31':'NE',
            '32':'NV','33':'NH','34':'NJ','35':'NM','36':'NY','37':'NC','38':'ND',
            '39':'OH','40':'OK','41':'OR','42':'PA','44':'RI','45':'SC','46':'SD',
            '47':'TN','48':'TX','49':'UT','50':'VT','51':'VA','53':'WA','54':'WV',
            '55':'WI','56':'WY'
        };

        var STATE_NAMES = {
            'AL':'Alabama','AK':'Alaska','AZ':'Arizona','AR':'Arkansas','CA':'California',
            'CO':'Colorado','CT':'Connecticut','DE':'Delaware','DC':'District of Columbia',
            'FL':'Florida','GA':'Georgia','HI':'Hawaii','ID':'Idaho','IL':'Illinois',
            'IN':'Indiana','IA':'Iowa','KS':'Kansas','KY':'Kentucky','LA':'Louisiana',
            'ME':'Maine','MD':'Maryland','MA':'Massachusetts','MI':'Michigan','MN':'Minnesota',
            'MS':'Mississippi','MO':'Missouri','MT':'Montana','NE':'Nebraska','NV':'Nevada',
            'NH':'New Hampshire','NJ':'New Jersey','NM':'New Mexico','NY':'New York',
            'NC':'North Carolina','ND':'North Dakota','OH':'Ohio','OK':'Oklahoma',
            'OR':'Oregon','PA':'Pennsylvania','RI':'Rhode Island','SC':'South Carolina',
            'SD':'South Dakota','TN':'Tennessee','TX':'Texas','UT':'Utah','VT':'Vermont',
            'VA':'Virginia','WA':'Washington','WV':'West Virginia','WI':'Wisconsin','WY':'Wyoming'
        };

        var worldTopoUrl = 'https://cdn.jsdelivr.net/npm/world-atlas@2/countries-110m.json';
        var usTopoUrl = 'https://cdn.jsdelivr.net/npm/us-atlas@3/states-10m.json';
        var topoCache = {};

        function loadTopo(url) {
            if (topoCache[url]) return Promise.resolve(topoCache[url]);
            return d3.json(url).then(function(data) {
                topoCache[url] = data;
                return data;
            });
        }

        function buildIsoMapping() {
            return {
                '4':'AF','8':'AL','12':'DZ','16':'AS','20':'AD','24':'AO','28':'AG',
                '31':'AZ','32':'AR','36':'AU','40':'AT','44':'BS','48':'BH','50':'BD',
                '51':'AM','52':'BB','56':'BE','60':'BM','64':'BT','68':'BO','70':'BA',
                '72':'BW','76':'BR','84':'BZ','86':'IO','90':'SB','92':'VG','96':'BN',
                '100':'BG','104':'MM','108':'BI','112':'BY','116':'KH','120':'CM',
                '124':'CA','132':'CV','140':'CF','144':'LK','148':'TD','152':'CL',
                '156':'CN','158':'TW','170':'CO','174':'KM','178':'CG','180':'CD',
                '184':'CK','188':'CR','191':'HR','192':'CU','196':'CY','203':'CZ',
                '204':'BJ','208':'DK','212':'DM','214':'DO','218':'EC','222':'SV',
                '226':'GQ','231':'ET','232':'ER','233':'EE','234':'FO','238':'FK',
                '242':'FJ','246':'FI','250':'FR','254':'GF','258':'PF','262':'DJ',
                '266':'GA','268':'GE','270':'GM','275':'PS','276':'DE','288':'GH',
                '296':'KI','300':'GR','304':'GL','308':'GD','312':'GP','316':'GU',
                '320':'GT','324':'GN','328':'GY','332':'HT','340':'HN','344':'HK',
                '348':'HU','352':'IS','356':'IN','360':'ID','364':'IR','368':'IQ',
                '372':'IE','376':'IL','380':'IT','384':'CI','388':'JM','392':'JP',
                '398':'KZ','400':'JO','404':'KE','408':'KP','410':'KR','414':'KW',
                '417':'KG','418':'LA','422':'LB','426':'LS','428':'LV','430':'LR',
                '434':'LY','438':'LI','440':'LT','442':'LU','450':'MG','454':'MW',
                '458':'MY','462':'MV','466':'ML','470':'MT','474':'MQ','478':'MR',
                '480':'MU','484':'MX','492':'MC','496':'MN','498':'MD','499':'ME',
                '504':'MA','508':'MZ','512':'OM','516':'NA','520':'NR','524':'NP',
                '528':'NL','540':'NC','554':'NZ','558':'NI','562':'NE','566':'NG',
                '570':'NU','578':'NO','583':'FM','584':'MH','585':'PW','586':'PK',
                '591':'PA','598':'PG','600':'PY','604':'PE','608':'PH','616':'PL',
                '620':'PT','624':'GW','626':'TL','630':'PR','634':'QA','642':'RO',
                '643':'RU','646':'RW','659':'KN','662':'LC','666':'PM','670':'VC',
                '674':'SM','678':'ST','682':'SA','686':'SN','688':'RS','690':'SC',
                '694':'SL','702':'SG','703':'SK','704':'VN','705':'SI','706':'SO',
                '710':'ZA','716':'ZW','724':'ES','728':'SS','729':'SD','740':'SR',
                '748':'SZ','752':'SE','756':'CH','760':'SY','762':'TJ','764':'TH',
                '768':'TG','776':'TO','780':'TT','784':'AE','788':'TN','792':'TR',
                '795':'TM','798':'TV','800':'UG','804':'UA','807':'MK','818':'EG',
                '826':'GB','834':'TZ','840':'US','854':'BF','858':'UY','860':'UZ',
                '862':'VE','876':'WF','882':'WS','887':'YE','894':'ZM'
            };
        }

        function addLegend(el, colorScale, min, max, suffix) {
            var legend = d3.select(el).append('div')
                .attr('class', 'choropleth-legend');
            legend.append('span').text(min.toLocaleString() + suffix);
            var bar = legend.append('div').attr('class', 'legend-bar');
            var stops = [];
            for (var i = 0; i <= 10; i++) {
                stops.push(colorScale(min + (max - min) * i / 10));
            }
            bar.style('background', 'linear-gradient(to right, ' + stops.join(', ') + ')');
            var maxLabel = max > 1000 ? Math.round(max).toLocaleString() : max.toFixed(1);
            legend.append('span').text(maxLabel + suffix);
        }

        function renderWorldMap(el, data) {
            loadTopo(worldTopoUrl).then(function(world) {
                var countries = topojson.feature(world, world.objects.countries);
                var width = el.clientWidth || 900;
                var height = width * 0.5;

                var svg = d3.select(el).append('svg')
                    .attr('viewBox', '0 0 ' + width + ' ' + height)
                    .attr('preserveAspectRatio', 'xMidYMid meet');

                var projection = d3.geoNaturalEarth1().fitSize([width, height], countries);
                var path = d3.geoPath().projection(projection);

                var isoNumToAlpha = buildIsoMapping();
                var values = {};
                var maxVal = 0;
                countries.features.forEach(function(f) {
                    var alpha = isoNumToAlpha[f.id] || '';
                    var count = data[alpha] || 0;
                    values[f.id] = count;
                    if (count > maxVal) maxVal = count;
                });

                var color = d3.scaleSequential(d3.interpolateBlues)
                    .domain([0, maxVal > 0 ? maxVal : 1]);

                var tooltip = d3.select(el).append('div')
                    .attr('class', 'choropleth-tooltip')
                    .style('display', 'none');

                svg.selectAll('path')
                    .data(countries.features)
                    .enter().append('path')
                    .attr('d', path)
                    .attr('fill', function(d) {
                        var v = values[d.id] || 0;
                        return v > 0 ? color(v) : '#eee';
                    })
                    .attr('stroke', '#999')
                    .attr('stroke-width', 0.5)
                    .on('mouseover', function(event, d) {
                        var alpha = isoNumToAlpha[d.id] || '??';
                        var name = d.properties.name || alpha;
                        var count = values[d.id] || 0;
                        tooltip.style('display', 'block')
                            .html('<strong>' + name + '</strong> (' + alpha + ')<br>' +
                                count.toLocaleString() + ' responses');
                        d3.select(this).attr('stroke', '#333').attr('stroke-width', 1.5);
                    })
                    .on('mousemove', function(event) {
                        var rect = el.getBoundingClientRect();
                        tooltip.style('left', (event.clientX - rect.left + 10) + 'px')
                            .style('top', (event.clientY - rect.top - 30) + 'px');
                    })
                    .on('mouseout', function() {
                        tooltip.style('display', 'none');
                        d3.select(this).attr('stroke', '#999').attr('stroke-width', 0.5);
                    });

                addLegend(el, color, 0, maxVal, '');
            });
        }

        function renderUsMap(el, data, normalized) {
            loadTopo(usTopoUrl).then(function(us) {
                var states = topojson.feature(us, us.objects.states);
                var width = el.clientWidth || 900;
                var height = width * 0.6;

                var svg = d3.select(el).append('svg')
                    .attr('viewBox', '0 0 ' + width + ' ' + height)
                    .attr('preserveAspectRatio', 'xMidYMid meet');

                var projection = d3.geoAlbersUsa().fitSize([width, height], states);
                var path = d3.geoPath().projection(projection);

                var values = {};
                var maxVal = 0;
                states.features.forEach(function(f) {
                    var fips = String(f.id).padStart(2, '0');
                    var abbr = FIPS_TO_STATE[fips] || '';
                    var count = data[abbr] || 0;
                    var val = count;
                    if (normalized && count > 0) {
                        var pop = US_STATE_POP[abbr] || 1;
                        val = (count / pop) * 1000000;
                    }
                    values[f.id] = { raw: count, display: val, abbr: abbr };
                    if (val > maxVal) maxVal = val;
                });

                var color = d3.scaleSequential(d3.interpolateYlOrRd)
                    .domain([0, maxVal > 0 ? maxVal : 1]);

                var tooltip = d3.select(el).append('div')
                    .attr('class', 'choropleth-tooltip')
                    .style('display', 'none');

                svg.selectAll('path')
                    .data(states.features)
                    .enter().append('path')
                    .attr('d', path)
                    .attr('fill', function(d) {
                        var v = values[d.id];
                        return (v && v.display > 0) ? color(v.display) : '#eee';
                    })
                    .attr('stroke', '#fff')
                    .attr('stroke-width', 1)
                    .on('mouseover', function(event, d) {
                        var v = values[d.id] || {};
                        var name = STATE_NAMES[v.abbr] || v.abbr;
                        var html = '<strong>' + name + '</strong><br>' +
                            (v.raw || 0).toLocaleString() + ' responses';
                        if (normalized && v.raw > 0) {
                            html += '<br>' + v.display.toFixed(1) + ' per million residents';
                        }
                        tooltip.style('display', 'block').html(html);
                        d3.select(this).attr('stroke', '#333').attr('stroke-width', 2);
                    })
                    .on('mousemove', function(event) {
                        var rect = el.getBoundingClientRect();
                        tooltip.style('left', (event.clientX - rect.left + 10) + 'px')
                            .style('top', (event.clientY - rect.top - 30) + 'px');
                    })
                    .on('mouseout', function() {
                        tooltip.style('display', 'none');
                        d3.select(this).attr('stroke', '#fff').attr('stroke-width', 1);
                    });

                var suffix = normalized ? ' per M' : '';
                addLegend(el, color, 0, maxVal, suffix);
            });
        }

        function render() {
            var data = props.mapData;
            var type = props.mapType;
            if (!data || !type) return;

            var el = container.value;
            if (!el) return;
            el.innerHTML = '';

            if (type === 'us') {
                renderUsMap(el, data, props.normalized);
            } else {
                renderWorldMap(el, data);
            }
        }

        watch(function() { return props.mapData; }, function(newVal) {
            if (newVal) nextTick(render);
        });

        watch(function() { return props.normalized; }, function() {
            if (props.mapData) nextTick(render);
        });

        return { container };
    }
};
