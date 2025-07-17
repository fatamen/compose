<template>
<div class="report-button-container">
    <i class="bi bi-flag report-icon" @click="emit('openReportModal')"></i>
    <span class="report-text" @click="emit('openReportModal')">檢舉</span>
</div>
</template>

<script setup>
// 沒有 props 和複雜邏輯，只是一個觸發事件的按鈕
const emit = defineEmits(['openReportModal']);
</script>

<style scoped>
.report-button-container {
display: flex;
align-items: center;
gap: 5px; /* 圖示和文字之間的間距 */
cursor: pointer;
color: #888; /* 灰色 */
font-size: 0.9em;
transition: color 0.2s ease;
}

.report-button-container:hover {
color: #dc3545; /* 懸停時變紅色 */
}

.report-icon {
font-size: 1.1em;
}

.report-text {
user-select: none; /* 防止文字被選中 */
}
</style>