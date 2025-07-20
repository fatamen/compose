<template>
  <section class="popular-section" v-if="locationStore.address && restaurants.length > 0">
    <h2>附近熱門美食</h2>
    <div class="restaurant-scroll">
      <div class="restaurant-card" v-for="restaurant in restaurants" :key="restaurant.id">
        <img
          :src="restaurant.photo || '/path/to/default-popular-image.jpg'" :alt="restaurant.name"
          @click="navigateToRestaurant(restaurant.id)"
          style="cursor: pointer;"
        />
        <div class="info">
          <h3>
            {{ restaurant.name }} {{ restaurant.score ? restaurant.score.toFixed(1) : 'N/A' }}★
          </h3>
          <p class="comment-and-distance-group"> <span class="comment-trigger-text" @click="openCommentModal(restaurant.id)" style="cursor: pointer;">
              ({{ restaurant.comments ? restaurant.comments.length : 0 }} 則評論)
            </span>
            <span v-if="restaurant.distanceInKilometers" class="distance-text">
              • {{ restaurant.distanceInKilometers.toFixed(2) }} km </span>
          </p>
        </div>
      </div>
    </div>
  </section>
  <section class="popular-section" v-else-if="locationStore.address && restaurants.length === 0">
    <h2>附近熱門美食</h2>
    <p class="no-restaurants-message">附近沒有符合條件的餐廳。</p>
  </section>
  <section class="popular-section" v-else>
    <h2>附近熱門美食</h2>
    <p class="no-restaurants-message">請開啟定位以查看附近熱門餐廳。</p>
  </section>

  <CommentModal
    v-if="showCommentModal"
    :storeId="selectedStoreId"
    @close="showCommentModal = false"
  />
</template>

<script setup>
import { ref} from 'vue'; // 引入 ref
import { useRouter } from 'vue-router';
import CommentModal from '@/components/Jimmy/Comment.vue'; // <-- 新增這一行
import { useLocationStore } from '@/stores/location';

// 定義 Props
const props = defineProps({  
  restaurants: { // 這個 props 現在預期接收的是 Home.vue 轉換後的餐廳數據
    type: Array,
    required: true,
    default: () => [],
  },
});

const router = useRouter(); // 初始化 useRouter
const locationStore = useLocationStore(); 

// 控制評論模態框顯示的狀態
const showCommentModal = ref(false);
const selectedStoreId = ref(null); // 用於儲存當前點擊的餐廳 ID

// 定義導航方法
const navigateToRestaurant = (id) => {
  router.push(`/restaurant/${id}`);
};

// 打開評論模態框的函數
const openCommentModal = (storeId) => {
  selectedStoreId.value = storeId;
  showCommentModal.value = true;
};
</script>

<style scoped>
.popular-section {
  padding: 10px;
  background-color: #fff;
  margin: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.popular-section h2 {
  font-size: 23px;
  font-weight: bold; 
  margin-bottom: 15px;
  color: #333;
}

.restaurant-scroll {
  display: flex;
  overflow-x: auto;
  gap: 10px;
  padding-bottom: 5px; /* 為滾動條留出空間 */
  -webkit-overflow-scrolling: touch; /* 提升 iOS 上的滾動體驗 */
}

/* 自定義滾動條樣式 */
.restaurant-scroll::-webkit-scrollbar {
  height: 6px; /* 滾動條高度 */
}

.restaurant-scroll::-webkit-scrollbar-track {
  background: #f1f1f1; /* 滾動條軌道背景 */
  border-radius: 10px;
}

.restaurant-scroll::-webkit-scrollbar-thumb {
  background-color: #ffba20; /* 滾動條滑塊顏色 */
  border-radius: 10px;
  border: 1px solid #f1f1f1; /* 滑塊邊框 */
}

.restaurant-scroll::-webkit-scrollbar-thumb:hover {
  background-color: #e6a000; /* 滑塊懸停顏色 */
}

.restaurant-scroll .restaurant-card {
  flex: 0 0 180px; /* 固定寬度，不壓縮 */
  width: 180px; /* 確保寬度一致 */
  height: 200px;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease-in-out; /* 添加過渡效果 */
}

.restaurant-scroll .restaurant-card:hover {
  transform: translateY(-3px); /* 懸停效果 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.restaurant-card img {
  width: 100%;
  height: 120px;
  object-fit: cover; /* 圖片裁剪並覆蓋 */
  border-bottom: 1px solid #eee; /* 圖片底部邊框 */
}

.restaurant-card .info {
  padding: 7.5px 10px; /* 調整內邊距 */
}

.restaurant-card h3 {
  font-weight: bold; 
  font-size: 16px;
  margin-top: 5px; /* 調整標題上邊距 */
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

/* 新增的評論和距離的父容器樣式 */
.comment-and-distance-group {
  display: inline-flex; /* <-- 這裡改成 inline-flex */
  align-items: center; /* 垂直居中對齊 */
  gap: 5px; /* 給評論和距離之間添加一點間距 */
  margin-top: 5px; /* 調整與上方 h3 的間距 */
}

/* 新增的評論觸發文字樣式 */
.comment-trigger-text {
  cursor: pointer;
  color: #007bff; /* 藍色鏈接 */
  text-decoration: underline;
  font-size: 13px; /* 調整字體大小 */
  display: block; /* 確保獨佔一行 */
  margin-top: 5px;
}

.comment-trigger-text:hover {
  color: #0056b3;
}

/* 公里數文字樣式 */
.distance-text {
  font-size: 13px; /* 保持你想要的字體大小 */
  color: #666; /* 保持顏色 */
  white-space: nowrap; /* 防止距離數字換行 */
}

.no-restaurants-message {
  text-align: center;
  padding: 20px;
  font-size: 1.2rem;
  color: #666;
}
</style>