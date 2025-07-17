<template>
    <div class="time-picker-sectioned goldenbowl-restaurant-theme">
        <div v-for="section in sections" :key="section.label" class="time-section">
            <h6 class="section-label">{{ section.label }}</h6>
            <div class="time-slot-group">
                <button v-for="slot in section.slots" :key="slot" :class="timeSlotClasses(slot)"
                    @click="selectTime(slot)" :disabled="isDisabled(slot)">
                    <span class="time-text">{{ slot }}</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import '@/assets/css/restaurant-theme.css'

const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    sections: {
        type: Array,
        default: () => []
    },
    disabledSlots: {
        type: Array,
        default: () => []
    },
    selectedDate: {
        type: Date,
        default: null
    }
})

const emit = defineEmits(['update:modelValue'])

const selectTime = (slot) => {
    if (!isDisabled(slot)) {
        emit('update:modelValue', slot)
    }
}

// 檢查時間段是否過期
const isTimeExpired = (timeSlot) => {
    if (!props.selectedDate) return false

    try {
        const now = new Date()
        const selectedDate = new Date(props.selectedDate)

        // 如果是今天，檢查時間是否已過
        if (selectedDate.toDateString() === now.toDateString()) {
            const [hours, minutes] = timeSlot.split(':').map(Number)
            const slotTime = new Date(now)
            slotTime.setHours(hours, minutes, 0, 0)

            // 如果時間段在當前時間之前，則為過期
            return slotTime <= now
        }

        return false
    } catch (error) {
        console.error('檢查時間過期時發生錯誤:', error)
        return false
    }
}

const isDisabled = (slot) => {
    // 檢查是否為已預訂的時間段
    const isBooked = props.disabledSlots.includes(slot)

    // 檢查是否為過期的時間段
    const isExpired = isTimeExpired(slot)

    return isBooked || isExpired
}

const timeSlotClasses = (slot) => {
    const isExpired = isTimeExpired(slot)
    const isBooked = props.disabledSlots.includes(slot)

    return [
        'time-slot',
        {
            'selected': props.modelValue === slot,
            'disabled': isBooked || isExpired,
            'expired': isExpired
        }
    ]
}
</script>

<style scoped>
.time-picker-sectioned {
    margin: 1rem 0;
}

.time-section {
    margin-bottom: 1.5rem;
}

.section-label {
    color: var(--restaurant-primary);
    font-size: 0.9rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
}

.time-slot-group {
    display: grid;
    grid-template-columns: repeat(4, 120px);
    gap: 0.75rem;
    justify-content: start;
}

.time-slot {
    background: var(--restaurant-bg-primary);
    border: 2px solid var(--restaurant-border-medium);
    border-radius: 8px;
    padding: 0.75rem 0.5rem;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 500;
    font-size: 0.9rem;
    width: 120px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--restaurant-text-primary);
    box-shadow: 0 1px 4px var(--restaurant-shadow-light);
}

.time-slot:hover:not(.disabled) {
    border-color: var(--restaurant-primary);
    background: var(--restaurant-shadow-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium);
}

.time-slot.selected {
    background: var(--restaurant-gradient-primary);
    border-color: var(--restaurant-primary);
    color: var(--restaurant-text-primary);
    font-weight: 600;
    box-shadow: 0 4px 16px var(--restaurant-shadow-medium);
}

.time-slot.disabled {
    background: var(--restaurant-bg-light);
    border-color: var(--restaurant-border-light);
    color: var(--restaurant-text-muted);
    cursor: not-allowed;
    opacity: 0.6;
}

.time-slot.expired {
    background: #f8f9fa;
    border-color: #dee2e6;
    color: #6c757d;
    cursor: not-allowed;
    opacity: 0.4;
    text-decoration: line-through;
}

.time-slot.expired:hover {
    transform: none;
    box-shadow: none;
}

.time-text {
    font-size: 0.9rem;
    line-height: 1;
}

/* 響應式設計 - 保持固定大小 */
@media (max-width: 768px) {
    .time-slot-group {
        grid-template-columns: repeat(3, 110px);
        gap: 0.5rem;
        justify-content: start;
    }

    .time-slot {
        width: 110px;
        height: 44px;
        padding: 0.5rem 0.25rem;
    }

    .time-text {
        font-size: 0.8rem;
    }
}

@media (max-width: 480px) {
    .time-slot-group {
        grid-template-columns: repeat(2, 140px);
        gap: 0.5rem;
        justify-content: center;
    }

    .time-slot {
        width: 140px;
        height: 44px;
    }
}
</style>