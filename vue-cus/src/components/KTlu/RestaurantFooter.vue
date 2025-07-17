<template>
    <footer class="restaurant-footer goldenbowl-restaurant-theme">
        {{ restaurant.copyrightText || '© 2021 Restaurant All Rights Reserved' }}
    </footer>
</template>

<script setup>
import '@/assets/css/restaurant-theme.css'

defineProps({
    restaurant: {
        type: Object,
        required: true
    }
})
</script>

<style scoped>
.restaurant-footer {
    text-align: center;
    color: var(--restaurant-text-secondary);
    margin-top: 3rem;
    padding: 2rem 0;
    font-size: 0.9rem;
    border-top: 2px solid var(--restaurant-border-light);
    background: var(--restaurant-bg-secondary);
}
</style>