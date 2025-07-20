<template>
  <PopularRestaurants
    :restaurants="popularRestaurantsByDistance"
    @comments-data-changed="handleCommentsDataChanged" />

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
      </p>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import TopFilterButtons from '@/components/Jimmy/TopFilterButtons.vue';
import SidebarFilters from '@/components/Jimmy/SidebarFilters.vue';
import PopularRestaurants from '@/components/Jimmy/PopularRestaurants.vue';
import SearchSection from '@/components/Jimmy/SearchSection.vue';
import RestaurantListSection from "@/components/Jimmy/RestaurantListSection.vue"
import axios from '@/plungins/axios.js';
import { useUserStore } from '@/stores/user';
import { useRestaurantDisplayStore } from '@/stores/restaurantDisplay';
import { useLocationStore } from '@/stores/location';

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';


const userStore = useUserStore();
const locationStore = useLocationStore();
const restaurantDisplayStore = useRestaurantDisplayStore();


const API_URL = import.meta.env.VITE_API_URL;
const SEARCH_RADIUS_KM = 3.0; // 定義熱門餐廳的搜索半徑為 3 公里

const WS_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

let stompClient = null;
let scoreSubscription = null;

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

// === STOMP WebSocket 連線函數 ===
const connectStompWebSocket = () => {
  if (stompClient && stompClient.connected) {
    console.log('Home.vue: STOMP Client 已連接，無需重複連接。');
    return;
  }

  const wsUrl = `${WS_BASE_URL}/ws`; // 確保這是你的 Spring Boot WebSocket 端點
  console.log(`Home.vue: 嘗試連線到 WebSocket (STOMP) 伺服器: ${wsUrl}`);

  stompClient = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    debug: (str) => {
      // console.log('STOMP Debug: ' + str); // 在開發模式下可以開啟更多日誌
    },
    reconnectDelay: 5000, // 斷線後 5 秒嘗試重連
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = (frame) => {
    console.log('Home.vue: STOMP Connected: ' + frame);

    const scoreTopic = '/topic/scores'; // **請務必確認後端實際推播的主題**
    console.log(`Home.vue: 訂閱主題: ${scoreTopic}`);

    if (scoreSubscription) {
      scoreSubscription.unsubscribe();
      console.log('Home.vue: 已取消舊的分數訂閱。');
    }

    scoreSubscription = stompClient.subscribe(scoreTopic, (message) => {
      try {
    const update = JSON.parse(message.body);
    console.log('Home.vue: 收到 STOMP 訊息:', update);

    // 不論收到哪種評論相關的更新，都直接觸發重新獲取完整商店數據的流程
    // 假設後端發送的訊息中總會有 storeId
    if (update.storeId !== undefined) {
      console.log(`Home.vue: STOMP 訊息觸發重新載入商店 ID ${update.storeId} 的完整資料...`);
      handleCommentsDataChanged({ storeId: update.storeId }); // 直接呼叫此函數
    }
  } catch (error) {
    console.error('Home.vue: 解析 STOMP 訊息失敗:', error);
  }
}, (error) => {
  console.error(`Home.vue: 訂閱 ${scoreTopic} 錯誤:`, error);
});
  };

  stompClient.onStompError = (frame) => {
    console.error('Home.vue: Broker reported STOMP error: ' + frame.headers['message']);
    console.error('Home.vue: Additional STOMP details: ' + frame.body);
  };

  stompClient.onDisconnect = (frame) => {
    console.log('Home.vue: STOMP 連線已關閉:', frame);
    setTimeout(connectStompWebSocket, 5000);
  };

  stompClient.activate();
};

// === STOMP WebSocket 斷開連線函數 ===
const disconnectStompWebSocket = () => {
  if (stompClient) {
    console.log('Home.vue: 關閉 STOMP WebSocket 連線...');
    if (scoreSubscription) {
      scoreSubscription.unsubscribe();
      scoreSubscription = null;
    }
    stompClient.deactivate();
    stompClient = null;
  }
};


onMounted(async () => {
  if (!locationStore.coordinates) {
    const success = await locationStore.getCurrentLocation();
    if (!success) {
      console.warn("無法獲取當前位置，PopularRestaurants可能無法顯示附近店家。");
    }
  }
  fetchStoresByDisplayMode();
  connectStompWebSocket();
});

onUnmounted(() => {
  disconnectStompWebSocket();
});


const isSidebarActive = ref(false);
const toggleSidebar = () => {
  isSidebarActive.value = !isSidebarActive.value;
};

const searched = ref('');
const searchQuery = ref('');

const updateSearchQuery = (searchTerm) => {
  console.log('Home.vue 收到搜尋內容:', searchTerm);
  searchQuery.value = searchTerm;
  fetchStoresByDisplayMode();
};

const resetSidebarFilters = () => {
  filters.value.category = [];
  filters.value.minscore = 0;
  filters.value.isOpen = false;
};

const handleTopFilterSearch = (keyword) => {
  console.log('Home.vue: 收到 TopFilterButtons 發出的搜尋關鍵字:', keyword);
  resetSidebarFilters();
  searched.value = keyword;
  searchQuery.value = keyword;
  fetchStoresByDisplayMode();
};

const allStores = ref([]);

// 新增：獲取單一餐廳資料的函數 (用於補償 STOMP 訊息不完整的情況)
const fetchStoreById = async (storeId) => {
  try {
    const userId = userStore.userId;
    const url = `/api/stores/${storeId}${userId ? `?userId=${userId}` : ''}`;
    console.log(`Home.vue: 正在獲取單一餐廳 ID ${storeId} 的最新資料 (從 REST API)...`, url);
    const response = await axios.get(url);
    return response.data; // 返回完整的餐廳物件
  } catch (error) {
    console.error(`Home.vue: 獲取商店 ID ${storeId} 資料失敗:`, error);
    return null;
  }
};


const fetchStores = async (searchTerm = '') => {
  try {
    const userId = userStore.userId;
    let url = `/api/stores`;
    const params = {};

    if (searchTerm) {
      params.search = searchTerm;
    }
    if (userId) {
      params.userId = userId;
    }

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

const fetchStoresByDisplayMode = async () => {
  await fetchStores(searchQuery.value);
};

const uniqueCategoryNames = computed(() => {
  const categories = new Set();
  allStores.value.forEach(store => {
    if (store.categoryNames) {
      store.categoryNames.forEach(name => categories.add(name));
    }
  });
  return Array.from(categories).sort();
});

const filteredRestaurants = computed(() => {
  let filtered = [...allStores.value];
  let userLat = null;
  let userLon = null;

  if (locationStore.coordinates && locationStore.coordinates.lat && locationStore.coordinates.lon) {
    userLat = parseFloat(locationStore.coordinates.lat);
    userLon = parseFloat(locationStore.coordinates.lon);
  }

  filtered = filtered.map(store => {
    let distance = null;
    if (userLat !== null && userLon !== null && store.lat && store.lng) {
      distance = calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng));
    }
    return {
      ...store,
      distance: distance
    };
  });

  if (filters.value.category.length > 0) {
    filtered = filtered.filter((store) =>
      store.categoryNames && store.categoryNames.some(catName => filters.value.category.includes(catName))
    );
  }

  filtered = filtered.filter((store) => (store.score || 0) >= filters.value.minscore);

  if (filters.value.isOpen) {
    filtered = filtered.filter((store) => store.isOpen === true);
  }

  if (sortOption.value === '評分最高') {
    filtered = filtered.sort((a, b) => (b.score || 0) - (a.score || 0));
  } else if (sortOption.value === '距離最近') {
    if (locationStore.coordinates && locationStore.coordinates.lat && locationStore.coordinates.lon) {
      const userLat = parseFloat(locationStore.coordinates.lat);
      const userLon = parseFloat(locationStore.coordinates.lon);
      filtered = filtered.map(store => ({
        ...store,
        distance: calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng))
      })).sort((a, b) => (a.distance || Infinity) - (b.distance || Infinity));
    } else {
      console.warn("無法獲取使用者位置，無法按距離最近排序 RestaurantListSection。");
    }
  } else if (sortOption.value === '最快送達') {
    filtered = filtered.sort((a, b) => (a.deliveryTime || Infinity) - (b.deliveryTime || Infinity));
  }

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
    isFavorited: store.isFavorited,
    isOpen: store.isOpen,
    distance: store.distance
  }));
  return mappedRestaurants;
});

const popularRestaurantsByDistance = computed(() => {
  if (!locationStore.coordinates || !locationStore.coordinates.lat || !locationStore.coordinates.lon) {
    console.log('Home.vue: 無法獲取使用者位置，PopularRestaurants 不進行距離篩選。');
    return [];
  }

  const userLat = parseFloat(locationStore.coordinates.lat);
  const userLon = parseFloat(locationStore.coordinates.lon);

  let result = allStores.value.map(store => {
    const distance = calculateDistance(userLat, userLon, parseFloat(store.lat), parseFloat(store.lng));
    return {
      ...store,
      distanceInKilometers: distance
    };
  }).filter(store => {
    return store.distanceInKilometers <= SEARCH_RADIUS_KM;
  });

  if (!restaurantDisplayStore.showAllRestaurants && userStore.userId) {
    result = result.filter(store => store.isFavorited);
  } else if (!restaurantDisplayStore.showAllRestaurants && !userStore.userId) {
    console.log('Home.vue: 用戶未登入，收藏模式下不顯示熱門餐廳。');
    return [];
  }

  result.sort((a, b) => (a.distanceInKilometers || Infinity) - (b.distanceInKilometers || Infinity));
  result = result.slice(0, 20);

  console.log('Home.vue: popularRestaurantsByDistance (已距離篩選與人氣排序), 數量:', result.length, '部分資料範例:', result.slice(0, 2).map(r => ({ id: r.id, name: r.name, distance: r.distanceInKilometers.toFixed(2), popularity: r.popularityScore })));
  return result;
});

const displayedRestaurants = computed(() => {
  let restaurantsToDisplay = [...filteredRestaurants.value];

  if (!restaurantDisplayStore.showAllRestaurants) {
    if (userStore.userId) {
      restaurantsToDisplay = restaurantsToDisplay.filter(r => r.isFavorited);
    } else {
      restaurantsToDisplay = [];
    }
  }
  console.log('Home.vue: displayedRestaurants 最終列表，部分資料範例:', restaurantsToDisplay.slice(0, 2).map(r => ({ id: r.id, name: r.name, isFavorited: r.isFavorited })));
  return restaurantsToDisplay;
});

const handleFavoriteStatusUpdate = ({ storeId, isFavorited }) => {
  console.log('Home.vue: 收到 update:favoriteStatus 事件', { storeId, isFavorited });
  const index = allStores.value.findIndex(store => store.id === storeId);
  if (index !== -1) {
    allStores.value[index].isFavorited = isFavorited;
    console.log(`Home.vue: 更新 allStores[${index}].isFavorited 為 ${allStores.value[index].isFavorited}`);
  }
};

// === 修改後的 handleCommentsDataChanged 函數 (優先使用 HTTP 獲取完整數據) ===
const handleCommentsDataChanged = async ({ storeId }) => { // 移除 newCommentCount 和 newAverageScore 參數
  console.log(`Home.vue: 收到 comments-data-changed 事件，正在重新獲取餐廳 ID ${storeId} 的最新資料 (透過 HTTP)...`);

  const updatedStore = await fetchStoreById(storeId); // 透過 HTTP 請求獲取最新完整數據
  
  if (updatedStore) {
    const storeIndex = allStores.value.findIndex(store => store.id === storeId);
    if (storeIndex !== -1) {
      // **關鍵：用新物件替換舊物件，確保觸發響應式更新**
      allStores.value[storeIndex] = updatedStore;
      console.log(`Home.vue: 已成功更新餐廳 ID ${storeId} 的完整資料。新評論數: ${updatedStore.comments ? updatedStore.comments.length : 0}, 新平均分數: ${updatedStore.score}`);
    } else {
      console.warn(`Home.vue: 在 allStores 中未找到 ID 為 ${storeId} 的餐廳來更新。`);
    }
  } else {
    console.error(`Home.vue: 無法獲取餐廳 ID ${storeId} 的最新資料，評論數據可能無法即時更新。`);
  }
};


const filters = ref({
  category: [],
  minscore: 0,
  isOpen: false,
});

const sortOption = ref('評分最高');


const updatescore = () => {
  // 觸發篩選更新 (由於 computed property 會自動響應 filters 變化，這裡可能不需要額外邏輯)
};

const sortRestaurants = () => {
  // 由 filteredRestaurants computed 屬性處理，這裡不需要額外邏輯
};
</script>

<style scoped>
/* 樣式保持不變，無需修改 */
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
  flex-shrink: 0;
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