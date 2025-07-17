<template> <!--點選餐廳字卡後出現 往下是RestaurantTemplate-->
    <div v-if="isLoading" class="text-center p-5">
        <div class="spinner-border" role="status"></div>
        <p class="mt-2">店家資訊載入中...</p>
    </div>
    <div v-else-if="error" class="alert alert-danger m-3">{{ error }}</div>
    <div v-else-if="restaurantData">
        <!-- 把從 API 獲取的、真實的 restaurantData 傳遞下去 -->
        <RestaurantTemplate :restaurant="restaurantData" />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '../../plungins/axios.js'; // 確保路徑正確
import RestaurantTemplate from "@/components/KTlu/RestaurantTemplate.vue";
import { useRoute } from 'vue-router';
const route = useRoute();

// 【核心修改 1】接收一個名為 'id' 的 prop


const restaurantData = ref(null);
const isLoading = ref(true);
const error = ref(null);

// 【核心修改 2】在 onMounted 中，根據 id 獲取店家資料基本資料
onMounted(async () => {
    try {
        // 使用我們統一的 /api/stores 路徑
         
        const response = await apiClient.get(`/api/stores/${route.params.id}`);
        restaurantData.value = response.data;
    } catch (e) {
        console.error("獲取店家資料失敗:", e);
        console.log()
        error.value = "抱歉，找不到該店家或發生錯誤。";
    } finally {
        isLoading.value = false;
    }
});

</script>



<!-- vue-cus/
├── src/
│   ├── components/
│   │   └── KTlu/              # 您的組件目錄
│   │       ├── RestaurantTemplate.vue
│   │       ├── ReservationForm.vue
│   │       ├── RestaurantBanner.vue
│   │       ├── RestaurantInfo.vue
│   │       ├── RestaurantMenu.vue
│   │       ├── RestaurantMap.vue
│   │       ├── RestaurantFooter.vue
│   │       ├── TimePickerSectioned.vue
│   │       ├── ItemDetailModal.vue
│   │       └── CartModal.vue
│   ├── data/                  # 資料目錄
│   │   ├── restaurants.js
│   │   └── timeslots_30d.json
│   ├── services/              # 服務目錄
│   │   └── timeSlotService.js
│   ├── utils/                 # 工具函數目錄
│   │   └── timeSlotUtils.js
│   └── restaurant-index.js    # 原index.js的備份 -->
