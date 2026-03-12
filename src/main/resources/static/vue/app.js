/**
 * Vue 3 App - Main entry point
 * Replaces AngularJS module (app.js, views.js, routing)
 */
(function() {
    const { createApp, ref, computed, watch } = Vue;

    const routes = [
        { path: '/:id', component: ChartView, props: function(route) { return { viewId: route.params.id }; } }
    ];

    const router = VueRouter.createRouter({
        history: VueRouter.createWebHashHistory(),
        routes: routes
    });

    // Redirect root to /gender
    router.beforeEach(function(to, from, next) {
        if (to.path === '/') {
            next('/gender');
        } else {
            next();
        }
    });

    const app = createApp({
        setup() {
            const currentRoute = computed(() => {
                return router.currentRoute.value.params.id || 'gender';
            });

            return { currentRoute };
        }
    });

    app.component('chartjs-chart', ChartjsChart);
    app.component('choropleth-map', ChoroplethMap);
    app.component('chart-view', ChartView);

    app.use(router);
    app.mount('#app');
})();
