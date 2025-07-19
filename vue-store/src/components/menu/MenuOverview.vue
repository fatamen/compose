<script setup>
import { ref, computed } from 'vue';

// 1. 定義 Props
const props = defineProps({
    items: { type: Array, required: true },
    categories: { type: Array, required: true }
});

// 2. 定義 Emits
const emit = defineEmits(['addNewItem', 'editItem']);

// 3. 核心邏輯：使用 computed 將扁平的 items 陣列轉換為按 categoryId 分組的物件
const groupedItems = computed(() => {
    // 先按 category 的 order 排序
    const sortedCategories = [...props.categories].sort((a, b) => a.order - b.order);

    // 使用 reduce 建立一個 { 'cat-1': { name: '招牌飲品', items: [...] }, ... } 結構
    return sortedCategories.reduce((acc, category) => {
        acc[category.id] = {
            name: category.name,
            items: props.items.filter(item => item.categoryId === category.id)
        };
        return acc;
    }, {});
});

// 4. 內部狀態：用一個陣列來記錄哪些類別是展開的
const expandedCategories = ref(
    // 預設將第一個類別展開
    props.categories.length > 0 ? [props.categories.sort((a,b) => a.order - b.order)[0].id] : []
);

// 5. 方法：處理類別的展開/收合
const toggleCategory = (categoryId) => {
    const index = expandedCategories.value.indexOf(categoryId);
    if (index === -1) {
        // 如果不在陣列中，就加進去 (展開)
        expandedCategories.value.push(categoryId);
    } else {
        // 如果在陣列中，就移除 (收合)
        expandedCategories.value.splice(index, 1);
    }
};

// 輔助函式，檢查某類別是否展開
const isCategoryExpanded = (categoryId) => {
    return expandedCategories.value.includes(categoryId);
};
</script>

<template>
    <div>
        <!-- 頂部的新增按鈕和搜尋框 (不變) -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="input-group" style="max-width: 400px;">
                <span class="input-group-text">🔍</span>
                <input type="text" class="form-control" placeholder="搜尋品項">
            </div>
            <button class="btn btn-warning" @click="emit('addNewItem')">+ 新增</button>
        </div>

        <!-- 列表區域：現在我們要遍歷分好組的 groupedItems -->
        <div class="category-list-container">
            <div v-for="(group, categoryId) in groupedItems" :key="categoryId" class="list-group mb-3">
                <!-- 品項類別 Header -->
                <div class="list-group-item list-group-item-light fw-bold d-flex justify-content-between align-items-center" @click="toggleCategory(categoryId)" style="cursor: pointer;">
                    <span>{{ group.name }}</span>
                    <!-- 根據展開狀態顯示不同圖標 -->
                    <span class="fs-5">{{ isCategoryExpanded(categoryId) ? '▼' : '▲' }}</span>
                </div>

                <!-- 屬於該類別的品項列表，使用 v-show 來控制顯示/隱藏 -->
                <div v-show="isCategoryExpanded(categoryId)">
                    <div v-for="item in group.items" :key="item.id" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center ps-5" @click="emit('editItem', item)">
                        <div class="d-flex align-items-center">
                            <span class="me-3">☰</span>
                            <img :src="item.img" alt="" width="60" height="60" class="me-3 rounded">
                            <span>{{ item.name }}</span>
                        </div>
                        <div class="d-flex align-items-center">
                            <span class="me-4">NT$ {{ item.price }}</span>
                            <!-- 狀態按鈕，這裡為了簡化先只顯示文字 -->
                            <span class="badge" :class="item.status === '供應中' ? 'bg-success' : 'bg-secondary'">
                                {{ item.status }}
                            </span>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</template>