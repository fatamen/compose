<template>
    <div class="restaurant-map goldenbowl-restaurant-theme">
        <h4 class="map-title">地圖</h4>
        <div class="map-container">
            <iframe :src="mapUrl" allowfullscreen :title="restaurant.name + '地圖'" class="map-frame"></iframe>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import '@/assets/css/restaurant-theme.css'

const props = defineProps({
    restaurant: {
        type: Object,
        required: true
    }
})

const mapUrl = computed(() => {
    return `https://www.google.com/maps?q=${encodeURIComponent(props.restaurant.address)}&output=embed`
})
</script>

<style scoped>
.restaurant-map {
    width: 50%;
    margin: 2rem 0;
    padding: 1.5rem;
    background: var(--restaurant-bg-light);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
}

.map-title {
    color: var(--restaurant-primary);
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 1rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
}

.map-container {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 56.25%;
    /* 16:9 比例 */
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px var(--restaurant-shadow-medium);
    border: 2px solid var(--restaurant-primary-light);
}

.map-frame {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: none;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .restaurant-map {
        width: 70%;
    }

    .map-container {
        padding-bottom: 75%;
        /* 移動端使用4:3比例 */
    }
}

@media (max-width: 480px) {
    .restaurant-map {
        width: 90%;
    }
}
</style>