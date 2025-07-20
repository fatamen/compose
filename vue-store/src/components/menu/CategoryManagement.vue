<script setup>
import { computed, ref } from 'vue';

// 1. Props & Emits: 組件的合約
const props = defineProps({
    categories: {
        type: Array,
        required: true
    }
});
const emit = defineEmits(['addNewCategory', 'editCategory','updateCategoryOrder']);

// 2. 核心邏輯：只做排序，不處理任何拖曳狀態
const sortedCategories = computed(() => {
    // 確保列表總是按 sort 排序後再渲染
    return [...props.categories].sort((a, b) => a.sort - b.sort);
});

// 3. 事件處理函式：只保留最基本的新增和編輯
const handleAddNew = () => {
    emit('addNewCategory');
};
const handleEdit = (category) => {
    emit('editCategory', category);
};

// 4. 拖曳實驗
const isDragging = ref(false);
const draggedItem = ref(null);
const dragOverIndex = ref(-1);

// 拖曳開始
const handleDragStart = (event, category, index) => {
    isDragging.value = true;
    draggedItem.value = { category, index };
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/html', event.target.outerHTML);
    event.target.style.opacity = '0.5';
};

// 拖曳結束
const handleDragEnd = (event) => {
    isDragging.value = false;
    draggedItem.value = null;
    dragOverIndex.value = -1;
    event.target.style.opacity = '1';
};

// 拖曳經過
const handleDragOver = (event, index) => {
    event.preventDefault();
    dragOverIndex.value = index;
};

// 拖曳離開
const handleDragLeave = () => {
    dragOverIndex.value = -1;
};

// 放置
const handleDrop = (event, targetIndex) => {
    event.preventDefault();
    
    if (!draggedItem.value || draggedItem.value.index === targetIndex) {
        return;
    }
    
    const sourceIndex = draggedItem.value.index;
    const newCategories = [...sortedCategories.value];
    
    // 移動項目
    const [movedItem] = newCategories.splice(sourceIndex, 1);
    newCategories.splice(targetIndex, 0, movedItem);
    
    // 重新計算 sort 值
    const updatedCategories = newCategories.map((category, index) => ({
        ...category,
        sort: index + 1
    }));
    
    // 發送更新事件給父組件
    emit('updateCategoryOrder', updatedCategories);
};

</script>

<template>
    <div>
        <!-- 頂部操作區 -->
        <div class="d-flex justify-content-end mb-3">
            <button class="btn btn-warning" @click="handleAddNew">
                + 新增品項類別
            </button>
        </div>

        <!-- 類別列表容器 -->
        <div class="list-group">
            <!-- 空狀態提示 -->
            <div v-if="!sortedCategories || sortedCategories.length === 0" class="list-group-item text-center text-muted">
                目前沒有任何品項類別，請點擊右上角新增。
            </div>

            <!-- 列表渲染：新增拖曳功能 -->
            <div
                v-else
                v-for="(category, index) in sortedCategories"
                :key="category.id"
                class="list-group-item list-group-item-action d-flex align-items-center"
                :class="{
                    'dragging': isDragging && draggedItem?.index === index,
                    'drag-over': dragOverIndex === index
                }"
                draggable="true"
                @dragstart="handleDragStart($event, category, index)"
                @dragend="handleDragEnd"
                @dragover="handleDragOver($event, index)"
                @dragleave="handleDragLeave"
                @drop="handleDrop($event, index)"
                @click="handleEdit(category)"
                style="cursor: pointer;"
            >
                <!-- 拖曳圖標 -->
                <span class="me-3 text-muted drag-handle">☰</span>
                
                <!-- 類別名稱 -->
                <span class="fw-bold flex-grow-1">{{ category.name }}</span>
                
                <!-- 排序數字 -->
                <span class="badge bg-secondary rounded-pill">{{ category.sort }}</span>
            </div>

            
        </div>
    </div>
</template>

<style scoped>
.dragging {
    opacity: 0.5;
    transform: scale(0.95);
}

.drag-over {
    border-top: 3px solid #007bff;
    background-color: #f8f9fa;
}

.drag-handle {
    cursor: grab;
}

.drag-handle:active {
    cursor: grabbing;
}

.list-group-item {
    transition: all 0.2s ease;
}
</style>