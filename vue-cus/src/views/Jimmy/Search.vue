<template>
  <PopularRestaurants :restaurants="popularRestaurantsByDistance" />

  <SearchSection v-model:initialSearch="searched" @search="updateSearchQuery" />

  <TopFilterButtons
    v-model="sortOption"
    @search-keyword="handleTopFilterSearch"
  />

  <div class="filter-toggle" @click="toggleSidebar">篩選條件</div>
  <div class="content-container">
    <aside class="sidebar" :class="{ active: isSidebarActive }">
      <SidebarFilters
        :filters="filters"
        @update:filters="filters = $event"
        @update-score="updatescore"
        :availableCategories="uniqueCategoryNames" />
    </aside>

    <RestaurantListSection :restaurants="displayedRestaurants" @update:favoriteStatus="handleFavoriteStatusUpdate" />
  </div>

  <footer class="footer">
    <p>© 2025 金碗平台. 版權所有。</p>
    <p>
      <a href="#">關於我們</a>
      <a href="#">聯繫我們</a>
      <a href="#">隱私政策</a>
      <a href="#">服務條款</a>
      <!-- 您的使用者 ID: {{ userStore.userId }} -->
    </p>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import TopFilterButtons from '@/components/Jimmy/TopFilterButtons.vue';
import SidebarFilters from '@/components/Jimmy/SidebarFilters.vue';
import PopularRestaurants from '@/components/Jimmy/PopularRestaurants.vue';
import SearchSection from '@/components/Jimmy/SearchSection.vue';
import RestaurantListSection from "@/components/Jimmy/RestaurantListSection.vue"
import axios from 'axios';
import { useUserStore } from '@/stores/user';
import { useRestaurantDisplayStore } from '@/stores/restaurantDisplay';
import { useLocationStore } from '@/stores/location';


const userStore = useUserStore();
const locationStore = useLocationStore(); 
const restaurantDisplayStore = useRestaurantDisplayStore();


const API_URL = import.meta.env.VITE_API_URL;
const SEARCH_RADIUS_KM = 3.0; // 定義熱門餐廳的搜索半徑為 3 公里

// Haversine 公式計算距離 (單位: 公里)
const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371; // 地球半徑，單位公里
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // 距離，單位公里
};


onMounted(async () => { // 這裡應該是 async，因為 getCurrentLocation 是 async 的
  // 首次載入時，嘗試獲取當前位置。
  // 只有當成功獲取位置後，或者已經有緩存位置，才去載入餐廳。
  if (!locationStore.coordinates) { // 如果沒有座標，嘗試獲取
    const success = await locationStore.getCurrentLocation();
    if (!success) {
      console.warn("無法獲取當前位置，PopularRestaurants可能無法顯示附近店家。");
    }
  }
  // 無論是否成功獲取到當前位置，都嘗試獲取餐廳數據
  fetchStoresByDisplayMode();
});

// 當 userId 或顯示模式改變時，重新獲取商店數據
// 新增對 locationStore.coordinates 的監聽
watch([
  () => userStore.userId,
  () => restaurantDisplayStore.showAllRestaurants,
  () => locationStore.coordinates // <-- 新增: 監聽座標變化
], () => {
  console.log('User ID, display mode, or coordinates changed, refetching stores...');
  fetchStoresByDisplayMode();
}, { immediate: false, deep: true }); // immediate: false 防止在組件初始化時觸發兩次 fetch, deep: true 確保 coordinates 內部變化也能觸發

const isSidebarActive = ref(false);
const toggleSidebar = () => {
  isSidebarActive.value = !isSidebarActive.value;
};

// 搜索相關
const searched = ref(''); // 用於與 SearchSection 的 v-model 綁定
const searchQuery = ref(''); // 新增：實際用於觸發後端搜尋的查詢詞

// 處理來自 SearchSection 的搜尋事件
const updateSearchQuery = (searchTerm) => {
  console.log('Home.vue 收到搜尋內容:', searchTerm);
  searchQuery.value = searchTerm;
  fetchStoresByDisplayMode(); // 當搜尋詞改變時，重新從後端獲取資料
};

const resetSidebarFilters = () => {
  filters.value.category = [];
  filters.value.minscore = 0;
  filters.value.isOpen = false;
};

// 處理來自 TopFilterButtons 的搜尋關鍵字
const handleTopFilterSearch = (keyword) => {
  console.log('Home.vue: 收到 TopFilterButtons 發出的搜尋關鍵字:', keyword);

  // 清空所有側邊欄篩選，確保推薦搜尋的獨立性
  resetSidebarFilters();

  // 將關鍵字設定給實際的搜尋參數 `searchQuery`
  searchQuery.value = keyword;
  // **不修改 `searched.value`，這樣搜尋欄位的顯示就不會改變**

  // 直接觸發重新獲取資料
  fetchStoresByDisplayMode();
};

// 餐廳數據 (從後端取得)
const allStores = ref([]); // 存放從後端取得的原始 Store 數據

// 異步函數：從後端獲取 Store 數據
const fetchStores = async (searchTerm = '') => {
  try {
    const userId = userStore.userId; // 獲取當前用戶ID
    let url = `${API_URL}/api/stores`;
    const params = {};

    if (searchTerm) {
      params.search = searchTerm;
    }
    if (userId) {
      params.userId = userId; // 將 userId 作為參數傳遞
    }

    // 使用 URLSearchParams 構建查詢字符串，自動處理編碼
    const queryString = new URLSearchParams(params).toString();
    if (queryString) {
      url += `?${queryString}`;
    }

    console.log("Fetching stores from URL:", url);
    const response = await axios.get(url);
    allStores.value = response.data;
    console.log("從後端取得的商店資料:", allStores.value);
  } catch (error) {
    console.error('獲取商店資料失敗:', error);
  }
};

// 當 RestaurantListSection 或 Navbar 要求刷新時調用此函數
const fetchStoresByDisplayMode = async () => {
  // 總是先獲取全部數據（包含 isFavorited 標記），然後由 computed 屬性篩選
  await fetchStores(searchQuery.value);
};


// 新增 computed 屬性來收集所有餐廳的唯一類別名稱
const uniqueCategoryNames = computed(() => {
  const categories = new Set();
  allStores.value.forEach(store => {
    if (store.categoryNames) {
      store.categoryNames.forEach(name => categories.add(name));
    }
  });
  return Array.from(categories).sort(); // 返回排序後的唯一類別名稱陣列
});

// 計算屬性：應用分類、評分、開放狀態和排序後的餐廳列表
const filteredRestaurants = computed(() => {
  let filtered = [...allStores.value];
  let userLat = null;
  let userLon = null;

  // **新增：在過濾和排序之前，先嘗試獲取用戶位置並計算所有店家的距離**
  if (locationStore.coordinates && locationStore.coordinates.lat && locationStore.coordinates.lon) {
    userLat = parseFloat(locationStore.coordinates.lat);
    userLon = parseFloat(locationStore.coordinates.lon);
  }

  // 為所有店家計算距離，無論是否按距離排序
  // 這確保了每個餐廳物件都有 distance 屬性，即使值為 null
  filtered = filtered.map(store => {
    let distance = null;
    // 只有當用戶位置和店家位置都存在時才計算距離
    if (userLat !== null && userLon !== null && store.lat && store.lng) {
      distance = calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng));
    }
    return {
      ...store,
      distance: distance // 將計算出的距離添加到每個 store 物件中
    };
  });

  // 第一步：根據篩選條件進行過濾 (分類、評分、開放狀態)
  if (filters.value.category.length > 0) {
    filtered = filtered.filter((store) =>
      store.categoryNames && store.categoryNames.some(catName => filters.value.category.includes(catName))
    );
  }

  filtered = filtered.filter((store) => (store.score || 0) >= filters.value.minscore);

  if (filters.value.isOpen) {
    filtered = filtered.filter((store) => store.isOpen === true);
  }

  // 第二步：應用排序
  if (sortOption.value === '評分最高') {
    filtered = filtered.sort((a, b) => (b.score || 0) - (a.score || 0));
  } else if (sortOption.value === '距離最近') {
    // 這裡的「距離最近」應該是針對 RestaurantListSection，
    // 所以需要在 mappedRestaurants 中計算並提供距離，然後再排序
    // 這裡需要確保 store 數據中有 lat/lng
    if (locationStore.coordinates && locationStore.coordinates.lat && locationStore.coordinates.lon) {
      const userLat = parseFloat(locationStore.coordinates.lat);
      const userLon = parseFloat(locationStore.coordinates.lon);
      filtered = filtered.map(store => ({
        ...store,
        // 為每個店家計算距離，以便排序
        distance: calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng))
      })).sort((a, b) => (a.distance || Infinity) - (b.distance || Infinity));
    } else {
      console.warn("無法獲取使用者位置，無法按距離最近排序 RestaurantListSection。");
    }
  } else if (sortOption.value === '最快送達') {
    filtered = filtered.sort((a, b) => (a.deliveryTime || Infinity) - (b.deliveryTime || Infinity));
  }

  // 第三步：將處理後的 StoreDTO 轉換為 RestaurantListSection 需要的格式
  const mappedRestaurants = filtered.map(store => ({
    id: store.id,
    name: store.name,
    categoryNames: store.categoryNames || [],
    deliveryTime: store.deliveryTime || 20,
    score: store.score || 0,
    comments: store.comments || [],
    tags: store.foods ? [...new Set(store.foods.flatMap(food => food.tagNames || []))] : [],
    photo: store.photo,
    promo: '',
    popularityScore: store.popularityScore != null ? store.popularityScore : (store.score != null ? parseFloat(store.score) * 10 : 0),
    isFavorited: store.isFavorited, // 確保這裡正確映射了 isFavorited
    isOpen: store.isOpen,
    distance: store.distance // 新增：傳遞距離資訊
  }));
  // console.log('Home.vue: filteredRestaurants (pre-display-mode) 重新計算，部分資料範例:', mappedRestaurants.slice(0, 2).map(r => ({ id: r.id, name: r.name, isFavorited: r.isFavorited })));
  return mappedRestaurants;
});

// 新增：專門用於 PopularRestaurants 的 computed 屬性
const popularRestaurantsByDistance = computed(() => {
  // 確保有使用者位置座標
  if (!locationStore.coordinates || !locationStore.coordinates.lat || !locationStore.coordinates.lon) {
    console.log('Home.vue: 無法獲取使用者位置，PopularRestaurants 不進行距離篩選。');
    return []; // 如果沒有位置資訊，則不顯示熱門餐廳
  }

  const userLat = parseFloat(locationStore.coordinates.lat);
  const userLon = parseFloat(locationStore.coordinates.lon);

  let result = allStores.value.map(store => {
    // 計算每個店家與使用者的距離
    const distance = calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng));
    return {
      ...store,
      distanceInKilometers: distance // 將距離添加到店家物件中
    };
  }).filter(store => {
    // 篩選出在半徑內的店家
    return store.distanceInKilometers <= SEARCH_RADIUS_KM;
  });

  // 根據 restaurantDisplayStore.showAllRestaurants 篩選是否為收藏餐廳 
  if (!restaurantDisplayStore.showAllRestaurants && userStore.userId) { // 如果是「已收藏」模式且用戶已登入
    result = result.filter(store => store.isFavorited);
  } else if (!restaurantDisplayStore.showAllRestaurants && !userStore.userId) {
    // 如果是「已收藏」模式但用戶未登入，則不顯示任何餐廳
    console.log('Home.vue: 用戶未登入，收藏模式下不顯示熱門餐廳。');
    return [];
  }

  // 對熱門餐廳進行排序：按 distanceInKilometers 升序排列 (距離最近的在前)
  result.sort((a, b) => (a.distanceInKilometers || Infinity) - (b.distanceInKilometers || Infinity));

  // 限制熱門餐廳的數量，例如只顯示前 20 個
  result = result.slice(0, 20);

  console.log('Home.vue: popularRestaurantsByDistance (已距離篩選與人氣排序), 數量:', result.length, '部分資料範例:', result.slice(0, 2).map(r => ({ id: r.id, name: r.name, distance: r.distanceInKilometers.toFixed(2), popularity: r.popularityScore })));
  return result;
});

// **新增：最終顯示給子組件的餐廳列表**
const displayedRestaurants = computed(() => {
  let restaurantsToDisplay = [...filteredRestaurants.value]; // 從應用過濾和排序的列表開始

  // 如果是「已收藏」模式，則從 current filtered list 中再次篩選
  if (!restaurantDisplayStore.showAllRestaurants) {
    if (userStore.userId) { // 確保用戶已登入
      restaurantsToDisplay = restaurantsToDisplay.filter(r => r.isFavorited);
    } else {
      restaurantsToDisplay = []; // 未登入狀態下，收藏為空
    }
  }
  console.log('Home.vue: displayedRestaurants 最終列表，部分資料範例:', restaurantsToDisplay.slice(0, 2).map(r => ({ id: r.id, name: r.name, isFavorited: r.isFavorited })));
  return restaurantsToDisplay;
});

// 處理收藏狀態更新的函數
const handleFavoriteStatusUpdate = ({ storeId, isFavorited }) => {
  console.log('Home.vue: 收到 update:favoriteStatus 事件', { storeId, isFavorited });
  const index = allStores.value.findIndex(store => store.id === storeId);
  if (index !== -1) {
    allStores.value[index].isFavorited = isFavorited;
    console.log(`Home.vue: 更新 allStores[${index}].isFavorited 為 ${allStores.value[index].isFavorited}`);
    // 不需要手動觸發 computed，因為 allStores 已經是響應式的 ref
  }
};

// 篩選條件
const filters = ref({
  category: [],
  minscore: 0,
  isOpen: false,
});

// 排序選項
const sortOption = ref('評分最高');


// 更新配送時間 (這可能是一個佔位符，實際用途不明)
const updatescore = () => {
  // 觸發篩選更新 (由於 computed property 會自動響應 filters 變化，這裡可能不需要額外邏輯)
};

// 排序餐廳（實際上由 computed 處理，這裡僅為兼容）
const sortRestaurants = () => {
  // 由 filteredRestaurants computed 屬性處理，這裡不需要額外邏輯
};
</script>

<style scoped>
/* 這裡保留 Home.vue 原有的全局或佈局相關 CSS */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: Arial, sans-serif;
}

body {
  background-color: #f7f7f7;
}

/* 篩選與排序（頂部） */
.filters {
  padding: 15px 20px;
  background-color: #fff;
  margin: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filters .sort select {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

/* 內容容器 */
.content-container {
  display: flex;
  padding: 20px;
  gap: 20px;
}

/* 左側篩選欄的 CSS 保持不變，因為它仍然在 Home.vue 中 */
.sidebar {
  width: 250px;
  /* 固定寬度，可根據需求調整 */
  flex-shrink: 0;
  /* 防止縮小 */
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}


/* 頁腳 */
.footer {
  background-color: #333;
  color: white;
  padding: 20px;
  text-align: center;
  margin-top: 30px;
}

.footer a {
  color: #ffba20;
  text-decoration: none;
  margin: 0 10px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .content-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    display: none;
  }

  .sidebar.active {
    display: block;
  }

  .filter-toggle {
    display: block;
    padding: 10px;
    background-color: #ffba20;
    color: white;
    text-align: center;
    cursor: pointer;
    margin: 10px 20px;
    border-radius: 4px;
  }

  .filters {
    margin: 10px 10px 20px;
  }
}

@media (min-width: 769px) {
  .filter-toggle {
    display: none;
  }
}
</style>