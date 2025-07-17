<template>
    <div class="restaurant-banner goldenbowl-restaurant-theme">
        <img :src="restaurant.image" :alt="restaurant.name + '店面圖片'" class="banner-image" />
    </div>
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
.restaurant-banner {
    position: relative;
    width: 100%;
    height: 400px;
    margin: 0 0 0 0;
    overflow: hidden;
    border-bottom: 3px solid var(--restaurant-primary);
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
}

.banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
}

.banner-image:hover {
    transform: scale(1.02);
}

/* 響應式設計 */
@media (max-width: 768px) {
    .restaurant-banner {
        height: 300px;
        width: 100%;
    }
}

@media (max-width: 480px) {
    .restaurant-banner {
        height: 250px;
        width: 100%;
    }
}
</style>