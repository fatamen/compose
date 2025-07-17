<script setup>
import { computed } from 'vue';

// 1. 定義接口 (Props)
const props = defineProps({
    specs: { type: Array, required: true }
});

// 2. 定義接口 (Emits)，這跟Props都是組件的合約
const emit = defineEmits(['addNewSpec', 'editSpec']);


// 3. 核心邏輯：使用 computed 為每個 spec 增加分好欄的選項資料
const specsWithCategorizedOptions = computed(() => {
    return props.specs.map(spec => {
        // 為每個 spec 物件增加兩個新的屬性：availableOptions 和 unavailableOptions
        const availableOptions = spec.options.filter(opt => opt.status === '供應中');
        const unavailableOptions = spec.options.filter(opt => opt.status === '暫停供應');

        return {
            ...spec, // 保留原始 spec 的所有屬性 (id, name, etc.)
            availableOptions, // 供應中的選項陣列
            unavailableOptions, // 暫停供應的選項陣列
        };
    });
});

// 4. 組件方法 (Methods)，處理用戶交互
const handleAddNew = () => emit('addNewSpec');
const handleEdit = (spec) => emit('editSpec', spec);

// 輔助函式，用來格式化選項預覽文字
const formatOptionsToString = (options) => {
    if (!options || options.length === 0) {
        return '-'; // 如果沒有選項，顯示 "-"
    }
    return options.map(opt => opt.name).join('、');
};
</script>

<template>
    <div>
        <!-- Header Section -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="input-group" style="max-width: 400px;">
                <span class="input-group-text">🔍</span>
                <input type="text" class="form-control" placeholder="搜尋選項">
            </div>
            <button class="btn btn-warning" @click="handleAddNew">+ 建立客製化規格</button>
        </div>

        <!-- List Container with a Sticky Header -->
        <div class="list-container border rounded">
            <!-- 1. 固定的列表標頭 -->
            <div class="list-header d-flex align-items-center p-3 bg-light">
                <div class="col-drag-handle"></div>
                <div class="col-spec-name fw-bold">客製化類別</div>

                <!-- 供應中欄位標頭 -->
                <div class="col-options-available fw-bold">
                    <!-- 將圓點用 span 包裹，並加上 Bootstrap 的 text-success class -->
                    <span class="text-success me-1">●</span>
                    <span>選項供應中</span>
                </div>

                <!-- 暫停供應欄位標頭 -->
                <div class="col-options-unavailable fw-bold">
                    <!-- 將圓點用 span 包裹，並加上 Bootstrap 的 text-danger class -->
                    <span class="text-danger me-1">●</span>
                    <span>選項暫停供應</span>
                </div>
            </div>

            <!-- 2. 規格列表 -->
            <div class="list-body">
                <!-- 現在遍歷的是我們處理過的新陣列 -->
                <div 
                    v-for="spec in specsWithCategorizedOptions" 
                    :key="spec.id" 
                    class="list-group-item d-flex align-items-center p-3"
                    @click="handleEdit(spec)"
                    style="cursor: pointer;"
                >
                    <!-- 拖曳圖標欄 -->
                    <div class="col-drag-handle">
                        <span class="drag-handle">☰</span>
                    </div>
                    <!-- 規格名稱欄 -->
                    <div class="col-spec-name">
                        <p class="fw-bold mb-0">{{ spec.name }}</p>
                        <small class="text-muted">
                            選填 {{ spec.minSelection }} - {{ spec.maxSelection }} 可選項目
                        </small>
                    </div>
                    <!-- 供應中選項欄 -->
                    <div class="col-options-available">
                        {{ formatOptionsToString(spec.availableOptions) }}
                    </div>
                    <!-- 暫停供應選項欄 -->
                    <div class="col-options-unavailable">
                        {{ formatOptionsToString(spec.unavailableOptions) }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 使用 CSS 來定義各欄位的寬度，確保對齊 */
.list-header, .list-group-item {
    border-bottom: 1px solid #dee2e6;
}
.list-group-item:last-child {
    border-bottom: none;
}

/* 拖曳圖標欄 */
.col-drag-handle {
    flex: 0 0 40px; /* 不縮放，不擴展，基礎寬度 40px */
    text-align: center;
    color: #6c757d;
}

/* 規格名稱欄 */
.col-spec-name {
    flex: 1 1 25%; /* 可縮放，可擴展，基礎佔比 25% */
    padding-right: 1rem;
}

/* 供應中選項欄 */
.col-options-available {
    flex: 1 1 35%; /* 佔比 35% */
    padding-right: 1rem;
    color: #212529;
}

/* 暫停供應選項欄 */
.col-options-unavailable {
    flex: 1 1 35%; /* 佔比 35% */
    color: #6c757d;
}

</style>