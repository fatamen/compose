<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed } from 'vue';
import SlideOutPanel from '../components/common/SlideOutPanel.vue';
import apiClient from '../plungins/axios.js';
// import { uploadImage } from '../plungins/firebase-storage.js' // 導入 firebase
import PageHeader from '../components/common/PageHeader.vue';
// import CustomizationSpecs from '../components/menu/CustomizationSpecs.vue';  //預定捨棄功能
import EditItemPanel from '../components/menu/EditItemPanel.vue';
import CategoryManagement from '../components/menu/CategoryManagement.vue';
import EditCategoryPanel from '../components/menu/EditCategoryPanel.vue'; 
// import EditSpecModal from '../components/menu/EditSpecModal.vue';  //預定捨棄功能
import MenuOverview from '../components/menu/MenuOverview.vue';
import { useStore } from '../composables/useStore.js'; // 🔥 NEW: 導入 useStore
import { uploadImageGeneric } from '../plungins/imageUpload.js'

// 🔥 NEW: 使用共享的 store 狀態
const { selectedStore, currentStoreName, isLoggedIn } = useStore()

// --- 響應式狀態 (State) ---

// =================================================================
// 1. 組件核心狀與登入/登出監聽
// =================================================================

// 當前活動的 Tab
const activeTab = ref('overview'); // 'overview', 'categories' 或 'specs'

// 🔥 添加全域事件監聽，確保登入/登出時正確更新
onMounted(async () => {
    console.log('🎬 [MenuManagement] 組件掛載')
    
    // 監聽用戶登入事件
    const handleUserLoggedIn = async (event) => {
        console.log('👤 [MenuManagement] 收到用戶登入事件:', event.detail)
        // 延遲一點讓 useStore 先更新
        setTimeout(async () => {
            if (selectedStore.value) {
                console.log('🔄 [MenuManagement] 重新載入菜單資料')
                await fetchMenuData(selectedStore.value)
            }
        }, 500)
    }
    
    // 監聽用戶登出事件
    const handleUserLoggedOut = () => {
        console.log('👤 [MenuManagement] 收到用戶登出事件')
        // 清除菜單資料
        categories.splice(0)
        items.splice(0)
    }
    
    // 註冊事件監聽器
    window.addEventListener('userLoggedIn', handleUserLoggedIn)
    window.addEventListener('userLoggedOut', handleUserLoggedOut)
    
    // 如果已經有選中的店家，立即載入
    if (selectedStore.value) {
        await fetchMenuData(selectedStore.value)
    }
    
    // 🔥 在 onBeforeUnmount 中移除事件監聽器
    onBeforeUnmount(() => {
        console.log('🧹 [MenuManagement] 組件卸載')
        window.removeEventListener('userLoggedIn', handleUserLoggedIn)
        window.removeEventListener('userLoggedOut', handleUserLoggedOut)
    })
})


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
// 3. 【簡化】獲取資料的核心函式
// =================================================================

const fetchMenuData = async (storeId) => {
    if (!storeId) {
        categories.splice(0);
        items.splice(0);
        return;
    }

    try {
        isLoading.value = true;
        error.value = null;

        console.log(`🚀 [MenuManagement] 正在為店家 ID: ${storeId} 獲取菜單資料...`);
        
        // 🔥 NEW: 先測試店家是否存在
        console.log('📡 測試店家存在性...');
        try {
            const storeResponse = await apiClient.get(`/api/stores/profile?ownerId=1&storeId=${storeId}`);
            console.log('✅ 店家資料:', storeResponse.data);
        } catch (storeError) {
            console.warn('⚠️ 無法驗證店家存在性:', storeError);
        }
        
        const [categoriesResponse, itemsResponse] = await Promise.all([
            apiClient.get(`/api/food-classes/store/${storeId}`),
            apiClient.get(`/api/foods/store/${storeId}`),
        ]);

        console.log('✅ [MenuManagement] API 回應結果:');
        console.log('   分類回應:', categoriesResponse.data);
        console.log('   品項回應:', itemsResponse.data);
        
        // 🔥 NEW: 檢查回應狀態
        console.log('📊 API 回應狀態:', {
            categoriesStatus: categoriesResponse.status,
            itemsStatus: itemsResponse.status,
            categoriesLength: categoriesResponse.data.length,
            itemsLength: itemsResponse.data.length
        });

        categories.splice(0, categories.length, ...categoriesResponse.data);
        
        const formattedItems = itemsResponse.data.map(item => {
            // 通過 categoryName 找到對應的 categoryId
            let categoryId = null;
            if (item.categoryName) {
                const matchedCategory = categoriesResponse.data.find(cat => 
                    cat.name === item.categoryName
                );
                if (matchedCategory) {
                    categoryId = matchedCategory.id;
                    console.log(`   📋 [MenuManagement] 找到分類匹配: "${item.categoryName}" → ID: ${categoryId}`);
                } else {
                    console.warn(`   ⚠️ [MenuManagement] 找不到分類: "${item.categoryName}"`);
                }
            } else if (item.foodClassIds && item.foodClassIds.length > 0) {
                categoryId = item.foodClassIds[0];
                console.log(`   📋 [MenuManagement] 使用 foodClassIds: ${categoryId}`);
            }
            
            return {
                ...item,
                status: item.isActive ? '供應中' : '停售',
                categoryId: categoryId,
                imgResource: item.imgResource || item.imageUrl || item.image || '',
                imageUrl: item.imgResource || item.imageUrl || item.image || ''
            };
        });
        
        console.log('✅ [MenuManagement] 格式化後的 items:', formattedItems);
        items.splice(0, items.length, ...formattedItems);

        console.log('📊 [MenuManagement] 最終載入結果:');
        console.log(`   分類數量: ${categories.length}`);
        console.log(`   品項數量: ${items.length}`);

        // 🔥 NEW: 如果沒有資料，提供更多資訊
        if (categories.length === 0 && items.length === 0) {
            console.warn(`⚠️ 店家 ${storeId} 沒有任何菜單資料`);
            console.warn('💡 請檢查：');
            console.warn('   1. 店家 ID 是否正確？');
            console.warn('   2. 該店家是否有設定分類和品項？');
            console.warn('   3. API 路徑是否正確？');
        }

    } catch (e) {
        console.error(`❌ [MenuManagement] 獲取店家 ID:${storeId} 的資料失敗:`, e);
        console.error('   錯誤詳情:', {
            message: e.message,
            status: e.response?.status,
            statusText: e.response?.statusText,
            data: e.response?.data
        });
        error.value = `無法載入菜單資料：${e.response?.data?.message || e.message}`;
        categories.splice(0);
        items.splice(0);
    } finally {
        isLoading.value = false;
    }
};


// =================================================================
// 4. 監聽 store 變化
// =================================================================

// 🔥 NEW: 監聽 selectedStore 變化
watch(selectedStore, async (newStoreId, oldStoreId) => {
    console.log(`👀 [MenuManagement] selectedStore 變化: ${oldStoreId} → ${newStoreId}`)

    // 🔥 添加更詳細的除錯資訊
    const currentOwnerId = localStorage.getItem('ownerId')
    console.log(`🔍 [MenuManagement] 當前 ownerId: ${currentOwnerId}`)
    
    if (newStoreId && newStoreId !== oldStoreId) {
        console.log(`🚀 [MenuManagement] 準備載入店家 ${newStoreId} 的資料`)
        await fetchMenuData(newStoreId)
    }else if (!newStoreId) {
        console.log('🧹 [MenuManagement] selectedStore 為空，清除菜單資料')
        categories.splice(0)
        items.splice(0)
        }
}, { immediate: true })

onMounted(async () => {
    console.log('🎬 [MenuManagement] 組件掛載')
    
    if (selectedStore.value) {
        await fetchMenuData(selectedStore.value)
    }
});

onBeforeUnmount(() => {
    console.log('🧹 [MenuManagement] 組件卸載')
    // window.removeEventListener('storeChanged', handleStoreChanged)
})


// =================================================================
// 5. 品項管理相關 (Item Management)
// =================================================================

const isItemPanelOpen = ref(false); // 控制編輯品項 Modal 的開關

const currentEditingItem = ref(null); // 正在編輯的品項，null 代表是新增

const openItemPanel = (item) => {
    currentEditingItem.value = item ? { ...item } : null;
    isItemPanelOpen.value = true;
};

const closeItemPanel = () => {
    isItemPanelOpen.value = false;
};

const handleSaveItem = async (itemData) => {
    isLoading.value = true;
    try {
        let payload = { ...itemData }; // 先宣告一個 payload 變數

        // 🔥 新增：處理圖片上傳
        if (itemData.imageFile) {
            console.log('開始上傳圖片到本地伺服器...');
            try {
                const relativePath = await uploadImageGeneric(itemData.imageFile);
                console.log('圖片上傳成功，路徑:', relativePath);
                
                // 儲存相對路徑到 payload
                itemData.imgResource = relativePath; // 例如："images/temp_1234567890.jpg"
                console.log('設定 imgResource 到 itemData:', itemData.imgResource);
                
            } catch (uploadError) {
                console.error('圖片上傳失敗:', uploadError);
                alert('圖片上傳失敗，請重試');
                return;
            }
        }

        // 🔥 重要：圖片上傳完成後，重新建立 payload
        payload = { ...itemData };

        // 🔥 新增：處理圖片刪除
        if (itemData.deleteExistingImage) {
            console.log('使用者刪除了既有圖片');
            payload.imgResource = ''; // 清空圖片 URL
            // 注意：這裡可以選擇是否要從 Firebase 刪除舊圖片
            // 目前先不刪除，避免複雜化
        }

        // 判斷是新增還是編輯
        if (itemData.id) {
            console.log('=== 編輯模式 Debug ===');
            console.log('原始 itemData.imgResource:', itemData.imgResource);
            console.log('payload.imgResource:', payload.imgResource);

            // 🔥 新增：處理狀態轉換
            console.log('轉換前 payload.status:', payload.status);
            payload.isActive = payload.status === '供應中';
            console.log('轉換後 payload.isActive:', payload.isActive);

            // 將 categoryId 轉換為後端需要的 foodClassIds 陣列
            if (payload.categoryId) {
                payload.foodClassIds = [payload.categoryId];
            } else {
                payload.foodClassIds = []; // 如果沒有選擇分類，就送一個空陣列
            }
            delete payload.categoryId; // 移除掉後端不需要的 categoryId，保持 payload 乾淨

            // 🔥 確認圖片路徑沒有被清理掉
            console.log('清理前 payload.imgResource:', payload.imgResource);

            

            // // 🔥 新增：處理圖片欄位轉換
            // if (payload.imageUrl && !payload.imgResource) {
            //     payload.imgResource = payload.imageUrl;
            // }

            // 🔥 新增：清理不需要的欄位
            delete payload.categoryId;
            delete payload.imageFile;
            delete payload.deleteExistingImage;
            delete payload.imageUrl;  // ← 編輯模式也要清除
            delete payload.status;    // ← 新增：清除前端用的 status

            console.log('清理後 payload.imgResource:', payload.imgResource);
            console.log("準備發送 PUT 請求的 payload:", payload);
            const response = await apiClient.put(`/api/foods/${itemData.id}`, payload);

            // 🔥 新增：檢查 API 回應
            console.log("API 回應狀態:", response.status);
            console.log("API 回應資料:", response.data);

            alert('品項更新成功！');

        } else {
            // 【新增模式】
            // 為 payload 加上 storeId
            payload = { ...itemData, storeId: selectedStore.value };

            // 🔥 DEBUG: 加在這裡 - 新增模式的狀態檢查
            console.log('=== 新增模式 Debug ===');
            console.log('原始 itemData.status:', itemData.status);
            console.log('payload.status:', payload.status);

            // 同樣，將 categoryId 轉換為 foodClassIds 陣列
            if (payload.categoryId) {
                payload.foodClassIds = [payload.categoryId];
            } else {
                payload.foodClassIds = [];
            }
            delete payload.categoryId; // 移除掉後端不需要的 categoryId，保持 payload 乾淨

            // 🔥 新增：處理狀態轉換
            console.log('轉換前 payload.status:', payload.status);
            payload.isActive = payload.status === '供應中';
            console.log('轉換後 payload.isActive:', payload.isActive);
            
            // 🔥 新增：處理圖片欄位轉換
            if (payload.imageUrl && !payload.imgResource) {
                payload.imgResource = payload.imageUrl;
            }

            // 🔥 新增：清理不需要的欄位
            delete payload.categoryId;
            delete payload.imageFile; // 移除 imageFile，只保留 imageUrl
            delete payload.deleteExistingImage; // 移除刪除標記
            delete payload.imageUrl; //  清除前端用的 imageUrl
            delete payload.status;    // ← 新增：清除前端用的 status

            console.log("準備發送 POST 請求的 payload:", payload);
            await apiClient.post('/api/foods', payload);
            alert('品項新增成功！');
        }
        
        // 🔥 NEW: 使用當前選中的 store 重新載入
        await fetchMenuData(selectedStore.value);

    } catch (e) {
        console.error('儲存品項失敗:', e);
        alert(`儲存失敗：${e.response?.data?.message || e.message}`);
    } finally {
        isLoading.value = false;
        closeItemPanel();
    }
};

const handleDeleteItem = async (itemId) => {
    if (confirm('確定要刪除此品項嗎？')) {
        isLoading.value = true;
        try {
            // 【修改】刪除：呼叫 DELETE API
            await apiClient.delete(`/api/foods/${itemId}`);
            alert('刪除成功！');
            await fetchMenuData(selectedStore.value); // 🔥 NEW: 使用當前選中的 store
        } catch (e) {
            console.error('刪除品項失敗:', e);
            alert(`刪除失敗：${e.response?.data?.message || e.message}`);
        } finally {
            isLoading.value = false;
            closeItemPanel();
        }
    }
};

// =================================================================
// 6. 品項類別管理相關 (Category Management)
// =================================================================

// 控制編輯品項類別 Modal 的開關
const isCategoryPanelOpen = ref(false);

// 正在編輯的品項類別，null 代表是新增
const currentEditingCategory = ref(null);

const openCategoryPanel = (category) => {
    console.log('打開品項類別 Modal，編輯的資料是:', category);
    if (category) {
        // 編輯模式：直接複製資料
        currentEditingCategory.value = { ...category };
    } else {
        // 新增模式：計算新的 sort 值
        // 1. 找出當前最大的 sort 值
        const maxSort = categories.length > 0 
            ? Math.max(...categories.map(c => c.sort || 0)) 
            : 0;
        //categories.map(c => c.sort || 0): 遍歷所有類別，取出它們的 sort 值，如果某個類別的 sort 是 null 或 undefined，就當作 0。
        //這會得到一個像 [1, 3, 2] 這樣的數字陣列。
        //Math.max(...): ... 是展開運算符，它會把 [1, 3, 2] 展開成 Math.max(1, 3, 2)，這個函式會返回陣列中的最大值（3）。
        
        // 2. 建立一個包含預設 sort 值的新物件
        currentEditingCategory.value = {
            name: '',
            description: '',
            sort: maxSort + 1, // 新的 sort 值
        };
    }
    isCategoryPanelOpen.value = true;
};

const maxCategorySort = computed(() => {
    if (currentEditingCategory.value?.id) { // 編輯模式
        return categories.length;
    } else { // 新增模式
        return categories.length + 1;
    }

});

const closeCategoryPanel = () => {
    isCategoryPanelOpen.value = false;
}

const handleSaveCategory = async (categoryData) => {
    isLoading.value = true;
    try {
        if (categoryData.id) {
            // 編輯模式
            await apiClient.put(`/api/food-classes/${categoryData.id}`, categoryData);
            alert('類別更新成功！');
        } else {
            // 新增模式，記得加上 storeId
            const payload = { ...categoryData, storeId: selectedStore.value }; // 🔥 NEW
            await apiClient.post('/api/food-classes', payload);
            alert('類別新增成功！');
        }
        // 成功後，只重新獲取 categories 列表即可
        const response = await apiClient.get(`/api/food-classes/store/${selectedStore.value}`); // 🔥 NEW
        categories.splice(0, categories.length, ...response.data);
    } catch (e) {
        console.error('儲存品項類別失敗:', e);
        alert(`儲存失敗：${e.response?.data?.message || e.message}`);
    } finally {
        isLoading.value = false;
        closeCategoryPanel(); // 使用您已有的關閉函式
    }
};

const handleDeleteCategory = async (categoryId) => {
    if (confirm('確定要刪除此品項類別嗎？\n刪除後，屬於此類別的品項將變為「未分類」。')) {
        isLoading.value = true;
        try {
            await apiClient.delete(`/api/food-classes/${categoryId}`);
            alert('刪除成功！');
            const response = await apiClient.get(`/api/food-classes/store/${selectedStore.value}`);// 🔥 NEW
            categories.splice(0, categories.length, ...response.data);
        } catch (e) {
            console.error('刪除品項類別失敗:', e);
            alert(`刪除失敗：${e.response?.data?.message || e.message}`);
        } finally {
            isLoading.value = false;
            closeCategoryPanel();
        }
    }
};
const handleUpdateCategoryOrder = (updatedCategories) => {
    // 更新本地資料
    categories.splice(0, categories.length, ...updatedCategories);
    
    // 如果需要，可以在這裡呼叫 API 儲存新的排序
    // saveCategoryOrder(updatedCategories);
};

// =================================================================
// 6. 規格管理相關 (Specification Management) (來不及做)
// =================================================================

// 控制編輯規格 Modal 的開關
// const isSpecModalOpen = ref(false);

// 正在編輯的規格，null 代表是新增
// const currentEditingSpec = ref(null);

// const openSpecModal = (spec) => {
//     console.log('打開規格 Modal，編輯的資料是:', spec);
//     currentEditingSpec.value = spec ? { ...spec } : null;
//     isSpecModalOpen.value = true;
// };

// const closeSpecModal = () => {
//     isSpecModalOpen.value = false;
// }

// const handleSaveSpec = (specData) => {
//     console.log('儲存規格:', specData);
//     alert('規格已儲存！');
//     closeSpecModal();
// }

// const handleDeleteSpec = (specId) => {
//     if (confirm('確定要刪除此規格嗎？')) {
//         alert('規格已刪除！');
//         closeSpecModal();
//     }
// }

// =================================================================
// 7. 通用方法 (General Methods)
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
                <!-- 🔥 NEW: 簡化的店家顯示 -->
                <div v-if="currentStoreName" class="badge bg-primary fs-6">
                    🏪 {{ currentStoreName }}
                </div>
                <div v-else-if="!isLoggedIn" class="badge bg-warning fs-6">
                    ⚠️ 未登入
                </div>
                <div v-else class="badge bg-secondary fs-6">
                    📍 請選擇店家
                </div>
            </template>
        </PageHeader>

        <!-- 沒有選中店家時的提示 -->
        <div v-if="!selectedStore && !isLoading" class="alert alert-info">
            <h5>📋 請選擇店家</h5>
            <p class="mb-0">
                請在左側邊欄選擇要管理的店家，然後就能編輯該店家的菜單資料。
            </p>
        </div>
        
        <!-- 載入中狀態 -->
        <div v-else-if="isLoading" class="text-center p-5">
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
                    <a class="nav-link" :class="{ active: activeTab === 'categories' }" href="#"
                        @click.prevent="selectTab('categories')">品項類別管理</a>
                </li>
                <!-- <li class="nav-item">
                    <a class="nav-link" :class="{ active: activeTab === 'specs' }" href="#"
                        @click.prevent="selectTab('specs')">客製化規格</a>
                </li> -->
            </ul>

            <div class="mt-4">
                <MenuOverview v-if="activeTab === 'overview'" 
                    :items="items" 
                    :categories="categories"
                    @add-new-item="openItemPanel(null)" 
                    @edit-item="openItemPanel" />

                <CategoryManagement v-if="activeTab === 'categories'"
                    :categories="categories"
                    @addNewCategory="openCategoryPanel(null)"
                    @editCategory="openCategoryPanel"
                    @updateCategoryOrder="handleUpdateCategoryOrder"
                />

                <!-- <CustomizationSpecs v-if="activeTab === 'specs'" 
                    :specs="specs" 
                    @add-new-spec="openSpecModal(null)"
                    @edit-spec="openSpecModal" 
                /> --><!-- 客製化規格暫停開發 -->
            </div>
        </div>


        <!-- Modals (不受佈局影響，已套用共用面板SlideOutPanel) -->
        <SlideOutPanel v-model:isOpen="isItemPanelOpen"
            :title="currentEditingItem ? '編輯品項' : '新增品項'">
        
        <!-- 
        只有在 isItemPanelOpen 為 true (面板打開) 時，才渲染 EditItemPanel。
        這樣可以確保每次打開面板時，EditItemPanel 都會重新掛載，
        其內部的 watchEffect 會重新執行，正確地初始化表單資料。
        -->
            <EditItemPanel v-if="isItemPanelOpen" 
                :item="currentEditingItem" 
                :categories="categories"
                @close="isItemPanelOpen = false" 
                @save="handleSaveItem"
                @delete="handleDeleteItem" 
            />
        </SlideOutPanel>


        <SlideOutPanel v-model:isOpen="isCategoryPanelOpen"
            :title="currentEditingCategory ? '編輯品項類別' : '新增品項類別'"
        >
        
            <EditCategoryPanel v-if="isCategoryPanelOpen" 
                :category="currentEditingCategory" 
                :max-sort="maxCategorySort" 
                @close="closeCategoryPanel" 
                @save="handleSaveCategory"
                @delete="handleDeleteCategory" 
            />
        </SlideOutPanel>

        
        <!-- <SlideOutPanel v-model:isOpen="isSpecModalOpen"
            :title="currentEditingSpec ? '編輯客製化規格' : '新增客製化規格'"> -->
        
        <!-- 同樣只在 isSpecModalOpen 為 true 時，才渲染 EditSpecModal。-->

            <!-- <EditSpecModal v-if="isSpecModalOpen" 
                :spec="currentEditingSpec" 
                @close="isSpecModalOpen = false" 
                @save="handleSaveSpec"
                @delete="handleDeleteSpec" 
            />
        </SlideOutPanel> -->
    </div>
</template>

<style scoped>
/* 大部分已搬到共用樣式 SellerLayout.vue 處理 */
.badge {
    font-size: 0.85rem !important;
}
</style>