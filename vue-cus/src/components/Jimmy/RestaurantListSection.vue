<template>
  <section class="restaurant-list">
    <div class="restaurant-card" v-for="restaurant in restaurants" :key="restaurant.id">
      <div class="image-container">
        <img :src="restaurant.photo " :alt="restaurant.name"
          @click="navigateToRestaurant(restaurant.id)" style="cursor: pointer;" />
        <i class="favorite-icon bi"
          :class="{ 'bi-heart-fill': restaurant.isFavorited, 'bi-heart': !restaurant.isFavorited }"
          @click.stop="toggleFavorite(restaurant)">
        </i>
      </div>
      <div class="info">
        <h3>{{ restaurant.name }}</h3>
        <p>
          {{ restaurant.categoryNames && restaurant.categoryNames.length > 0 ? restaurant.categoryNames.join(' / ') : '無類別' }}
          •
          {{ restaurant.isOpen ? '營業中' : '休息中' }}
        </p>
        <p class="score-group"> <span class="score-text">
            {{ restaurant.score }}★
          </span>
        </p>
        <div class="comment-distance-group"> <span class="comment-trigger-text" @click="openComment(restaurant.id)" style="cursor: pointer;">
            ({{ restaurant.comments ? restaurant.comments.length : 0 }} 則評論)
          </span>
          <span v-if="restaurant.distance" class="distance-text">
            • {{ restaurant.distance.toFixed(2) }} km
          </span>
        </div>
      </div>
    </div>
    <div v-if="restaurants.length === 0" class="no-restaurants-message">
        <p v-if="!restaurantDisplayStore.showAllRestaurants && currentUserId">您目前沒有收藏任何餐廳。</p>
        <p v-else-if="!restaurantDisplayStore.showAllRestaurants && !currentUserId">請先登入以查看收藏餐廳。</p>
        <p v-else>目前沒有符合條件的餐廳。</p>
    </div>
  </section>

  <Comment
    v-if="showComment"
    :storeId="selectedStoreId"
    @close="showComment = false"
  />
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Comment from '@/components/Jimmy/Comment.vue';
import { useUserStore } from '@/stores/user';
import axios from '@/plungins/axios.js';
import { useRestaurantDisplayStore } from '@/stores/restaurantDisplay';
import swal from 'sweetalert2';

const userStore = useUserStore();
const currentUserId = computed(() => userStore.userId); // 獲取當前用戶ID
const restaurantDisplayStore = useRestaurantDisplayStore();

const props = defineProps({
  restaurants: { // 這個 props 現在接收的是 Home.vue 經過所有篩選和模式選擇後的結果
    type: Array,
    required: true,
    default: () => [],
  },
});

// 移除 'fetch-restaurants' 事件，因為現在 Home.vue 會監聽 Pinia store 來觸發數據更新
const emit = defineEmits(['update:favoriteStatus']); 
const router = useRouter();

// 控制評論模態框顯示的狀態
const showComment = ref(false);
const selectedStoreId = ref(null); // 用於儲存當前點擊的餐廳 ID

const navigateToRestaurant = (id) => {
  router.push(`/restaurant/${id}`);
};

// 打開評論模態框的函數
const openComment = (storeId) => {
  console.log("User:" + userStore.userId);
  selectedStoreId.value = storeId;
  showComment.value = true;
};

// --- 收藏功能相關方法 ---
const toggleFavorite = async (restaurant) => {
  if (!currentUserId.value) {
    swal.fire({
      icon: 'info',
      title: '請先登入',
      text: '您需要登入才能收藏餐廳！',
      confirmButtonText: '確定'
    });
    return;
  }

  const storeId = restaurant.id;
  const userId = currentUserId.value;
  const originalIsFavorited = restaurant.isFavorited; // 儲存原始狀態，以便回滾

  console.log(`RestaurantListSection.vue: 點擊收藏按鈕，當前狀態 restaurant.isFavorited: ${originalIsFavorited}`);

  // 樂觀更新：立即改變愛心狀態
  restaurant.isFavorited = !originalIsFavorited;
  console.log(`RestaurantListSection.vue: 執行樂觀更新，isFavorited 變為: ${restaurant.isFavorited}`);

  try {
    let response;
    if (originalIsFavorited) {
      response = await axios.delete(`/api/stores/${storeId}/favorite/${userId}`);
      if (response.data.success) {
        // alert('已取消收藏！'); // 通常在樂觀更新後不再需要彈出提示
      } else {
        restaurant.isFavorited = originalIsFavorited; // 如果失敗，回滾狀態
        swal.fire({
          icon: 'error',
          title: '取消收藏失敗！',
          text: '請稍後再試。',
          confirmButtonText: '確定'
        });
        return; // 終止函數執行
      }
    } else {
      response = await axios.post(`/api/stores/${storeId}/favorite/${userId}`);
      if (response.data.success) {
        // alert('已成功收藏！'); // 通常在樂觀更新後不再需要彈出提示
      } else {
        restaurant.isFavorited = originalIsFavorited; // 如果失敗，回滾狀態
        swal.fire({
          icon: 'error',
          title: '收藏失敗！',
          text: '請稍後再試。',
          confirmButtonText: '確定'
        });
        return; // 終止函數執行
      }
    }

    console.log("收藏操作結果:", response.data);

    // 發射事件，通知父組件更新數據
    emit('update:favoriteStatus', {
      storeId: storeId,
      isFavorited: restaurant.isFavorited // 傳遞最終狀態
    });
    console.log(`RestaurantListSection.vue: 發射 update:favoriteStatus 事件，storeId: ${storeId}, isFavorited: ${restaurant.isFavorited}`);

  } catch (error) {
    console.error('收藏/取消收藏操作出錯:', error);
    swal.fire({
      icon: 'error',
      title: '操作發生錯誤！',
      text: '收藏/取消收藏時發生網路或伺服器錯誤，請稍後再試。',
      confirmButtonText: '確定'
    });
    restaurant.isFavorited = originalIsFavorited; // 網路錯誤時回滾
    console.log(`RestaurantListSection.vue: 網路錯誤，isFavorited 回滾為: ${restaurant.isFavorited}`);
  }
};

// 移除監聽 restaurantDisplayStore.showAllRestaurants，因為 Home.vue 會處理數據更新
// watch(() => restaurantDisplayStore.showAllRestaurants, (newValue) => {
//   console.log(`RestaurantDisplayStore.showAllRestaurants changed to: ${newValue}`);
//   emit('fetch-restaurants'); // 通知父組件重新獲取數據
// });

// 移除 onMounted 中的 emit('fetch-restaurants'); 因為現在由 Home.vue 全權管理
// onMounted(() => {
//   emit('fetch-restaurants');
// });

</script>

<style scoped>
/* 餐廳列表的 CSS */
.restaurant-list {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.restaurant-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
  height: 300px;
  position: relative;
  /* 新增：讓子元素可以相對定位 */
}

.restaurant-list .restaurant-card:hover {
  transform: translateY(-3px);
  /* 懸停效果 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.restaurant-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.restaurant-card .info {
  padding: 15px;
}

.restaurant-card h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.restaurant-card p {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.restaurant-card .tags {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.restaurant-card .tags span {
  background-color: #f0f0f0;
  padding: 5px 10px;
  border-radius: 12px;
  font-size: 12px;
}

/* 評分單獨的行，不需要特別的 flex 樣式，它本身就是塊級元素 */
.score-group {
  margin-bottom: 5px; /* 讓評分和下方評論/距離組之間有間距 */
}

/* 新增的評分、評論和距離的父容器樣式 */
.score-comment-distance-group {
  display: flex; /* 使用 Flexbox 讓內部元素水平排列 */
  align-items: center; /* 垂直居中對齊 */
  gap: 5px; /* 為內部元素之間添加間距 */
  flex-wrap: wrap; /* 允許在空間不足時換行，以防萬一 */
}

/* 公里數文字樣式 */
.distance-text {
  font-size: 13px; /* 保持你想要的字體大小 */
  color: #666; /* 保持顏色 */
  white-space: nowrap; /* 防止距離數字換行 */
}

/* 新增的評論觸發文字樣式 */
.comment-trigger-text {
  cursor: pointer;
  color: #007bff;
  /* 藍色鏈接 */
  text-decoration: underline;
  font-size: 13px;
  /* 調整字體大小 */
}

.comment-trigger-text:hover {
  color: #0056b3;
}

/* 愛心圖示樣式 */
.favorite-icon {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 20px;
  /* 調整圖標大小 */
  color: white;
  /* 預設白色 */
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.6);
  /* 增加陰影，讓圖標更明顯 */
  cursor: pointer;
  transition: color 0.2s, transform 0.2s;
  /* 添加過渡效果 */
  z-index: 10;
  /* 確保在圖片上方 */
}

.favorite-icon.bi-heart-fill {
  color: #c50f2a;
  /* 實心愛心顏色，例如粉紅色 */
  text-shadow: 0 0 2px rgba(197, 15, 42, 0.6);
  /* 實心愛心的陰影 */
}

.favorite-icon:hover {
  transform: scale(1.1);
  /* 懸停放大效果 */
}

.no-restaurants-message {
  text-align: center;
  grid-column: 1 / -1;
  /* 讓訊息佔滿整個網格寬度 */
  padding: 20px;
  font-size: 1.2rem;
  color: #666;
}
</style>