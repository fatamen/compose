<script setup>
import { ref, computed, watch, onMounted } from 'vue';
// import defaultPhoto from '../../assets/default-photo.jpg';
import { useImageUrl } from '../../composables/useImageUrl.js';

// 1. 定義 Props
const props = defineProps({
    items: { type: Array, required: true },
    categories: { type: Array, required: true }
});

// 2. 定義 Emits
const emit = defineEmits(['addNewItem', 'editItem']);

// 3. 核心邏輯：使用 computed 將扁平的 items 陣列轉換為按 categoryId 分組的物件
const groupedItems = computed(() => {
    // 先按 category 的 order 排序（如果沒有 order 屬性，改用 sort）
    const sortedCategories = [...props.categories].sort((a, b) => (a.order || a.sort || 0) - (b.order || b.sort || 0));

    // 使用 reduce 建立一個 { 'cat-1': { name: '招牌飲品', items: [...] }, ... } 結構
    return sortedCategories.reduce((acc, category) => {
        acc[category.id] = {
            name: category.name,
            items: props.items.filter(item => item.categoryId === category.id)
        };
        return acc;
    }, {});
});

// 4. localStorage 的鍵名
const STORAGE_KEY = 'menu_overview_expanded_categories';

// 5. 從 localStorage 讀取展開狀態
const loadExpandedStateFromStorage = () => {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        const result = saved ? JSON.parse(saved) : [];
        console.log('🔍 從 localStorage 讀取展開狀態:', {
            rawSaved: saved,
            parsed: result
        });
        return result;
    } catch (error) {
        console.warn('❌ 無法從 localStorage 讀取展開狀態:', error);
        return [];
    }
};

// 6. 儲存展開狀態到 localStorage
const saveExpandedStateToStorage = (expandedIds) => {
    try {
        // 確保將 Proxy 物件轉換為純陣列，並統一轉換為字串類型
        const cleanArray = [...expandedIds].map(id => String(id));
        const jsonString = JSON.stringify(cleanArray);
        localStorage.setItem(STORAGE_KEY, jsonString);
        console.log('💾 儲存展開狀態到 localStorage:', {
            original: expandedIds,
            cleaned: cleanArray,
            jsonString: jsonString
        });
    } catch (error) {
        console.warn('❌ 無法儲存展開狀態到 localStorage:', error);
    }
};

// 7. 計算預設展開的類別
const getDefaultExpandedCategories = () => {
    console.log('🎯 開始計算預設展開的類別');
    console.log('📊 當前 props 狀態:', {
        categoriesCount: props.categories.length,
        itemsCount: props.items.length,
        categories: props.categories,
        items: props.items
    });
    
    // 先嘗試從 localStorage 讀取
    const savedState = loadExpandedStateFromStorage();
    console.log('📱 localStorage 中的狀態:', savedState);
    
    if (savedState.length > 0) {
        // 驗證儲存的類別 ID 是否仍然存在
        // 統一轉換為字串進行比較
        const validIds = savedState.filter(savedId => 
            props.categories.some(cat => String(cat.id) === String(savedId))
        );
        console.log('✅ 驗證後的有效 ID:', {
            savedIds: savedState,
            validIds: validIds,
            availableCategoryIds: props.categories.map(cat => String(cat.id))
        });
        
        if (validIds.length > 0) {
            console.log('🎉 使用 localStorage 中的展開狀態:', validIds);
            // 返回時轉換為對應的原始類型
            return validIds.map(id => {
                const category = props.categories.find(cat => String(cat.id) === String(id));
                return category ? category.id : id;
            });
        }
    }
    
    // 如果沒有儲存的狀態，或儲存的狀態無效，使用預設邏輯
    const sortedCategories = [...props.categories].sort((a, b) => (a.order || a.sort || 0) - (b.order || b.sort || 0));
    const defaultExpanded = [];
    
    console.log('📝 計算預設展開邏輯:');
    for (const category of sortedCategories) {
        // 檢查該類別是否有品項
        const hasItems = props.items.some(item => String(item.categoryId) === String(category.id));
        console.log(`   類別 "${category.name}" (ID: ${category.id}): ${hasItems ? '有' : '無'}品項`);
        if (hasItems) {
            defaultExpanded.push(category.id);
        }
    }
    
    console.log('🏁 最終預設展開的類別:', defaultExpanded);
    return defaultExpanded;
};


// 8. 內部狀態：展開的類別列表
const expandedCategories = ref([]);

// 9. 初始化展開狀態
const initializeExpandedState = () => {
    console.log('🚀 初始化展開狀態 - 開始');
    const newExpanded = getDefaultExpandedCategories();
    expandedCategories.value = newExpanded;
    console.log('🚀 初始化展開狀態 - 完成:', {
        oldValue: expandedCategories.value,
        newValue: newExpanded
    });
};

// 10. 方法：處理類別的展開/收合
const toggleCategory = (categoryId) => {
    console.log('🔄 切換類別展開狀態:', categoryId, '(類型:', typeof categoryId, ')');
    console.log('🔄 當前展開陣列:', expandedCategories.value.map(id => ({ id, type: typeof id })));
    
    // 統一轉換為字串進行比較
    const categoryIdStr = String(categoryId);
    const index = expandedCategories.value.findIndex(id => String(id) === categoryIdStr);
    const wasExpanded = index !== -1;
    
    console.log('🔍 查找結果:', { index, wasExpanded, categoryIdStr });
    
    if (index === -1) {
        // 如果不在陣列中，就加進去 (展開)
        expandedCategories.value.push(categoryId);
        console.log(`   ✅ 展開類別 ${categoryId}`);
    } else {
        // 如果在陣列中，就移除 (收合)
        expandedCategories.value.splice(index, 1);
        console.log(`   ❌ 收合類別 ${categoryId}`);
    }
    
    console.log('🔄 切換後的展開狀態:', expandedCategories.value);
};

// 11. 輔助函式，檢查某類別是否展開
const isCategoryExpanded = (categoryId) => {
    // 統一轉換為字串進行比較
    const isExpanded = expandedCategories.value.some(id => String(id) === String(categoryId));
    return isExpanded;
};

// 12. 全部展開/收合功能
const expandAll = () => {
    console.log('🌟 執行全部展開');
    // 展開所有類別
    const allCategoryIds = props.categories.map(cat => cat.id);
    expandedCategories.value = [...allCategoryIds];
    console.log('🌟 全部展開完成:', expandedCategories.value);
};

const collapseAll = () => {
    console.log('🌟 執行全部收合');
    // 收合所有類別
    expandedCategories.value = [];
    console.log('🌟 全部收合完成');
};

// 13. 檢查是否全部展開或全部收合
const isAllExpanded = computed(() => {
    if (props.categories.length === 0) return false;
    return props.categories.every(cat => 
        expandedCategories.value.some(id => String(id) === String(cat.id))
    );
});

const isAllCollapsed = computed(() => {
    return expandedCategories.value.length === 0;
});

// 14. 監聽展開狀態變化，自動儲存到 localStorage
watch(expandedCategories, (newValue, oldValue) => {
    console.log('👀 監聽到展開狀態變化:', {
        from: oldValue,
        to: newValue
    });
    saveExpandedStateToStorage(newValue);
}, { deep: true });

// 15. 監聽 props 變化，重新初始化展開狀態
watch([() => props.categories, () => props.items], ([newCategories, newItems], [oldCategories, oldItems]) => {
    console.log('👀 監聽到 props 變化:', {
        categories: {
            old: oldCategories?.length || 0,
            new: newCategories?.length || 0
        },
        items: {
            old: oldItems?.length || 0,
            new: newItems?.length || 0
        }
    });
    
    // 當類別或品項資料變化時，重新計算展開狀態
    // 但要確保資料已經載入完成，且避免重複初始化
    if (newCategories.length > 0 && expandedCategories.value.length === 0) {
        console.log('🔄 首次載入資料，進行初始化');
        initializeExpandedState();
    } else if (newCategories.length > 0 && oldCategories && oldCategories.length !== newCategories.length) {
        console.log('🔄 類別數量變化，重新初始化');
        initializeExpandedState();
    } else {
        console.log('⚠️ 跳過重新初始化 - 資料無顯著變化或已初始化');
    }
}, { immediate: false });

// 16. 組件掛載時初始化
onMounted(() => {
    console.log('🎬 組件掛載 - onMounted 觸發');
    console.log('📊 掛載時的 props:', {
        categoriesLength: props.categories.length,
        itemsLength: props.items.length
    });
    
    // 只有在資料已經載入時才初始化，避免重複初始化
    if (props.categories.length > 0) {
        console.log('✅ 資料已就緒，立即初始化');
        initializeExpandedState();
    } else {
        console.log('⚠️ 資料尚未載入，等待 watch 觸發初始化');
    }
});

// 17. 圖片載入邏輯
const { getImageUrl } = useImageUrl();

</script>

<template>
    <div>
        <!-- 頂部的新增按鈕和搜尋框 -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="input-group" style="max-width: 400px;">
                <span class="input-group-text">🔍</span>
                <input type="text" class="form-control" placeholder="搜尋品項">
            </div>

            <div class="d-flex gap-2">
                <!-- 全部展開/收合按鈕 -->
                <div class="btn-group" role="group" aria-label="展開控制">
                    <button 
                        type="button" 
                        class="btn btn-sm"
                        :class="{ disabled: isAllExpanded }"
                        @click="expandAll"
                        :disabled="isAllExpanded"
                        title="全部展開"
                    >
                        📖 全部展開
                    </button>
                    <button 
                        type="button" 
                        class="btn btn-sm"
                        :class="{ disabled: isAllCollapsed }"
                        @click="collapseAll"
                        :disabled="isAllCollapsed"
                        title="全部收合"
                    >
                        📕 全部收合
                    </button>
                </div>
                <!-- 新增品項按鈕 -->
                <button class="btn btn-warning" @click="emit('addNewItem')">+ 新增</button>
            </div>
        </div>

        <!-- 列表區域：現在我們要遍歷分好組的 groupedItems -->
        <div class="category-list-container">
            <div v-for="(group, categoryId) in groupedItems" :key="categoryId" class="list-group mb-3">

                <!-- 品項類別 Header -->
                <div class="list-group-item list-group-item-light fw-bold d-flex justify-content-between align-items-center" 
                    @click="toggleCategory(categoryId)" 
                    style="cursor: pointer;"
                >
                    <span>{{ group.name }} ({{ group.items.length }})</span>
                    <!-- 根據展開狀態顯示不同圖標 -->
                    <span class="fs-5">{{ isCategoryExpanded(categoryId) ? '▼' : '▲' }}</span>
                </div>

                <!-- 屬於該類別的品項列表，使用 v-show 來控制顯示/隱藏 -->
                <div 
                    v-show="isCategoryExpanded(categoryId)"
                    class="category-items-container"
                >
                    <!-- 如果該類別沒有品項，顯示提示訊息 -->
                    <div v-if="group.items.length === 0" class="list-group-item text-center text-muted ps-5 empty-state">
                        此類別目前沒有品項
                    </div>

                    <!-- 品項列表 -->
                    <div v-for="item in group.items" 
                        :key="item.id" 
                        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center ps-5 item-row" 
                        @click="emit('editItem', item)"
                        style="cursor: pointer;"
                    >
                        <div class="d-flex align-items-center">
                            <img 
                                :src="getImageUrl(item.imgResource)" 
                                :alt="item.name"
                                width="60" 
                                height="60" 
                                class="me-3 rounded item-image"
                                style="object-fit: cover;"
                            >
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

<style scoped>
/* === 基本元素樣式 === */
.list-group-item {
    transition: all 0.3s ease;
    border: 1px solid #dee2e6;
}

.list-group-item:hover {
    background-color: #f8f9fa;
    transform: translateX(2px);
}

/* === 類別標題樣式 === */
.list-group-item-light {
    background-color: #f8f9fa;
    border-bottom: 2px solid #dee2e6;
    font-weight: 600;
    cursor: pointer;
    user-select: none;
}

.list-group-item-light:hover {
    background-color: #e9ecef;
    transform: none;
}

/* === 展開/收合動畫 === */
.category-items-container {
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    animation: slideDown 0.4s ease-out;
}

@keyframes slideDown {
    from {
        opacity: 0;
        max-height: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        max-height: 1000px;
        transform: translateY(0);
    }
}

/* === 品項行樣式 === */
.item-row {
    background-color: #ffffff;
    margin-left: 0;
    padding: 0.75rem 1rem;
    transition: all 0.2s ease;
}

.item-row:hover {
    background-color: #fff3cd;
    box-shadow: inset 3px 0 0 #ffc107;
    transform: translateX(3px);
}

.item-row:last-child {
    border-bottom-left-radius: 0.375rem;
    border-bottom-right-radius: 0.375rem;
}

/* === 品項內容樣式 === */
.item-image {
    object-fit: cover;
    border: 2px solid #dee2e6;
    transition: border-color 0.2s ease;
}

.item-row:hover .item-image {
    border-color: #ffc107;
}

.item-name {
    font-weight: 500;
    color: #495057;
    transition: color 0.2s ease;
}

.item-row:hover .item-name {
    color: #212529;
}

.item-price {
    font-weight: 600;
    color: #28a745;
    font-size: 1.1em;
}

.item-status {
    transition: transform 0.2s ease;
}

.item-row:hover .item-status {
    transform: scale(1.05);
}

/* === 空狀態樣式 === */
.empty-state {
    background-color: #f8f9fa;
    font-style: italic;
    border-left: 4px solid #6c757d;
    animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}


/* === 滾動條樣式 === */
.category-list-container {
    max-height: calc(100vh - 380px);
    overflow-y: auto;
    padding-right: 4px;
    margin-right: -4px;
    padding-bottom: 30px; /* 適度的底部內邊距即可 */
}

/* 自定義滾動條 */
.category-list-container::-webkit-scrollbar {
    width: 8px;
}

.category-list-container::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.category-list-container::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

.category-list-container::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

.category-list-container::-webkit-scrollbar-thumb:active {
    background: #888;
}

/* Firefox 滾動條樣式 */
.category-list-container {
    scrollbar-width: thin;
    scrollbar-color: #c1c1c1 #f1f1f1;
}

/* === 全部展開/收合按鈕樣式 === */
.btn-group .btn {
    transition: all 0.2s ease;
}

.btn-group .btn:not(.disabled):hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn-group .btn.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* === 響應式調整 === */
@media (max-width: 768px) {
    .category-list-container {
        max-height: calc(100vh - 420px);
        padding-bottom: 40px; /* 手機上增加更多底部空間 */
    }
    
    .item-row {
        padding: 0.5rem;
    }
    
    .item-image {
        width: 50px;
        height: 50px;
    }
    
    .btn-group .btn {
        font-size: 0.8rem;
        padding: 0.25rem 0.5rem;
    }
}

/* === 小視窗特別處理 === */
@media (max-height: 700px) {
    .category-list-container {
        max-height: calc(100vh - 360px);
        padding-bottom: 50px;
    }
}

@media (max-height: 600px) {
    .category-list-container {
        max-height: calc(100vh - 340px);
        padding-bottom: 60px;
    }
}

/* === 極小視窗處理 === */
@media (max-height: 500px) {
    .category-list-container {
        max-height: calc(100vh - 320px);
        padding-bottom: 70px;
    }
}

/* === 載入狀態動畫 === */
.list-group {
    animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* === 互動狀態增強 === */
.list-group-item-light:active {
    transform: scale(0.98);
}

.item-row:active {
    transform: scale(0.98) translateX(3px);
}

/* === 底部空白區域，確保滾動體驗 === */
.scroll-bottom-spacer {
    height: 60px;
    flex-shrink: 0;
}

/* === 額外的滾動優化 === */
.category-list-container {
    /* 確保容器能夠正確計算內容高度 */
    contain: layout style;
    will-change: scroll-position;
}

</style>