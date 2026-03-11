/**
 * Vue 3 App - replaces AngularJS module
 * Uses hash-based routing with Vue Router
 */
(function() {
    const { createApp } = Vue;
    const { createRouter, createWebHashHistory } = VueRouter;

    const router = createRouter({
        history: createWebHashHistory(),
        routes: [
            {
                path: '/:id',
                component: {
                    props: ['id'],
                    template: '<chart-view :route-id="id"></chart-view>',
                    components: { 'chart-view': ChartView }
                },
                props: function(route) { return { id: route.params.id }; }
            },
            { path: '/', redirect: '/gender' }
        ]
    });

    const app = createApp({
        setup() {
            const { ref, computed, watch } = Vue;
            const route = VueRouter.useRoute();
            const currentId = computed(function() {
                return route.params.id || '';
            });

            // Sidebar toggle
            function toggleSidebar() {
                var el = document.getElementById('sidebarNav');
                if (el.className.indexOf('show') >= 0) {
                    el.className = 'sidebar-collapse';
                } else {
                    el.className = 'sidebar-collapse show';
                }
            }

            return { currentId, toggleSidebar };
        }
    });

    app.use(router);
    app.mount('#app');
})();
