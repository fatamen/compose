<template>
    <div class="restaurant-info goldenbowl-restaurant-theme">
        <h3 class="info-title">餐廳資訊</h3>
        <ul class="info-list">
            <li class="info-item">
                <i class="pi pi-map-marker info-icon"></i>
                <span>{{ restaurant.address }}</span>
            </li>
            <li class="info-item">
                <i class="pi pi-phone info-icon"></i>
                <a :href="'tel:' + restaurant.phone" class="phone-link">{{ restaurant.phone }}</a>
            </li>
        </ul>
        <h4 class="hours-title">營業時間</h4>
        <p class="hours-text">{{ restaurant.businessHours }}</p>
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
.restaurant-info {
    width: 100%;
    margin: 2rem 0;
    padding: 1.5rem;
    background: var(--restaurant-bg-light);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
}

.info-title {
    color: var(--restaurant-primary);
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 1rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
}

.info-list {
    list-style: none;
    padding: 0;
    margin: 0 0 1.5rem 0;
}

.info-item {
    display: flex;
    align-items: center;
    margin-bottom: 0.75rem;
    color: var(--restaurant-text-primary);
    padding: 0.75rem;
    background: var(--restaurant-bg-primary);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
    transition: all 0.2s ease;
}

.info-item:hover {
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
    border-color: var(--restaurant-primary-light);
}

.info-icon {
    color: var(--restaurant-primary);
    margin-right: 0.75rem;
    font-size: 1.1rem;
    width: 20px;
    text-align: center;
}

.phone-link {
    color: inherit;
    text-decoration: none;
    transition: color 0.2s ease;
}

.phone-link:hover {
    color: var(--restaurant-primary);
    text-decoration: underline;
}

.hours-title {
    color: var(--restaurant-primary);
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
}

.hours-text {
    color: var(--restaurant-text-primary);
    margin: 0;
    line-height: 1.5;
    padding: 0.75rem;
    background: var(--restaurant-bg-primary);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
}

/* 響應式設計 */
@media (max-width: 768px) {
    .restaurant-info {
        width: 100%;
        padding: 1rem;
    }

    .info-title,
    .hours-title {
        font-size: 1.1rem;
    }
}

@media (max-width: 480px) {
    .restaurant-info {
        width: 100%;
    }
}
</style>