/**
 * Loading state composable - replaces jQuery-based loading.js
 */
const useLoading = () => {
    const { ref } = Vue;
    const loading = ref(false);
    const requestCount = ref(0);

    function show() {
        requestCount.value++;
        loading.value = true;
    }

    function hide() {
        requestCount.value--;
        if (requestCount.value <= 0) {
            requestCount.value = 0;
            loading.value = false;
        }
    }

    return { loading, show, hide };
};
