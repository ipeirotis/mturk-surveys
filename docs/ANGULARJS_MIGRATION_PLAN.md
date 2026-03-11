# AngularJS Migration Plan (T7.1)

## Executive Summary

The mturk-surveys frontend is a **demographics analytics dashboard** built on AngularJS 1.8.3 (originally 1.2.15, upgraded). AngularJS has been EOL since December 2021, receiving no security patches or updates. This document evaluates migration paths and provides a phased implementation plan.

**Key finding:** The frontend is surprisingly small (~1,300 lines of custom JS, 1 controller, 3 services, 4 directives, 2 HTML templates). This makes a **clean rewrite** feasible and preferable to incremental migration.

---

## 1. Current State Assessment

### Codebase Inventory

| Category | Count | Lines of Code |
|----------|-------|---------------|
| Controllers | 1 (`ChartController`) | 456 |
| Directives | 4 (chartjsChart, choroplethMap, navpills, pills) | 728 |
| Services/Factories | 3 (dataService, interceptor/loading, dateFilterState) | 93 |
| Config (app.js, views.js) | 2 | 17 |
| **Total custom JS** | **10 files** | **~1,294** |
| HTML templates | 2 (index.html, chart.html) | 260 |
| CSS | 1 (style.css) | 321 |

### Dependencies

| Library | Version | Used For | AngularJS-coupled? |
|---------|---------|----------|-------------------|
| AngularJS | 1.8.3 | Framework | Yes (removed in migration) |
| angular-route | 1.8.3 | Single-route routing | Yes |
| angular-sanitize | 1.8.3 | HTML sanitization | Yes |
| angular-cookies | 1.8.3 | Cookie access | **Not actually used** |
| angular-resource | 1.8.3 | REST client | **Not actually used** |
| UI Bootstrap | 2.5.0 | Datepicker only | Yes |
| Chart.js | 4.4.7 (CDN) | Bar/area/line/donut charts | No |
| D3.js | 7.x (CDN) | Choropleth maps | No |
| TopoJSON | 3.x (CDN) | Map data | No |
| Bootstrap | 5.3.3 (CDN) | CSS framework | No |
| jQuery | 1.9.1 (CDN) | Loading spinner only | No (can remove) |

### AngularJS Patterns in Use

- **$scope** with ~100+ properties in ChartController (no `controllerAs`)
- **$scope.$watch** with deep equality checks on large data objects
- **ng-model** two-way binding for date pickers
- **ngRoute** with a single parameterized route (`/:id` → chart.html)
- **$http.get** with `$cacheFactory` for API calls
- **HTTP interceptor** for global loading state
- **Custom directives** wrapping Chart.js and D3 (link functions with DOM manipulation)
- **$filter('date')** for date formatting
- **$timeout** to work around digest cycle timing

### API Surface

The entire SPA consumes **one endpoint**:

```
GET /api/survey/demographics/chartData?from=MM/dd/yyyy&to=MM/dd/yyyy
```

Response shape (simplified):
```json
{
  "aggregated": { "daily": { "<field>": { "<date>": { "<category>": <pct> } } } },
  "counts": { "totalResponses": N, "days": [...], "total<Field>": {...} }
}
```

### What's NOT Part of the SPA

The **FreeMarker template** (`src/main/resources/templates/demographics.html`) is the MTurk worker survey form. It uses jQuery, has zero AngularJS, and is **completely separate**. It stays as-is — no migration needed.

---

## 2. Framework Evaluation

### Options Considered

| Framework | Bundle Size | Learning Curve | Build Tooling Required | Ecosystem |
|-----------|------------|----------------|----------------------|-----------|
| **Vue 3 + Vite** | ~33KB gzipped | Low (template syntax similar to AngularJS) | Yes (Vite) | Large |
| **React + Vite** | ~40KB gzipped | Medium | Yes (Vite) | Largest |
| **Preact** | ~3KB gzipped | Medium (React API) | Yes (Vite) | Small |
| **Alpine.js** | ~15KB gzipped | Very low | No | Small |
| **Vanilla JS + Web Components** | 0KB | Medium-high | Optional | N/A |

### Evaluation Criteria

1. **Maintainability by a solo developer** (university professor, not a frontend specialist)
2. **Minimal build tooling** (current setup has none; keep it simple)
3. **Template-driven** (current code uses HTML templates with directives, not JSX)
4. **Good Chart.js/D3 integration** (these libraries stay; just need to unwrap from AngularJS)
5. **Long-term viability** (avoid another EOL situation in 5 years)
6. **CDN-friendly** (current setup loads everything from CDNs; no npm/node required)

### Recommendation: **Vue 3 (via CDN, no build step)**

**Why Vue 3:**

1. **Template syntax is the closest to AngularJS.** `ng-if` → `v-if`, `ng-repeat` → `v-for`, `ng-model` → `v-model`, `ng-click` → `@click`. The migration is nearly mechanical for templates.

2. **Works without a build step.** Vue 3 can be loaded from a CDN and used directly in HTML files, just like AngularJS today. No Vite/Webpack/npm needed. This matches the current zero-tooling approach.

3. **Reactive system replaces $scope.$watch.** Vue's reactivity (`ref()`, `computed()`) automatically tracks dependencies — no manual watchers needed. This eliminates the most error-prone AngularJS pattern.

4. **Composition API fits this codebase.** The single-controller architecture maps naturally to Vue's Composition API: each logical concern (chart data, date filtering, display mode) becomes a composable function.

5. **Stable and widely adopted.** Vue 3 has been stable since 2020, has a 5+ year track record, and is backed by a large community and corporate sponsors.

6. **Single-file or multi-file.** Can start with inline templates (like the current setup) and optionally move to Single File Components later with a build step.

**Why not the others:**

- **React**: Requires JSX (needs a build step) or `createElement` calls (verbose). Doesn't match the template-driven style of the current codebase.
- **Alpine.js**: Great for sprinkling interactivity, but lacks a component model for the chart/map directives. No router. Would feel limiting.
- **Vanilla JS/Web Components**: Maximum control but more boilerplate. Shadow DOM complicates styling. No reactivity system — would need to hand-roll state management.
- **Preact**: Same JSX issue as React, smaller ecosystem for chart wrappers.

### CDN-based Vue 3 (No Build Step)

Vue 3 supports a "progressive" mode via CDN:

```html
<script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>
<script>
const { createApp, ref, computed, watch, onMounted } = Vue;

const app = createApp({
  setup() {
    const from = ref(new Date());
    const to = ref(new Date());
    const chartData = ref(null);
    // ... reactive state replaces $scope
    return { from, to, chartData };
  }
});
app.mount('#app');
</script>
```

This means **no npm, no node, no bundler, no package.json** — the same deployment model as today.

---

## 3. Component Mapping

| AngularJS Artifact | Current | Vue 3 Equivalent |
|---|---|---|
| `angular.module('mturk', [...])` | Module definition | `createApp({...})` |
| `ChartController` | 456-line controller with $scope | Composition API `setup()` with `ref()`/`computed()` |
| `dataService` factory | $http.get + $cacheFactory | `fetch()` + simple Map cache (or composable) |
| `interceptor` + `loading` | HTTP interceptor + jQuery spinner | Wrapper around `fetch()` with reactive `loading` ref |
| `dateFilterState` factory | Singleton state object | `reactive()` object (shared via provide/inject or module-level) |
| `chartjsChart` directive | link fn + $scope.$watch | Vue component with `watch()` + `onMounted()`/`onUnmounted()` |
| `choroplethMap` directive | link fn + D3 rendering | Vue component with `watch()` + `onMounted()` |
| `navpills` directive | Watches $location.path() | Component using `vue-router`'s `useRoute()` or simple reactive prop |
| `pills` directive | Watches ng-model | `v-model` with `:class` binding (no component needed) |
| `ngRoute /:id` | Hash-based routing | `vue-router` (hash mode) or reactive variable (routing is trivial here) |
| `ng-model="from"` | Two-way binding | `v-model` (same concept) |
| `$scope.$watch('chartData', fn, true)` | Deep watcher | `watch(chartData, fn, { deep: true })` |
| `$http.get(url)` | HTTP client | `fetch(url).then(r => r.json())` |
| `$filter('date')(date, 'MM/dd/yyyy')` | Date formatting | `Intl.DateTimeFormat` or template literal |
| `$timeout(fn, 0)` | Digest cycle workaround | `nextTick()` (rarely needed) |
| `ng-if`, `ng-show` | Conditional rendering | `v-if`, `v-show` |
| `ng-repeat` | List rendering | `v-for` |
| `ng-class` | Dynamic CSS classes | `:class` |
| `ng-click` | Event binding | `@click` |
| `{{expression}}` | Interpolation | `{{expression}}` (identical syntax) |

---

## 4. Migration Strategy: Clean Rewrite

### Why Rewrite (Not Incremental)

1. **The codebase is small** (1,294 lines). A rewrite is ~3-5 days of focused work.
2. **No coexistence complexity.** Running AngularJS and Vue side-by-side requires careful bootstrapping, shared state bridges, and dual routing — more effort than the rewrite itself.
3. **Clean break from jQuery.** The loading spinner is the only jQuery usage; a rewrite eliminates it naturally.
4. **Opportunity to simplify.** The 456-line ChartController can be decomposed into smaller, focused composables.
5. **No tests to port.** There are zero tests, so there's no test suite to maintain compatibility with.

### What Stays Unchanged

- **Backend** (Java Spring Boot) — no changes needed
- **API contract** — same endpoint, same response format
- **Chart.js 4.4.7** — stays, just unwrapped from AngularJS directive
- **D3.js v7** — stays, just unwrapped from AngularJS directive
- **Bootstrap 5.3.3** — stays, CSS classes unchanged
- **FreeMarker template** (demographics.html) — stays as-is
- **CSS** (style.css) — stays, minor tweaks only
- **Spring Boot static file serving** — same mechanism

---

## 5. Phased Implementation Plan

### Phase 0: Setup (0.5 days)

**Goal:** Create the new file structure alongside the old one.

- Create `src/main/resources/static/vue/` directory for new components
- Add Vue 3 CDN link to a new `index-vue.html` (copy of current index.html)
- Add Vue Router CDN link (for hash-based routing)
- Verify Vue 3 mounts and renders "Hello World" at `/vue-test` route
- Keep old AngularJS app running at `/index.html` (zero risk)

**Files created:**
```
static/
├── index.html          (existing AngularJS — untouched)
├── index-vue.html      (new Vue app — for testing)
└── vue/
    ├── app.js           (Vue app creation + router)
    ├── composables/
    │   ├── useChartData.js   (data fetching + caching)
    │   ├── useDateFilter.js  (date range state)
    │   └── useLoading.js     (loading state)
    └── components/
        ├── ChartView.js      (main dashboard — replaces ChartController)
        ├── ChartjsChart.js   (Chart.js wrapper — replaces directive)
        ├── ChoroplethMap.js   (D3 map wrapper — replaces directive)
        └── NavPills.js        (navigation — replaces directive)
```

### Phase 1: Core App Shell (1 day)

**Goal:** Navigation, routing, and loading indicator work.

1. Set up Vue 3 app with `createApp()` and mount on `#app`
2. Implement hash-based routing (either vue-router or simple reactive variable — since there's only one route pattern `/:id`, a reactive variable may suffice)
3. Port sidebar navigation from `index.html` — replace `ng-href="#/gender"` with `@click` handlers or `<router-link>`
4. Port `navpills` active-state highlighting — replace directive with `:class="{ active: currentView === 'gender' }"`
5. Port `pills` toggle buttons (display mode, top-N) — replace with `v-model` / `:class` bindings
6. Implement loading composable — replace jQuery-based `loading.js` with reactive `ref(false)` + `v-if`

**Deliverable:** App shell renders with working navigation, active highlighting, and loading spinner. No chart data yet.

### Phase 2: Data Layer (0.5 days)

**Goal:** API calls work, data flows to components.

1. Port `dataService` — replace `$http.get` + `$cacheFactory` with `fetch()` + `Map` cache
2. Port `dateFilterState` — replace factory with reactive `ref()` for from/to dates
3. Port date picker — replace `uib-datepicker-popup` with native `<input type="date">` (Bootstrap 5 styles it well) or a lightweight Vue datepicker
4. Wire up data fetching: on date change → fetch chart data → populate reactive state
5. Port summary statistics cards — replace `{{summaryStats.totalResponses | number}}` with Vue interpolation + `toLocaleString()`

**Deliverable:** Changing date range fetches data and displays summary cards. Charts not yet rendered.

**Note on datepicker:** The UI Bootstrap datepicker is the **only** AngularJS UI component in use. Options:
- **`<input type="date">`** — zero dependencies, works in all modern browsers, native styling. Recommended for simplicity.
- **VueDatePicker** — if popup calendar UX is important, add `vue-datepicker` via CDN.

### Phase 3: Chart Component (1 day)

**Goal:** Bar, area, line, and donut charts render correctly.

1. Port `ng-google-chart.js` directive (381 lines) to a Vue component
   - Replace `$scope.$watch('chartData')` with `watch(props.chartData)`
   - Replace `$elm[0]` with template ref (`ref="canvas"`)
   - Replace `$scope.$on('$destroy')` with `onUnmounted()`
   - Keep all Chart.js configuration logic (colors, tooltips, legend click behavior)
2. Port display mode toggle (bar/area/line/donut) — already handled by `:class` bindings in Phase 1
3. Port Top-N filtering logic from ChartController
4. Port volume chart (separate Chart.js instance)
5. Port trend arrows (prior-period comparison)

**Deliverable:** All chart types render with tooltips, legend interaction, and Top-N filtering.

### Phase 4: Map Component (1 day)

**Goal:** Choropleth maps render correctly.

1. Port `choropleth.js` directive (307 lines) to a Vue component
   - Replace `$scope.$watch('mapData')` with `watch(props.mapData)`
   - Replace `$elm[0]` with template ref
   - Keep all D3 rendering logic (projections, color scales, tooltips, legends)
   - Keep TopoJSON caching
2. Port per-capita toggle for US states map
3. Verify world map and US states map both render

**Deliverable:** Both choropleth views work with tooltips, legends, and normalization toggle.

### Phase 5: Polish and Cutover (0.5 days)

**Goal:** Replace the AngularJS app with the Vue app.

1. Final side-by-side testing — verify every nav item, chart type, date range, and map view
2. Rename `index-vue.html` → `index.html` (replace the old one)
3. Delete all AngularJS files:
   - `js/app.js`, `js/views.js`
   - `js/controller/chartController.js`
   - `js/service/dataService.js`, `js/service/interceptor.js`, `js/service/loading.js`
   - `js/directive/ng-google-chart.js`, `js/directive/choropleth.js`, `js/directive/navpills.js`, `js/directive/pills.js`
   - `views/chart.html`
   - `lib/ui-bootstrap-tpls-2.5.0.min.js`
4. Remove AngularJS CDN references (angular.min.js, angular-route, angular-sanitize, angular-cookies, angular-resource)
5. Remove jQuery CDN reference (no longer needed)
6. Update `CLAUDE.md` and `TASKS.md` to reflect the new stack
7. Verify `mvn clean install` still packages everything correctly

**Deliverable:** Production-ready Vue 3 app. AngularJS fully removed.

### Total Estimated Effort: **4-5 days**

---

## 6. Risk Assessment

### Low Risk

| Risk | Mitigation |
|------|-----------|
| **Chart.js/D3 behavior changes** | These libraries are framework-agnostic. The rendering code stays nearly identical; only the lifecycle wrapper changes. |
| **Broken routing** | Only one route pattern (`/:id`). Trivial to implement in any framework or even without a router. |
| **CSS breakage** | Bootstrap 5 classes are framework-independent. `style.css` doesn't reference any AngularJS classes. |
| **API compatibility** | Backend is untouched. Same endpoint, same response format. |

### Medium Risk

| Risk | Mitigation |
|------|-----------|
| **Date picker UX change** | Moving from UI Bootstrap popup calendar to `<input type="date">` changes the UX slightly. Native date inputs vary by browser. Test on Chrome, Firefox, Safari. If unacceptable, add a Vue datepicker component via CDN. |
| **Digest cycle timing differences** | AngularJS `$timeout` calls exist to work around digest cycles. Vue's reactivity is synchronous by default, so some rendering timing may differ. Use `nextTick()` if needed. |
| **Deep watcher performance** | The current deep watcher on `chartData` is expensive in AngularJS. Vue's deep watchers are more efficient, but the large data object should ideally be replaced with a reactive reference that's replaced (not mutated) to avoid deep comparison. |

### High Risk

| Risk | Mitigation |
|------|-----------|
| **No tests to verify correctness** | There are zero tests. Manual testing is the only verification. Create a checklist of all 12 navigation views (10 demographics + 2 maps) x 4 chart types (bar, area, line, donut) x date range changes = ~96 manual test cases. Consider adding basic Playwright/Cypress E2E tests as part of migration. |
| **Solo maintainer bus factor** | The new framework must be simple enough that someone unfamiliar can maintain it. Vue's template syntax is HTML-like and readable. CDN-based setup has no build tooling to break. Documentation in CLAUDE.md is essential. |

---

## 7. Build Tooling Decision

### Recommendation: No Build Step (CDN Only)

The current setup serves individual JS files directly from `src/main/resources/static/`. The Vue 3 migration can maintain this approach:

```html
<!-- Vue 3 core -->
<script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>

<!-- Optional: Vue Router (if using router instead of reactive variable) -->
<script src="https://unpkg.com/vue-router@4/dist/vue-router.global.prod.js"></script>

<!-- App files (same pattern as current AngularJS) -->
<script src="/vue/composables/useLoading.js"></script>
<script src="/vue/composables/useDateFilter.js"></script>
<script src="/vue/composables/useChartData.js"></script>
<script src="/vue/components/ChartjsChart.js"></script>
<script src="/vue/components/ChoroplethMap.js"></script>
<script src="/vue/components/NavPills.js"></script>
<script src="/vue/components/ChartView.js"></script>
<script src="/vue/app.js"></script>
```

**Pros:**
- Zero new tooling (no npm, node, Vite, Webpack)
- Same deployment model (static files in Spring Boot JAR)
- Same debugging experience (individual source files in browser DevTools)
- Maven build unchanged

**Cons:**
- No TypeScript (can add later with a build step if desired)
- No Single File Components (templates are inline strings or separate HTML)
- No tree-shaking (Vue global build is ~40KB gzipped — acceptable)
- No hot module replacement during development

**Future option:** If the frontend grows significantly, introduce Vite with `vite build` outputting to `src/main/resources/static/dist/`. The Maven frontend-maven-plugin can run Vite during `mvn package`. But this is unnecessary for the current ~1,300 LOC codebase.

---

## 8. What About the Pending T7 Tasks?

Several Track 7 tasks are pending. Here's how they interact with the migration:

| Task | Status | Migration Impact |
|------|--------|-----------------|
| **T7.13** Cross-tabulation | Deferred (Hard) | Implement in Vue after migration. Backend work needed first. |
| **T7.14** Worker retention | Deferred (Hard) | Implement in Vue after migration. Backend work needed first. |
| **T7.15** Response time trends | Pending (Moderate) | Implement in Vue after migration. Small frontend addition. |
| **T7.19** Sparklines | Pending (Easy) | Implement in Vue after migration. New Chart.js component. |
| **T7.23** Heatmap view | Pending (Moderate) | Implement in Vue after migration. New D3 component. |

**Recommendation:** Complete the Vue migration first (T7.1), then implement pending features in Vue. Don't add new features to the AngularJS codebase.

---

## 9. Migration Checklist

### Pre-Migration
- [ ] Verify current AngularJS app works correctly (baseline)
- [ ] Document all 12 navigation views and expected behavior
- [ ] Screenshot current dashboard for visual comparison

### Phase 0: Setup
- [ ] Add Vue 3 CDN to `index-vue.html`
- [ ] Create `vue/` directory structure
- [ ] Verify Vue mounts successfully

### Phase 1: App Shell
- [ ] Sidebar navigation renders with all 12 items
- [ ] Active item highlighting works
- [ ] Display mode buttons (Bar/Area/Line/Donut) toggle correctly
- [ ] Top-N filter buttons toggle correctly
- [ ] Loading spinner shows during API calls

### Phase 2: Data Layer
- [ ] API call to `/api/survey/demographics/chartData` works
- [ ] Response caching works (same date range doesn't re-fetch)
- [ ] Date pickers allow selecting from/to dates
- [ ] Summary statistics cards show correct numbers
- [ ] Trend arrows show correct direction and percentage

### Phase 3: Charts
- [ ] Bar chart renders correctly for all 10 demographics
- [ ] Area chart renders correctly
- [ ] Line chart renders correctly
- [ ] Donut chart renders correctly (latest period)
- [ ] Volume chart renders correctly
- [ ] Tooltips show count + percentage + total
- [ ] Legend click focuses/dims datasets
- [ ] Top-N filtering works with "Other" aggregation

### Phase 4: Maps
- [ ] World choropleth renders with country data
- [ ] US states choropleth renders
- [ ] Per-capita toggle works for US states
- [ ] Map tooltips show correct values
- [ ] Color legends display correctly

### Phase 5: Cutover
- [ ] All 12 views verified working
- [ ] Old AngularJS files deleted
- [ ] jQuery reference removed
- [ ] UI Bootstrap reference removed
- [ ] `mvn clean install` succeeds
- [ ] CLAUDE.md updated (tech stack section)
- [ ] TASKS.md updated (T7.1 marked complete)

---

## 10. Summary

| Dimension | Current | After Migration |
|-----------|---------|----------------|
| **Framework** | AngularJS 1.8.3 (EOL) | Vue 3.x (active LTS) |
| **Routing** | angular-route (ngRoute) | Vue Router 4 or reactive variable |
| **HTTP** | $http + $cacheFactory | fetch() + Map cache |
| **State** | $scope (100+ properties) | Composition API ref()/reactive() |
| **Reactivity** | $scope.$watch (manual) | Automatic dependency tracking |
| **Charting** | Chart.js 4.4.7 (unchanged) | Chart.js 4.4.7 (unchanged) |
| **Maps** | D3.js v7 (unchanged) | D3.js v7 (unchanged) |
| **CSS** | Bootstrap 5.3.3 (unchanged) | Bootstrap 5.3.3 (unchanged) |
| **Build** | No bundler (unchanged) | No bundler (unchanged) |
| **jQuery** | Used for loading spinner | Removed |
| **Bundle size** | ~230KB (AngularJS + modules) | ~40KB (Vue 3 global) |
| **Estimated effort** | — | **4-5 days** |
