<script setup>
import { ref, reactive, onMounted, watch } from 'vue'; // 導入 onMounted 、watch函數
import apiClient from '../plungins/axios.js'; // 導入 apiClient
// import { apiService } from '../services/apiService.js'; // 導入 API 服務
import PageHeader from '../components/common/PageHeader.vue';
import CustomizationSpecs from '../components/menu/CustomizationSpecs.vue';
import EditItemModal from '../components/menu/EditItemModal.vue';
import EditSpecModal from '../components/menu/EditSpecModal.vue';
import MenuOverview from '../components/menu/MenuOverview.vue';

// --- 響應式狀態 (State) ---

// =================================================================
// 1. 組件核心狀態 (Component Core State)
// =================================================================

// 當前活動的 Tab
const activeTab = ref('overview'); // 'overview' 或 'specs'

// 模擬的商店資料
const stores = ref([
    { id: 1, name: '美味小館' },
    { id: 3, name: '燒烤之家' },
]);
const selectedStore = ref(stores.value[0]?.id || null); // 預設選中第一個店家的 ID


// =================================================================
// 2. 資料源 (Data Sources) - 從後端獲取的資料
// =================================================================

// 新增 categories 陣列
const categories = reactive([]);


const items = reactive([]);


const specs = reactive([]);

const isLoading = ref(false); // 新增一個加載狀態，用於顯示讀取中的提示

const error = ref(null); // 新增一個錯誤狀態


// =================================================================
// 3. 【全新】獲取資料的核心函式
// =================================================================

const fetchMenuData = async (storeId) => {
    if (!storeId) {
        // 如果沒有 storeId，清空列表並返回
        categories.splice(0);
        items.splice(0);
        return;
    }

    try {
        isLoading.value = true;
        error.value = null;

        console.log(`🚀 正在為店家 ID: ${storeId} 獲取菜單資料...`);
        // 使用 Promise.all 來並行發送所有請求，效率更高
        const [categoriesResponse, itemsResponse] = await Promise.all([
            apiClient.get(`/api/food-classes/store/${storeId}`),
            apiClient.get(`/api/foods/store/${storeId}`),
            // apiClient.get(`/specs/store/${storeId}`), // 未來可以加上規格的 API
        ]);

        // 【修改】使用 .splice(0) 和解構賦值來安全地更新 reactive 陣列
        categories.splice(0, categories.length, ...categoriesResponse.data);
        items.splice(0, items.length, ...itemsResponse.data);
        // specs.splice(0, specs.length, ...specsResponse.data);

        console.log('✅ 成功獲取分類:', categories);
        console.log('✅ 成功獲取品項:', items);

    } catch (e) {
        console.error(`❌ 獲取店家 ID:${storeId} 的資料失敗:`, e);
        error.value = '無法載入菜單資料，請稍後再試。';
        // 發生錯誤時清空資料
        categories.splice(0);
        items.splice(0);
    } finally {
        isLoading.value = false;
    }
};


// =================================================================
// 4. 生命週期鉤子和監聽器
// =================================================================
onMounted(() => {
    fetchMenuData(selectedStore.value);
});

// 【新增】監聽 selectedStore 的變化，當使用者切換店家時，重新獲取資料
watch(selectedStore, (newStoreId) => {
    fetchMenuData(newStoreId);
});


// =================================================================
// 5. 品項管理相關 (Item Management)
// =================================================================

const isItemModalOpen = ref(false); // 控制編輯品項 Modal 的開關

const currentEditingItem = ref(null); // 正在編輯的品項，null 代表是新增

const openItemModal = (item) => {
    currentEditingItem.value = item ? { ...item } : null;
    isItemModalOpen.value = true;
};

const closeItemModal = () => {
    isItemModalOpen.value = false;
};

const handleSaveItem = async (itemData) => {
    isLoading.value = true;
    try {
        let payload; // 先宣告一個 payload 變數

        // 判斷是新增還是編輯
        if (itemData.id) {
            // 【編輯模式】
            // 直接使用 itemData 作為 payload 的基礎
            payload = { ...itemData };
            
            // 將 categoryId 轉換為後端需要的 foodClassIds 陣列
            if (payload.categoryId) {
                payload.foodClassIds = [payload.categoryId];
            } else {
                payload.foodClassIds = []; // 如果沒有選擇分類，就送一個空陣列
            }
            delete payload.categoryId; // 移除掉後端不需要的 categoryId，保持 payload 乾淨

            console.log("準備發送 PUT 請求的 payload:", payload);
            await apiClient.put(`/api/foods/${itemData.id}`, payload);
            alert('品項更新成功！');

        } else {
            // 【新增模式】
            // 為 payload 加上 storeId
            payload = { ...itemData, storeId: selectedStore.value };

            // 同樣，將 categoryId 轉換為 foodClassIds 陣列
            if (payload.categoryId) {
                payload.foodClassIds = [payload.categoryId];
            } else {
                payload.foodClassIds = [];
            }
            delete payload.categoryId;

            console.log("準備發送 POST 請求的 payload:", payload);
            await apiClient.post('/api/foods', payload);
            alert('品項新增成功！');
        }
        
        // 操作成功後，重新獲取列表
        await fetchMenuData(selectedStore.value);

    } catch (e) {
        console.error('儲存品項失敗:', e);
        alert(`儲存失敗：${e.response?.data?.message || e.message}`);
    } finally {
        isLoading.value = false;
        closeItemModal();
    }
};

const handleDeleteItem = async (itemId) => {
    if (confirm('確定要刪除此品項嗎？')) {
        isLoading.value = true;
        try {
            // 【修改】刪除：呼叫 DELETE API
            await apiClient.delete(`/api/foods/${itemId}`);
            alert('刪除成功！');
            await fetchMenuData(selectedStore.value); // 重新獲取列表
        } catch (e) {
            console.error('刪除品項失敗:', e);
            alert(`刪除失敗：${e.response?.data?.message || e.message}`);
        } finally {
            isLoading.value = false;
            closeItemModal();
        }
    }
};

// =================================================================
// 5. 規格管理相關 (Specification Management) (待修改)
// =================================================================

// 控制編輯規格 Modal 的開關
const isSpecModalOpen = ref(false);

// 正在編輯的規格，null 代表是新增
const currentEditingSpec = ref(null);

const openSpecModal = (spec) => {
    console.log('打開規格 Modal，編輯的資料是:', spec);
    currentEditingSpec.value = spec ? { ...spec } : null;
    isSpecModalOpen.value = true;
};

const closeSpecModal = () => {
    isSpecModalOpen.value = false;
}

const handleSaveSpec = (specData) => {
    console.log('儲存規格:', specData);
    alert('規格已儲存！');
    closeSpecModal();
}

const handleDeleteSpec = (specId) => {
    if (confirm('確定要刪除此規格嗎？')) {
        alert('規格已刪除！');
        closeSpecModal();
    }
}

// =================================================================
// 6. 通用方法 (General Methods)
// =================================================================

// 切換 Tab
const selectTab = (tab) => {
    activeTab.value = tab;
};

</script>

<template>
    <div>
        <!-- 使用新的公版 PageHeader 組件 -->
        <PageHeader title="菜單管理">
            <!-- 這是要 "塞" 進插槽的內容 -->
            <template #actions>
                <select class="form-select" v-model="selectedStore" style="width: auto; min-width: 180px;">  <!-- 切換店鋪下拉選單 -->
                    <option v-for="store in stores" :key="store.id" :value="store.id">
                        {{ store.name }}
                    </option>
                </select>
            </template>
        </PageHeader>

        <!-- 在列表區域的外面，加上 loading 和 error 的判斷 -->
        <div v-if="isLoading" class="text-center p-5">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">資料載入中...</p>
        </div>

        <div v-else-if="error" class="alert alert-danger">
            {{ error }}
        </div>

        <div v-else>
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link" :class="{ active: activeTab === 'overview' }" href="#"
                        @click.prevent="selectTab('overview')">菜單總覽</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" :class="{ active: activeTab === 'specs' }" href="#"
                        @click.prevent="selectTab('specs')">客製化規格</a>
                </li>
            </ul>

            <div class="mt-4">
                <MenuOverview v-if="activeTab === 'overview'" :items="items" :categories="categories"
                    @add-new-item="openItemModal(null)" @edit-item="openItemModal" />

                <CustomizationSpecs v-if="activeTab === 'specs'" :specs="specs" @add-new-spec="openSpecModal(null)"
                    @edit-spec="openSpecModal" />
            </div>
        </div>

        <!-- Modals (不受佈局影響，已套用共用面板SlideOutPanel) -->
        <SlideOutPanel v-model:isOpen="isItemModalOpen"
            :title="currentEditingItem ? '編輯品項' : '新增品項'">
        
        <!-- 
        只有在 isItemModalOpen 為 true (面板打開) 時，才渲染 EditItemModal。
        這樣可以確保每次打開面板時，EditItemModal 都會重新掛載，
        其內部的 watchEffect 會重新執行，正確地初始化表單資料。
        -->
            <EditItemModal v-if="isItemModalOpen" 
                :item="currentEditingItem" 
                :categories="categories"
                @close="isItemModalOpen = false" 
                @save="handleSaveItem"
                @delete="handleDeleteItem" 
            />
        </SlideOutPanel>

        
        <SlideOutPanel v-model:isOpen="isSpecModalOpen"
            :title="currentEditingSpec ? '編輯客製化規格' : '新增客製化規格'">
        
        <!-- 同樣只在 isSpecModalOpen 為 true 時，才渲染 EditSpecModal。-->

            <EditSpecModal v-if="isSpecModalOpen" 
                :spec="currentEditingSpec" 
                @close="isSpecModalOpen = false" 
                @save="handleSaveSpec"
                @delete="handleDeleteSpec" 
            />
        </SlideOutPanel>
    </div>
</template>

<style scoped>
/* 已搬到共用樣式 SellerLayout.vue */
</style>