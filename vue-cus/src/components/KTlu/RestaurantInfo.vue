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
        <h3 class="hours-title">營業時間</h3>

        <!-- 如果是陣列格式（從後端 API 取得） -->
        <div v-if="Array.isArray(restaurant.businessHours)" class="business-hours-list">
            <div v-for="(time, index) in restaurant.businessHours" :key="index" class="hours-item">
                <div class="day-info">
                    <span class="day-name">{{ time.dayName }}</span>
                </div>
                <div v-if="time.isOpen" class="hours-time">
                    <i class="pi pi-clock time-icon"></i>
                    <span class="time-text">{{ time.openTimeStr }} - {{ time.closeTimeStr }}</span>
                </div>
                <div v-else class="hours-closed">
                    <i class="pi pi-calendar-times closed-icon"></i>
                    <span class="closed-text">休息日</span>
                </div>
            </div>
        </div>

        <!-- 如果是字串格式（從本地資料取得） -->
        <div v-else class="business-hours-display">
            <div class="hours-content">
                <i class="pi pi-clock hours-icon"></i>
                <p class="hours-text">{{ restaurant.businessHours }}</p>
            </div>
        </div>
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
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
    transition: all 0.3s ease;
}

.restaurant-info:hover {
    box-shadow: 0 4px 16px var(--restaurant-shadow-light);
    transform: translateY(-1px);
}

.info-title {
    color: var(--restaurant-primary);
    font-size: 1.05 rem;
    font-weight: 600;
    margin-bottom: 1rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
    position: relative;
    padding-bottom: 0.5rem;
}

.info-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 2px;
    background: var(--restaurant-primary);
    border-radius: 1px;
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
    padding: 0.75rem 1rem;
    background: var(--restaurant-bg-light);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
    transition: all 0.2s ease;
}

.info-item:hover {
    background: var(--restaurant-bg-accent);
    border-color: var(--restaurant-primary-light);
    transform: translateX(2px);
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
    font-weight: 500;
}

.phone-link:hover {
    color: var(--restaurant-primary);
    text-decoration: none;
}

.hours-title {
    color: var(--restaurant-primary);
    font-size: 1.05 rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
    position: relative;
    padding-bottom: 0.5rem;
}

.hours-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 30px;
    height: 2px;
    background: var(--restaurant-primary);
    border-radius: 1px;
}

/* 字串格式營業時間顯示 */
.business-hours-display {
    background: var(--restaurant-bg-light);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
    padding: 1rem;
    transition: all 0.2s ease;
}

.business-hours-display:hover {
    background: var(--restaurant-bg-accent);
    border-color: var(--restaurant-primary-light);
}

.hours-content {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.hours-icon {
    color: var(--restaurant-primary);
    font-size: 1.4 rem;
}

.hours-text {
    color: var(--restaurant-text-primary);
    margin: 0;
    line-height: 1.5;
    font-size: 1.00 rem;
    font-weight: 500;
}

/* 陣列格式營業時間顯示 */
.business-hours-list {
    background: var(--restaurant-bg-light);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
    overflow: hidden;
}

.hours-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    border-bottom: 1px solid var(--restaurant-border-light);
    transition: all 0.2s ease;
}

.hours-item:last-child {
    border-bottom: none;
}

.hours-item:hover {
    background: var(--restaurant-bg-accent);
}

.day-info {
    display: flex;
    align-items: center;
}

.day-name {
    color: var(--restaurant-text-primary);
    font-weight: 500;
    font-size: 1.20 rem;
    min-width: 60px;
}

.hours-time {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--restaurant-text-secondary);
    font-size: 0.85rem;
    font-weight: 500;
}

.time-icon {
    color: var(--restaurant-primary);
    font-size: 0.9rem;
}

.time-text {
    color: var(--restaurant-text-secondary);
    font-weight: 600;
}

.hours-closed {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--restaurant-text-muted);
    font-size: 0.85rem;
    font-weight: 500;
}

.closed-icon {
    color: var(--restaurant-error);
    font-size: 0.9rem;
}

.closed-text {
    color: var(--restaurant-error);
    font-weight: 600;
    font-style: italic;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .restaurant-info {
        width: 100%;
        padding: 1rem;
        margin: 1rem 0;
    }

    .info-title,
    .hours-title {
        font-size: 1.1rem;
    }

    .hours-item {
        flex-direction: column;
        align-items: flex-start !important;
        gap: 0.25rem;
        padding: 0.75rem;
    }

    .day-name {
        min-width: auto;
        margin-bottom: 0.25rem;
    }

    .hours-time,
    .hours-closed {
        align-self: flex-end;
    }

    .info-item {
        padding: 0.75rem;
    }

    .business-hours-display {
        padding: 0.75rem;
    }

    .hours-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }
}

@media (max-width: 480px) {
    .restaurant-info {
        width: 100%;
        padding: 0.75rem;
    }

    .info-title,
    .hours-title {
        font-size: 1rem;
    }

    .hours-item {
        padding: 0.75rem;
    }

    .day-name {
        font-size: 0.85rem;
    }

    .time-text,
    .closed-text {
        font-size: 0.8rem;
    }
}
</style>