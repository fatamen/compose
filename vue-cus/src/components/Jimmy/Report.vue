<template>
    <div class="report-wrapper">
        <button class="report-btn" @click="toggleDropdown">
            <i class="bi bi-flag"></i> <!-- 使用 Bootstrap Icons 的旗幟圖標 -->
        </button>
        <div class="report-dropdown" v-if="showDropdown">
            <ul>
                <li v-for="item in report" :key="item.header" @click="selectReport(item)"
                    @mouseenter="showTooltip(item)" @mouseleave="hideTooltip" class="report-item">
                    {{ item.header }}
                    <div class="tooltip" v-if="activeTooltip === item.header">
                        {{ item.content }}
                    </div>
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
    report: {
        type: Array,
        required: true,
    },
});

const showDropdown = ref(false);
const activeTooltip = ref(null);

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
};

const selectReport = (item) => {
    console.log(`檢舉選項: ${item.header} - ${item.content}`);
    // 未來可在此處添加 API 請求或其他邏輯
    showDropdown.value = false; // 選擇後關閉下拉選單
};

const showTooltip = (item) => {
    activeTooltip.value = item.header;
};

const hideTooltip = () => {
    activeTooltip.value = null;
};
</script>

<style scoped>
.report-wrapper {
    position: relative;
    display: inline-block;
}

.report-btn {
    background: none;
    border: none;
    color: #999;
    font-size: 16px;
    cursor: pointer;
    padding: 5px;
    transition: color 0.2s;
}

.report-btn:hover {
    color: #ffba20;
}

.report-dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    z-index: 2100;
    /* 高於 .comment-popout 的 z-index */
    min-width: 150px;
    max-height: 200px;
    overflow-y: auto;
}

.report-dropdown ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.report-item {
    padding: 10px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    position: relative;
    transition: background-color 0.2s;
}

.report-item:hover {
    background-color: #f5f5f5;
}

.tooltip {
    position: absolute;
    top: 0;
    right: 100%;
    background-color: #333;
    color: #fff;
    padding: 8px;
    border-radius: 4px;
    font-size: 12px;
    width: 200px;
    z-index: 2200;
    transform: translateY(-10%);
    pointer-events: none;
    /* 防止 tooltip 影響滑鼠事件 */
}

@media (max-width: 768px) {
    .report-dropdown {
        min-width: 120px;
    }

    .tooltip {
        width: 150px;
        font-size: 10px;
    }
}
</style>